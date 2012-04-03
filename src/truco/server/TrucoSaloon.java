package truco.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.AbstractSaloon;
import server.TwoPlayersServerRoom;
import truco.common.ifaz.TrucoSaloonHandler;
import truco.common.ifaz.TrucoServerHandler;
import truco.common.messages.AbandonedTorneoMessage;
import truco.common.messages.JoinTorneoMessage;
import truco.common.messages.TorneoChatMessage;
import truco.common.messages.client.AbandonTorneoMessage;
import truco.common.messages.server.CampeonTorneoMessage;
import truco.common.messages.server.InfoTorneoMessage;
import truco.common.messages.server.NewPartidoTorneoMessage;
import truco.common.messages.server.PartidosTorneoMessage;
import truco.common.messages.server.TorneoStatusMessage;
import truco.common.messages.server.UpdatePartidoTorneoMessage;
import truco.common.model.Carta;
import truco.common.model.TrucoRoom;
import truco.server.torneo.Torneo;
import truco.server.torneo.Torneo.EstadoTorneo;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import common.ifaz.POSTHandler;
import common.messages.Message;
import common.messages.server.OPMessage;
import common.model.User;

public class TrucoSaloon extends AbstractSaloon implements TrucoServerHandler,
        TrucoSaloonHandler {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Torneo torneo;

    private Set<IoSession> espectadoresTorneo;

    public TrucoSaloon(int id, POSTHandler poster) {
        super(id, poster);

        Set<IoSession> s = Sets.newHashSet();
        espectadoresTorneo = Collections.synchronizedSet(s);

        // if (id == 2) {
        // new Thread() {
        // public void run() {
        // while (true) {
        // System.out.println("lobby = " + lobby.size());
        // System.out.println("espectadoresTorneo = "
        // + espectadoresTorneo.size());
        // System.out.println("rooms = " + rooms.size());
        // System.out.println("users = " + users.size());
        // try {
        // Thread.sleep(10 * 1000);
        // }
        // catch (InterruptedException e) {
        // e.printStackTrace();
        // }
        // }
        // };
        // }.start();
        // }
    }

    @Override
    public void createRoom(IoSession session, int puntos) {
        createRoom(session, puntos, false, false);
    }

    @Override
    public void createRoom(IoSession session, int puntos, boolean a30,
            boolean conFlor) {
        TwoPlayersServerRoom tsr = new TrucoServerRoom(this, session, puntos,
                a30, conFlor);

        createRoom(session, puntos, tsr);
    }

    @Override
    public void lobbyJoined(IoSession session) {
        super.lobbyJoined(session);

        // mando, si hay, data del torneo
        if (torneo != null && torneo.getEstado() == EstadoTorneo.INCOMPLETO) {
            session.write(new InfoTorneoMessage(torneo.getFaltan(), torneo
                    .getPuntos()));
        }
    }

    // //////

    @Override
    protected TrucoServerRoom getRoom(IoSession session) {
        TrucoServerRoom tsr = (TrucoServerRoom) super.getRoom(session);
        if (tsr != null) {
            return tsr;
        }
        else if (torneo != null) {
            return torneo.getRoom(session);
        }
        else {
            return null;
        }
    }

    @Override
    public void alMazo(IoSession session) {
        getRoom(session).alMazo(session);
    }

    @Override
    public void carta(IoSession session, Carta carta) {
        getRoom(session).carta(session, carta);
    }

    @Override
    public void envido(IoSession session) {
        getRoom(session).envido(session);
    }

    @Override
    public void falta(IoSession session) {
        getRoom(session).falta(session);
    }

    @Override
    public void noquiero(IoSession sess) {
        getRoom(sess).noquiero(sess);
    }

    @Override
    public void proximaMano(IoSession session) {
        getRoom(session).proximaMano();
    }

    @Override
    public void quiero(IoSession session) {
        getRoom(session).quiero(session);
    }

    @Override
    public void real(IoSession session) {
        getRoom(session).real(session);
    }

    @Override
    public void retruco(IoSession session) {
        getRoom(session).retruco(session);
    }

    @Override
    public void truco(IoSession session) {
        getRoom(session).truco(session);
    }

    @Override
    public void vale4(IoSession session) {
        getRoom(session).vale4(session);
    }

    @Override
    public void flor(IoSession session) {
        getRoom(session).flor(session);
    }

    @Override
    public void contraflor(IoSession session) {
        getRoom(session).contraflor(session);
    }

    @Override
    public void hastaAquiLlegoElOlor(IoSession session) {
        getRoom(session).hastaAquiLlegoElOlor(session);
    }

    @Override
    public void roomJoined(IoSession session) {
        super.roomJoined(session);

        // si el player está en partida de torneo y el otro abandonó justo,
        // cuando entra le aviso que el otro abandono, asi no queda trunca la
        // partida
        if (torneo != null) {
            TrucoServerRoom tsr = torneo.getRoom(session);
            if (tsr != null) {
                IoSession otro = tsr.getOtherPlayer(session);
                if (tsr.isAbandoned(otro)) {
                    torneo.userAbandoned(otro);
                }
            }
        }
    }

    /***************************************************************************
     * TORNEOS
     **************************************************************************/
    public void chauTorneo() {
        if (torneo == null) {
            throw new IllegalArgumentException("no hay torneo en curso");
        }

        torneo.cancelar();
        torneo = null;
    }

    public synchronized void crearTorneo(int players, int puntos) {
        if (!isAcceptingNewRooms()) {
            throw new IllegalArgumentException("salas cerradas");
        }
        if (torneo != null) {
            throw new IllegalArgumentException("ya hay torneo en curso");
        }
        else if ((players & -players) != players) {
            throw new IllegalArgumentException("players no es potencia de 2");
        }
        else if (players < 4) {
            throw new IllegalArgumentException(
                    "torneo debe ser minimo de 4 jugadores");
        }
        else {
            torneo = new Torneo(this, players, puntos);

            broadcastLobby(new InfoTorneoMessage(players, puntos));
        }
    }

    public void cancelarTorneo() {
        if (torneo == null) {
            throw new IllegalArgumentException("no hay torneo en curso");
        }
        else {
            if (torneo.getEstado() == EstadoTorneo.EN_CURSO) {
                throw new IllegalArgumentException("el torneo esta en curso");
            }
            else {
                // synchronized (torneo) {
                // for (IoSession sess : enTorneo()) {
                // }
                // }
            }
        }
    }

    @Override
    /**
     * el user quiere unirse a un torneo
     */
    public void joinTorneo(IoSession session) {
        if (torneo == null) {
            session.write(new TorneoStatusMessage());
        }
        else {
            boolean agregado = torneo.agregar(session);
            if (agregado) {
                broadcastLobby(new JoinTorneoMessage(getUser(session)));

                // lo saco del lobby
                lobby.remove(session);
            }
            else {
                session.write(new TorneoStatusMessage());
            }
        }
    }

    /**
     * El user está en el lobby de torneo, lo marco como LISTO
     */
    @Override
    public synchronized void joinedTorneo(IoSession session) {
        broadcastTorneo(new JoinTorneoMessage(getUser(session)), session);

        // mando lista de partidos y usuarios
        session.write(new PartidosTorneoMessage(torneo.getRoomInfos(),
                getUsersEnTorneo(), torneo.getCampeon()));

        EstadoTorneo estado = torneo.getEstado();
        if (estado == EstadoTorneo.INCOMPLETO) {
            torneo.setPlayerReady(session);

            if (torneo.todosListos()) {
                // mando al lobby para sacar el boton
                broadcastLobby(new TorneoStatusMessage(true));

                // arranco el torneo
                torneo.start();
            }
        }
        else if (estado == EstadoTorneo.EN_CURSO) {
            if (torneo.jugando(session)) {
                // sigue jugando
                torneo.setPlayerReady(session);
            }
            else {
                // uno que perdio
                espectadoresTorneo.add(session);
            }
        }
        else { // EstadoTorneo.TERMINADO
            espectadoresTorneo.add(session);
        }
    }

    private Set<User> getUsersEnTorneo() {
        return Sets.newHashSet(Iterables.transform(enTorneo(),
                new Function<IoSession, User>() {
                    @Override
                    public User apply(IoSession s) {
                        return getUser(s);
                    }
                }));
    }

    public Set<IoSession> enTorneo() {
        Set<IoSession> enTorneo = new HashSet<IoSession>(espectadoresTorneo);
        enTorneo.addAll(torneo.getEnLobby());

        return enTorneo;
    }

    protected void broadcastTorneo(Message msg) {
        broadcastTorneo(msg, null);
    }

    protected void broadcastTorneo(Message msg, IoSession excepto) {
        for (IoSession sess : enTorneo()) {
            if (sess != excepto) {
                sess.write(msg);
            }
        }
    }

    public void abandonTorneo(IoSession session) {
        abandonTorneo(session, true);
    }

    /**
     * Saco al user del torneo y lo mando de vuelta al lobby, de corresponder
     */
    public void abandonTorneo(IoSession session, boolean backToLobby) {
        espectadoresTorneo.remove(session);
        if (backToLobby) {
            lobby.add(session);
        }

        if (torneo != null) {
            broadcastTorneo(new AbandonedTorneoMessage(getUser(session)),
                    session);

            EstadoTorneo estado = torneo.getEstado();
            if (estado == EstadoTorneo.INCOMPLETO) {
                torneo.userAbandoned(session);

                // aviso al lobby que uno se fue
                broadcastLobby(new InfoTorneoMessage(torneo.getFaltan(), torneo
                        .getPuntos()));
                broadcastTorneo(new InfoTorneoMessage(torneo.getFaltan(), 0));
            }
            else if (estado == EstadoTorneo.EN_CURSO) {
                torneo.userAbandoned(session);
            }
            else { // EstadoTorneo.TERMINADO
            }
        }
    }

    /**
     * El user se fue del juego
     */
    protected void userDisconnected(IoSession session) {
        if (torneo != null) {
            // torneo.userAbandoned(session);
            abandonTorneo(session, false);
        }
    }

    /**
     * El user abandono un partido, veo si era un partido de torneo
     */
    protected void playerLeftRoom(IoSession session) {
        if (torneo != null) {
            TwoPlayersServerRoom tsr = torneo.getRoom(session);
            if (tsr != null) {
                log.debug("Torneo - " + getUser(session)
                        + " abandono la partida");

                // lo obtengo antes porque al hacer tsr.abandon se nullifica
                IoSession ganador = tsr.getOtherPlayer(session);

                // aviso al otro que abandono
                tsr.abandon(session);

                // el otro pasa de ronda
                torneo.gano(ganador);

                if (torneo.getCampeon() != null) {
                    broadcastTorneo(new CampeonTorneoMessage(torneo
                            .getCampeon()));
                }
            }
            else if (enTorneo().contains(session)) {
                log.debug("Torneo - " + getUser(session) + "(" + session
                        + ") abandono sin TrucoServerRoom");

                torneo.userAbandoned(session);
            }
        }
    }

    /**
     * @return si s1 y s2 estan jugando en un torneo
     */
    public boolean enTorneo(IoSession s1, IoSession s2) {
        return torneo != null && torneo.jugando(s1, s2);
    }

    /**
     * <code>ganador</code> gano el partido
     * 
     * @param ganador
     */
    public synchronized void finPartidoTorneo(IoSession ganador) {
        torneo.gano(ganador);

        if (torneo.getCampeon() != null) {
            broadcastTorneo(new CampeonTorneoMessage(torneo.getCampeon()));
        }
    }

    @Override
    public void torneoChat(IoSession session, String msg) {
        User u = getUser(session);
        TorneoChatMessage chat = new TorneoChatMessage(msg);
        chat.setFrom(u);

        if (u.getFloodControl().isFlooding()) {
            // kick por flooder
            poster.kickPlayer("flooding", u.getName(), 5);
            return;
        }

        broadcastTorneo(chat, session);
    }

    //
    // notificaciones
    // 
    public void notifyNewPartidoTorneo(TrucoRoom room, int ronda) {
        broadcastTorneo(new NewPartidoTorneoMessage(room, ronda));
    }

    public void notifyUpdatePartidoTorneo(int id, int puntaje1, int puntaje2) {
        broadcastTorneo(new UpdatePartidoTorneoMessage(id, puntaje1, puntaje2));
    }

    public void torneoTerminado() {
        log.debug("Torneo - torneoTerminado");

        // en 60 segundos todos al lobby
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                log.debug("Torneo - torneoTerminado.run");

                AbandonTorneoMessage msg = new AbandonTorneoMessage();
                for (IoSession sess : espectadoresTorneo) {
                    sess.write(msg);
                }
                espectadoresTorneo.clear();
                torneo = null;
                log.debug("Torneo - torneo = null");

                broadcastLobby(new TorneoStatusMessage(false));
            }
        }, 60 * 1000);
    }

    @Override
    protected boolean handleOPMessage(IoSession sess, String msg) {
        User u = getUser(sess);

        if (!poster.isOp(u.getName())) {
            return false;
        }

        // /torneo [cupo=16] [puntos=10]
        String prefix = "/torneo ";
        if (msg.startsWith(prefix)) {
            // bla bla
            String[] params = msg.substring(prefix.length()).split(" ");

            int cupo = 16;
            if (params.length > 0) {
                try {
                    cupo = Integer.parseInt(params[0]);
                }
                catch (NumberFormatException nfe) {
                }
            }

            int puntos = 10;
            if (params.length > 1) {
                try {
                    puntos = Integer.parseInt(params[1]);
                }
                catch (NumberFormatException nfe) {
                }
            }

            try {
                crearTorneo(cupo, puntos);

                log.info(u + " creo torneo de " + cupo);

                sess
                        .write(new OPMessage("torneo de " + cupo
                                + " participantes"));
            }
            catch (IllegalArgumentException iae) {
                sess.write(new OPMessage(iae.getMessage()));
            }

            return true; // los mensajes OP nunca pasan
        }
        else if (msg.startsWith("/chautorneo")) {
            chauTorneo();
            sess.write(new OPMessage("torneo cancelado"));
            return true; // los mensajes OP nunca pasan
        }
        else {
            return super.handleOPMessage(sess, msg);
        }
    }
}