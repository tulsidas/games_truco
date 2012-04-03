package truco.server.torneo;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.TwoPlayersServerRoom;
import server.db.RedisManager;
import truco.common.messages.server.TorneoRoomJoinedMessage;
import truco.common.model.RoomInfo;
import truco.common.model.TrucoRoom;
import truco.server.TrucoSaloon;
import truco.server.TrucoServerRoom;
import truco.server.torneo.Arbol.Estado;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import common.game.OponentAbandonedMessage;
import common.model.User;

public class Torneo {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static enum EstadoTorneo {
        // todavia no se llenó el cupo
        INCOMPLETO,
        // jugando
        EN_CURSO,
        // hay un campeon
        TERMINADO
    }

    private TrucoSaloon saloon;

    private Arbol raiz;

    private final int players, puntos;

    private AtomicReference<EstadoTorneo> estadoTorneo;

    private Map<IoSession, TrucoServerRoom> rooms;

    private Map<TrucoServerRoom, Integer> rondas;

    private Set<TrucoServerRoom> terminados;

    private User campeon;

    private ReadWriteLock lock;

    public Torneo(TrucoSaloon saloon, int players, int puntos) {
        this.saloon = saloon;
        this.raiz = new Arbol((int) (Math.log(players) / Math.log(2)));

        this.players = players;
        this.puntos = puntos;

        estadoTorneo = new AtomicReference<EstadoTorneo>(
                EstadoTorneo.INCOMPLETO);

        rooms = Collections
                .synchronizedMap(new HashMap<IoSession, TrucoServerRoom>());

        rondas = new HashMap<TrucoServerRoom, Integer>();

        terminados = Collections
                .synchronizedSet(new HashSet<TrucoServerRoom>());

        lock = new ReentrantReadWriteLock();
    }

    public int getPuntos() {
        return puntos;
    }

    public EstadoTorneo getEstado() {
        return estadoTorneo.get();
    }

    public User getCampeon() {
        return campeon;
    }

    public int getFaltan() {
        lock.readLock().lock();
        try {
            int hay = Collections2.filter(raiz.getHojas(),
                    new Predicate<Arbol>() {
                        @Override
                        public boolean apply(Arbol a) {
                            return a.getEstado() == Estado.LISTO;
                        }
                    }).size();
            return players - hay;
        }
        finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Los players que estan en el lobby, sea de espectadores o esperando jugar
     * 
     * @return
     */
    public Collection<IoSession> getEnLobby() {
        lock.readLock().lock();
        try {
            Collection<Arbol> listos = Collections2.filter(raiz.getHojas(),
                    new Predicate<Arbol>() {
                        @Override
                        public boolean apply(Arbol a) {
                            return a.getPlayer() != null
                                    && a.getEstado() == Estado.LISTO;
                        }
                    });

            Collection<IoSession> ret = Collections2.transform(listos,
                    new Function<Arbol, IoSession>() {
                        @Override
                        public IoSession apply(Arbol a) {
                            return a.getPlayer();
                        }
                    });
            return ret;
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public void start() {
        log.debug("Torneo - START");
        log.debug("Torneo - " + raiz.toString(saloon));

        Iterable<User> players = Iterables.transform(raiz.getPlayers(),
                new Function<IoSession, User>() {
                    @Override
                    public User apply(IoSession sess) {
                        return saloon.getUser(sess);
                    }
                });
        RedisManager.sacarPuntos(players, puntos);

        estadoTorneo.set(EstadoTorneo.EN_CURSO);

        startMatches();
    }

    public boolean agregar(IoSession nuevo) {
        lock.writeLock().lock();
        try {
            return raiz.agregar(nuevo);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public void userAbandoned(IoSession sess) {
        // Opciones
        // 1) está en el lobby de torneo como espectador (o se desconectó)
        // 2) está en el lobby de torneo y el torneo no empezó aún
        // 3) está en el lobby de torneo y está jugando
        // 4) antes de entrar al partido

        lock.writeLock().lock();
        try {
            if (jugando(sess)) {
                if (getEstado() == EstadoTorneo.EN_CURSO) {
                    TwoPlayersServerRoom room = rooms.get(sess);
                    if (room != null) { // 4)
                        log.debug("Torneo - " + saloon.getUser(sess)
                                + " abandono el torneo (4)");

                        IoSession op = room.getOtherPlayer(sess);
                        if (room.isReady(op)) {
                            // si el otro está en la sala, avisarle que este
                            // abandono y pasarlo de ronda
                            op.write(new OponentAbandonedMessage(true, null));
                            gano(op);
                        }
                        else {
                            // si no, marcarlo como abandonado y cuando entre
                            // avisarle que el otro abandono
                            room.setAbandoned(sess);

                            // ambos abandonaron
                            if (room.isAbandoned(op)) {
                                Arbol nodo = raiz.getNodo(sess).getPadre();
                                nodo.pruneChild();
                                nodo.setEstado(Estado.ABANDONO);
                            }
                        }
                    }
                    else { // 3)
                        log.debug("Torneo - " + saloon.getUser(sess)
                                + " abandono el torneo (3)");
                        // lo marco como abandonado
                        Arbol nodo = raiz.getNodo(sess);
                        nodo.setEstado(Estado.ABANDONO);

                        // si el oponente está listo, pasa de ronda
                        Arbol oponente = nodo.getSibling();
                        if (oponente.getEstado() == Estado.LISTO
                                && oponente.getPlayer() != null) {
                            gano(oponente.getPlayer());
                        }
                    }
                }
                else if (getEstado() == EstadoTorneo.INCOMPLETO) { // 2)
                    log.debug("Torneo - " + saloon.getUser(sess)
                            + " abandono el torneo (2)");
                    // adios
                    raiz.sacar(sess);
                }
                else { // EstadoTorneo.TERMINADO
                    log.debug("Torneo - " + saloon.getUser(sess)
                            + " abandono el torneo (TERMINADO)");
                }
            }
            else { // 1)
                User u = saloon.getUser(sess);
                if (u != null) {
                    log.debug("Torneo - " + u + " abandono el torneo (1)");
                }
            }

        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public boolean todosListos() {
        lock.readLock().lock();
        try {
            return raiz.todosListos();
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public boolean jugando(IoSession s1, IoSession s2) {
        lock.readLock().lock();
        try {
            Arbol a1 = raiz.getNodo(s1);
            Arbol a2 = raiz.getNodo(s2);

            boolean ret = a1 != null && a2 != null
                    && a1.getEstado() == Estado.JUGANDO
                    && a2.getEstado() == Estado.JUGANDO
                    && a1.getPadre().equals(a2.getPadre());

            return ret;
        }
        finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @return si sess esta jugando el torneo (no perdio o abandono)
     */
    public boolean jugando(IoSession sess) {
        lock.readLock().lock();
        try {
            return raiz.getNodo(sess) != null;
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public void gano(IoSession ganador) {
        lock.writeLock().lock();

        try {
            log.debug("Torneo - " + saloon.getUser(ganador)
                    + " gano la partida");

            Arbol nodo = raiz.getNodo(ganador);
            Estado estado = nodo.getEstado();

            IoSession perdedor = nodo.getSibling().getPlayer();

            // lo subo uno
            nodo.getPadre().setPlayer(ganador);
            nodo = nodo.getPadre();
            nodo.pruneChild();
            if (estado == Estado.LISTO) {
                nodo.setEstado(Estado.LISTO);
            }

            // saco la sala en la que estaban jugando
            rooms.remove(ganador);
            TrucoServerRoom tsr = rooms.remove(perdedor);

            // y la agrego a "terminados"
            if (tsr != null) { // si fue un partido que no se jugo, porque el
                // otro
                // abandono antes de empezar, el room no existe
                terminados.add(tsr);
            }

            // campeon?
            if (nodo.getPadre() == null) {
                campeon = saloon.getUser(ganador);
                RedisManager.darPuntos(campeon, puntos * players);

                log.debug("Torneo - " + campeon + " es el campeon");

                // fin del torneo
                estadoTorneo.set(EstadoTorneo.TERMINADO);

                saloon.torneoTerminado();
            }
            else {
                Arbol oponente = nodo.getSibling();
                if (oponente.getEstado() == Estado.ABANDONO) {
                    // pasa automaticamente de ronda
                    gano(ganador);
                }
                // else if (oponente.getEstado() == Estado.LISTO) {
                // se arma solo cuando ambos están en TorneoScene
                // startMatch(nodo.getPadre());
                // }
                // else {
                // // hay que esperar
                // nodo.setEstado(Estado.LISTO);
                // }
            }
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public void setPlayerReady(IoSession player) {
        lock.writeLock().lock();

        try {
            log.debug("Torneo - " + saloon.getUser(player) + " is ready");

            Arbol nodo = raiz.getNodo(player);
            nodo.setEstado(Estado.LISTO);

            if (getEstado() == EstadoTorneo.EN_CURSO) {
                startMatches();
            }
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    private synchronized void startMatches() {
        lock.writeLock().lock();

        try {
            for (Arbol match : raiz.getMatches()) {
                // los players
                if (match.getIzq().getEstado() == Estado.LISTO
                        && match.getDer().getEstado() == Estado.LISTO) {
                    startMatch(match);
                }
            }
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    private void startMatch(Arbol match) {
        IoSession izq = match.getIzq().getPlayer();
        IoSession der = match.getDer().getPlayer();

        boolean a30 = false; // XXX hardcoded
        boolean conFlor = false; // XXX hardcoded

        User user1 = saloon.getUser(izq);
        User user2 = saloon.getUser(der);

        // meto a ambos en la misma sala
        TrucoServerRoom tsr = new TrucoServerRoom(saloon, izq, puntos, a30,
                conFlor);
        tsr.join(der);
        tsr.setUser1(user1.getName());
        tsr.setUser2(user2.getName());

        rooms.put(izq, tsr);
        rooms.put(der, tsr);
        rondas.put(tsr, match.getDepth());

        // Room description
        TrucoRoom room = new TrucoRoom(tsr.getId(), puntos, a30 ? 30 : 15,
                conFlor, Lists.newArrayList(user1, user2));

        // jugando
        match.getIzq().setEstado(Estado.JUGANDO);
        match.getDer().setEstado(Estado.JUGANDO);

        // aviso que creo y se unio (en la izq)
        izq.write(new TorneoRoomJoinedMessage(room));
        der.write(new TorneoRoomJoinedMessage(room));

        log.debug("Torneo - arranca partido entre " + saloon.getUser(izq)
                + " y " + saloon.getUser(der));

        saloon.notifyNewPartidoTorneo(room, match.getDepth());
    }

    /**
     * @param player
     * @return el Room donde esta player
     */
    public TrucoServerRoom getRoom(IoSession session) {
        return rooms.get(session);
    }

    public Collection<RoomInfo> getRoomInfos() {
        lock.readLock().lock();
        try {
            Collection<TrucoServerRoom> tsrs = new HashSet<TrucoServerRoom>(
                    rooms.values());
            tsrs.addAll(terminados);
            Collection<RoomInfo> ret = new HashSet<RoomInfo>(tsrs.size());

            for (TrucoServerRoom tsr : tsrs) {
                int ronda = rondas.get(tsr);

                ret.add(new RoomInfo(tsr.getId(), ronda, tsr.getUser1(), tsr
                        .getUser2(), tsr.getPuntaje1(), tsr.getPuntaje2()));
            }

            return ret;
        }
        finally {
            lock.readLock().unlock();
        }
    }

    // chau torneo!
    public synchronized void cancelar() {
        Set<IoSession> todos = Sets.newHashSet();
        todos.addAll(getEnLobby());
        Iterables.addAll(todos, raiz.getPlayers());

        for (IoSession sess : todos) {
            if (sess != null) {
                sess.close();
            }
        }
    }
}

// TODO permitir jugadores que no sean potencias de 2
// TODO truncar el largo de nombre en el TorneoBox
// TODO hacer un simil Option del AbstractServerRoom para que no tire NPE