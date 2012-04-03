package truco.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.TwoPlayersServerRoom;
import truco.common.game.AlMazoMessage;
import truco.common.game.CartaMessage;
import truco.common.game.ContraFlorMessage;
import truco.common.game.EnvidoMessage;
import truco.common.game.FaltaEnvidoMessage;
import truco.common.game.FinJuegoMessage;
import truco.common.game.FinManoMessage;
import truco.common.game.FlorMessage;
import truco.common.game.HastaAquiLlegoElOlorMessage;
import truco.common.game.NoQuieroMessage;
import truco.common.game.PuntosEnvidoMessage;
import truco.common.game.PuntosFlorMessage;
import truco.common.game.QuieroMessage;
import truco.common.game.RealEnvidoMessage;
import truco.common.game.RetrucoMessage;
import truco.common.game.StartManoMessage;
import truco.common.game.TrucoMessage;
import truco.common.game.Vale4Message;
import truco.common.messages.server.TuTurnoMessage;
import truco.common.model.Carta;
import truco.common.model.Ipa;
import truco.common.model.Mano;
import truco.common.model.Mazo;
import truco.common.model.TrucoRoom;

import common.messages.server.UpdatedPointsMessage;
import common.model.AbstractRoom;
import common.model.User;

public class TrucoServerRoom extends TwoPlayersServerRoom {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Mazo mazo;

    private Mano mano1, mano2;

    private int puntaje1, puntaje2;

    private Carta carta1, carta2;

    /**
     * >0 gano 1 ==0 parda <0 gano 2
     */
    private int rondas[];

    private int rondaActual;

    // quien es mano
    private IoSession mano;

    // si el quiero/noquiero es para el envido o el truco
    private boolean envidoEnJuego, florEnJuego, contraFlorEnJuego;

    private int puntosTruco;

    // los tantos que se van cantando
    private List<Ipa> ipas;

    private boolean conFlor;

    // a cuanto se juega, 15 o 30
    private int puntajeJuego;

    // los nombres, para el fixture de torneo
    private String user1, user2;

    /**
     * @param player
     *            el jugador creador
     * @param puntos
     *            los puntos apostados
     */
    public TrucoServerRoom(TrucoSaloon salon, IoSession player, int puntos,
            boolean a30, boolean conFlor) {
        super(salon, player, puntos);

        this.mazo = new Mazo();
        this.ipas = new ArrayList<Ipa>(3);
        this.puntajeJuego = a30 ? 30 : 15;
        this.conFlor = conFlor;

        rondas = new int[3];

        proxMano();
    }

    public void proxMano() {
        puntosTruco = 1;
        envidoEnJuego = false;
        florEnJuego = false;
        contraFlorEnJuego = false;
        ipas.clear();
        mazo.reset();

        rondaActual = 0;
        carta1 = null;
        carta2 = null;

        if (mano == player1) {
            mano = player2;
        }
        else {
            mano = player1;
        }
    }

    @Override
    public boolean isGameOn() {
        return puntaje1 + puntaje2 > 2;
    }

    public void startMano() {
        // jugando
        setEnJuego(true);

        // mando las cartas
        mano1 = mazo.getMano();
        mano2 = mazo.getMano();

        player1.write(new StartManoMessage(mano1, mano == player1));
        player2.write(new StartManoMessage(mano2, mano == player2));
    }

    public void startNuevoJuego() {
        // reseteo
        puntaje1 = puntaje2 = 0;
        carta1 = carta2 = null;

        // reseteo la mano
        proxMano();

        // envio las cartas
        startMano();
    }

    public void truco(IoSession sess) {
        envidoEnJuego = false;
        florEnJuego = false;
        contraFlorEnJuego = false;
        multicast(new TrucoMessage(), sess);
    }

    public void retruco(IoSession sess) {
        if (puntosTruco < 2) {
            // estoy retrucando obviando el quiero
            puntosTruco = 2;
        }
        multicast(new RetrucoMessage(), sess);
    }

    public void vale4(IoSession sess) {
        if (puntosTruco < 3) {
            // estoy cantando vale4 obviando el quiero
            puntosTruco = 3;
        }
        multicast(new Vale4Message(), sess);
    }

    public void quiero(IoSession sess) {
        if (envidoEnJuego) {
            int puntosFalta = puntajeJuego - Math.max(puntaje1, puntaje2);
            int puntosEnvido = Ipa.querido(ipas, puntosFalta);

            int tanto1 = mano1.getValorEnvido();
            int tanto2 = mano2.getValorEnvido();

            if (tanto1 > tanto2 || (tanto1 == tanto2 && mano == player1)) {
                puntaje1 += puntosEnvido;
            }
            else {
                puntaje2 += puntosEnvido;
            }

            player1.write(new PuntosEnvidoMessage(puntosEnvido, tanto2));
            player2.write(new PuntosEnvidoMessage(puntosEnvido, tanto1));

            ipas.clear();

            checkFinDeJuego();
        }
        else if (contraFlorEnJuego) {
            // flor-flor
            int tanto1 = mano1.getValorFlor();
            int tanto2 = mano2.getValorFlor();

            // game over man!

            if (tanto1 > tanto2 || (tanto1 == tanto2 && mano == player1)) {
                puntaje1 = puntajeJuego;
            }
            else {
                puntaje2 = puntajeJuego;
            }

            player1.write(new PuntosFlorMessage(tanto2));
            player2.write(new PuntosFlorMessage(tanto1));

            checkFinDeJuego();
        }
        else {
            // truco en juego
            puntosTruco++;
            multicast(new QuieroMessage(), sess);
        }
    }

    public void noquiero(IoSession sess) {
        if (envidoEnJuego) {
            int puntos = Ipa.noQuerido(ipas);

            if (sess == player1) {
                puntaje2 += puntos;
            }
            else {
                puntaje1 += puntos;
            }

            ipas.clear();

            multicast(new NoQuieroMessage(puntos), sess);
        }
        else if (contraFlorEnJuego) {
            if (sess == player1) {
                puntaje2 += 6;
            }
            else {
                puntaje1 += 6;
            }

            contraFlorEnJuego = false;

            multicast(new NoQuieroMessage(6), sess);
        }
        else {
            if (sess == player1) {
                puntaje2 += puntosTruco;
                player1.write(new FinManoMessage(0, puntosTruco));
                player2.write(new NoQuieroMessage(puntosTruco));
            }
            else {
                puntaje1 += puntosTruco;
                player1.write(new NoQuieroMessage(puntosTruco));
                player2.write(new FinManoMessage(0, puntosTruco));
            }

            proxMano();
        }

        checkFinDeJuego();
    }

    public void envido(IoSession sess) {
        ipas.add(Ipa.ENVIDO);
        envidoEnJuego = true;
        int puntos = Ipa.noQuerido(ipas);
        multicast(new EnvidoMessage(puntos), sess);
    }

    public void real(IoSession sess) {
        ipas.add(Ipa.REAL_ENVIDO);
        envidoEnJuego = true;
        int puntos = Ipa.noQuerido(ipas);
        multicast(new RealEnvidoMessage(puntos), sess);
    }

    public void falta(IoSession sess) {
        ipas.add(Ipa.FALTA_ENVIDO);
        envidoEnJuego = true;
        int puntos = Ipa.noQuerido(ipas);
        multicast(new FaltaEnvidoMessage(puntos), sess);
    }

    public void flor(IoSession session) {
        Mano mano = session == player1 ? mano1 : mano2;
        if (!mano.tieneFlor()) {
            // HACK!
            User user = saloon.getUser(session);
            if (user != null) {
                log.warn(user + " CANTO FLOR Y NO TIENE+++");
            }

            // chau por cheat
            saloon.abandonGame(session);
            return;
        }

        envidoEnJuego = false;
        if (!florEnJuego) {
            florEnJuego = true;
            multicast(new FlorMessage(), session);
        }
        else {
            // flor-flor
            int tanto1 = mano1.getValorFlor();
            int tanto2 = mano2.getValorFlor();

            if (tanto1 > tanto2 || (tanto1 == tanto2 && mano == player1)) {
                puntaje1 += 6;
            }
            else {
                puntaje2 += 6;
            }

            player1.write(new PuntosFlorMessage(tanto2));
            player2.write(new PuntosFlorMessage(tanto1));

            checkFinDeJuego();
        }
    }

    public void contraflor(IoSession session) {
        // FIXME DRY flor()
        Mano mano = session == player1 ? mano1 : mano2;
        if (!mano.tieneFlor()) {
            // HACK!
            User user = saloon.getUser(session);
            if (user != null) {
                log.warn(user + " CANTO FLOR Y NO TIENE+++");
            }

            // chau por cheat
            saloon.abandonGame(session);
            return;
        }

        florEnJuego = false;
        contraFlorEnJuego = true;
        multicast(new ContraFlorMessage(), session);
    }

    public void hastaAquiLlegoElOlor(IoSession session) {
        florEnJuego = false;

        if (session == player1) {
            puntaje2 += 3;
        }
        else {
            puntaje1 += 3;
        }

        multicast(new HastaAquiLlegoElOlorMessage(), session);

        checkFinDeJuego();
    }

    public void alMazo(IoSession sess) {
        // puntos del truco + si habia un envido en juego, es como un no
        // querido
        int puntos = puntosTruco;

        // habia flor en juego?
        if (florEnJuego) {
            puntos += 3;
        }
        else if (contraFlorEnJuego) {
            puntos += 6;
        }
        else {
            puntos += Ipa.noQuerido(ipas);
        }

        if (sess == player1) {
            // si primera ronda y soy mano y no tire y no se canto tanto ni
            // truco
            // ni flor y me voy al mazo, 2 puntos
            if (rondaActual == 0 && mano == sess && carta1 == null
                    && !envidoEnJuego && !florEnJuego && puntos == 1) {
                // FIXME flor - queolor - almazo suma 2 y tiene que ser 1 solo
                puntos = 2;
            }

            puntaje2 += puntos;

            player1.write(new FinManoMessage(0, puntos));
            player2.write(new AlMazoMessage(puntos));
        }
        else {
            // si primera ronda y soy mano y no tire y no se canto tanto ni
            // truco
            // ni flor y me voy al mazo, 2 puntos
            if (rondaActual == 0 && mano == sess && carta2 == null
                    && !envidoEnJuego && !florEnJuego && puntos == 1) {
                puntos = 2;
            }

            puntaje1 += puntos;

            player1.write(new AlMazoMessage(puntos));
            player2.write(new FinManoMessage(0, puntos));
        }

        checkFinDeJuego();
        proxMano();
    }

    /**
     * @param session
     * @param carta
     */
    public void carta(IoSession session, Carta carta) {
        // chequear carta valida
        Mano cartas = session == player1 ? mano1 : mano2;
        if (!cartas.tieneCarta(carta)) {
            // HACK!
            User user = saloon.getUser(session);
            if (user != null) {
                log.warn(user + " TIRO " + carta + " Y NO LA TIENE+++");
            }

            // chau por cheat
            saloon.abandonGame(session);
            return;
        }

        if (session == player1) {
            carta1 = carta;
        }
        else {
            carta2 = carta;
        }

        IoSession otro = getOtherPlayer(session);
        // mando carta, le toca si aun falta terminar esta ronda
        otro.write(new CartaMessage(carta, carta1 == null || carta2 == null));

        if (carta1 != null && carta2 != null) {
            // termino la ronda
            rondas[rondaActual] = carta1.compareTo(carta2);

            carta1 = null;
            carta2 = null;

            if (rondaActual == 1 && rondas[0] == 0 && rondas[1] != 0) {
                // si estamos en 2da y fue parda primera, define excepto que sea
                // parda
                if (rondas[1] > 0) {
                    // player1 gano la mano
                    puntaje1 += puntosTruco;
                    finRonda(player1, player2/* , carta, session */);
                }
                else {
                    // player2 gano la mano
                    puntaje2 += puntosTruco;
                    finRonda(player2, player1/* , carta, session */);
                }

                // proxima mano
                proxMano();

                return;
            }
            else if (rondas[rondaActual] == 0 && rondaActual > 0
                    && !(rondaActual == 1 && rondas[0] == 0 & rondas[1] == 0)) {
                // parda 2da o 3ra (y no parda 1da y 2da), define primera
                if (rondas[0] > 0 || rondas[0] == 0 && mano == player1) {
                    // player1 gano la mano
                    puntaje1 += puntosTruco;
                    finRonda(player1, player2/* , carta, session */);
                }
                else {
                    // player2 gano la mano
                    puntaje2 += puntosTruco;
                    finRonda(player2, player1/* , carta, session */);
                }

                // proxima mano
                proxMano();
                return;
            }
            else if (rondaActual == 1 && rondas[0] > 0 && rondas[1] > 0) {
                // player1 gano primera y segunda
                puntaje1 += puntosTruco;
                finRonda(player1, player2/* , carta, session */);

                // proxima mano
                proxMano();
                return;
            }
            else if (rondaActual == 1 && rondas[0] < 0 && rondas[1] < 0) {
                // player2 gano primera y segunda
                puntaje2 += puntosTruco;
                finRonda(player2, player1/* , carta, session */);

                // proxima mano
                proxMano();
                return;
            }
            else if (rondaActual == 2) {
                // fin ultima ronda
                int r1 = 0;
                int r2 = 0;
                for (int i = 0; i < rondas.length; i++) {
                    if (rondas[i] > 0) {
                        r1++; // gano player1
                    }
                    else if (rondas[i] < 0) {
                        r2++; // gano player2
                    }
                }

                if (r1 > r2 || r1 == r2 && mano == player1) {
                    // player1 gano la mano
                    puntaje1 += puntosTruco;
                    finRonda(player1, player2/* , carta, session */);
                }
                else {
                    // player2 gano la mano
                    puntaje2 += puntosTruco;
                    finRonda(player2, player1/* , carta, session */);
                }

                // proxima mano
                proxMano();

                return;
            }
            else {
                // dar el turno al que gano la ronda, o al mano si hubo empate

                if (rondas[rondaActual] < 0) {
                    player2.write(new TuTurnoMessage());
                }
                else if (rondas[rondaActual] > 0) {
                    player1.write(new TuTurnoMessage());
                }
                else if (rondas[rondaActual] == 0) {
                    mano.write(new TuTurnoMessage());
                }

                rondaActual++;
            }
        }
    }

    private void finRonda(IoSession ganador, IoSession perdedor) {
        ganador.write(new FinManoMessage(puntosTruco, 0));
        perdedor.write(new FinManoMessage(0, puntosTruco));
        checkFinDeJuego();
    }

    private boolean enTorneo(IoSession s1, IoSession s2) {
        return ((TrucoSaloon) saloon).enTorneo(s1, s2);
    }

    /**
     * chequea si termino el juego y manda mensajes
     * 
     * @return si alguno de los players gano el juego
     */
    private void checkFinDeJuego() {
        if (isEnJuego()) { // si ya terminó el partido no transfiero 2 veces
            if (enTorneo(player1, player2)) {
                // actualizo pizarra
                ((TrucoSaloon) saloon).notifyUpdatePartidoTorneo(getId(),
                        puntaje1, puntaje2);

                if (puntaje1 >= puntajeJuego || puntaje2 >= puntajeJuego) {
                    setEnJuego(false);

                    // game over!
                    IoSession ganador = puntaje1 >= puntajeJuego ? player1
                            : player2;
                    IoSession perdedor = getOtherPlayer(ganador);

                    ganador.write(new FinJuegoMessage(true));
                    perdedor.write(new FinJuegoMessage(false));

                    ((TrucoSaloon) saloon).finPartidoTorneo(ganador);
                }
            }
            else {
                if (puntaje1 >= puntajeJuego) {
                    setEnJuego(false);

                    player1.write(new FinJuegoMessage(true));
                    player2.write(new FinJuegoMessage(false));

                    // transfiero puntos
                    int newPoints[] = saloon.transferPoints(player1, player2,
                            puntosApostados);

                    // mando puntos (si siguen conectados)
                    if (player1 != null) {
                        player1.write(new UpdatedPointsMessage(newPoints[0]));
                    }
                    if (player2 != null) {
                        player2.write(new UpdatedPointsMessage(newPoints[1]));
                    }
                }
                else if (puntaje2 >= puntajeJuego) {
                    setEnJuego(false);

                    player1.write(new FinJuegoMessage(false));
                    player2.write(new FinJuegoMessage(true));

                    // transfiero puntos
                    int newPoints[] = saloon.transferPoints(player2, player1,
                            puntosApostados);

                    // mando puntos (si siguen conectados)
                    if (player1 != null) {
                        player1.write(new UpdatedPointsMessage(newPoints[1]));
                    }

                    if (player2 != null) {
                        player2.write(new UpdatedPointsMessage(newPoints[0]));
                    }
                }
            }
        }
    }

    @Override
    public synchronized void abandon(IoSession session) {
        if (enTorneo(player1, player2)) {
            if (session == player1) {
                puntaje1 = -1;
            }
            else if (session == player2) {
                puntaje2 = -1;
            }

            // actualizo pizarra
            ((TrucoSaloon) saloon).notifyUpdatePartidoTorneo(getId(), puntaje1,
                    puntaje2);

            // dentro de torneo no tengo que transferir puntos por abandono, ya
            // fueron sacados al empezar
            setEnJuego(false);
        }

        super.abandon(session);
    }

    public void proximaMano() {
        startMano();
    }

    @Override
    public AbstractRoom createRoom() {
        return new TrucoRoom(getId(), puntosApostados, puntajeJuego, conFlor,
                getUsers());
    }

    @Override
    public void startGame() {
        setStarted(true);
        startMano();
    }

    public int getPuntaje1() {
        return puntaje1;
    }

    public int getPuntaje2() {
        return puntaje2;
    }

    public IoSession getPlayer1() {
        return player1;
    }

    public IoSession getPlayer2() {
        return player2;
    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }
}