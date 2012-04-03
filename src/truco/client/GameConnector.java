package truco.client;

import java.util.Collection;

import org.apache.mina.common.IoSession;

import common.model.User;

import truco.common.ifaz.GameHandler;
import truco.common.ifaz.GameMessage;
import truco.common.ifaz.TorneoHandler;
import truco.common.ifaz.TorneoMessage;
import truco.common.ifaz.TrucoLobbyHandler;
import truco.common.ifaz.TrucoLobbyMessage;
import truco.common.messages.TrucoProtocolDecoder;
import truco.common.model.Carta;
import truco.common.model.Mano;
import truco.common.model.RoomInfo;
import truco.common.model.TrucoRoom;
import client.AbstractGameConnector;

public class GameConnector extends AbstractGameConnector implements
        GameHandler, TorneoHandler {
    private TorneoHandler torneoHandler;

    public GameConnector(String host, int port, int salon, String user,
            String pass, long version) {
        super(host, port, salon, user, pass, version,
                new TrucoProtocolDecoder());
    }

    public void setTorneoHandler(TorneoHandler torneoHandler) {
        loginHandler = null;
        lobbyHandler = null;
        gameHandler = null;
        this.torneoHandler = torneoHandler;
    }

    @Override
    public void messageReceived(IoSession sess, Object message) {
        super.messageReceived(sess, message);

        if (message instanceof GameMessage && gameHandler != null) {
            ((GameMessage) message).execute(this);
        }
        else if (message instanceof TrucoLobbyMessage && lobbyHandler != null) {
            ((TrucoLobbyMessage) message)
                    .execute((TrucoLobbyHandler) lobbyHandler);
        }
        else if (message instanceof TorneoMessage && torneoHandler != null) {
            ((TorneoMessage) message).execute(this);
        }
    }

    // /////////////
    // GameHandler
    // /////////////
    public void startMano(Mano mano, boolean empiezo) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).startMano(mano, empiezo);
        }
    }

    public void carta(Carta c, boolean teToca) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).carta(c, teToca);
        }
    }

    @Override
    public void tuTurno() {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).tuTurno();
        }
    }

    public void finMano(int puntosYo, int puntosOtro) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).finMano(puntosYo, puntosOtro);
        }
    }

    public void finJuego(boolean victoria) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).finJuego(victoria);
        }
    }

    public void finMano(int puntosYo, int puntosOtro, Carta c) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).finMano(puntosYo, puntosOtro, c);
        }
    }

    public void alMazo(int ptos) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).alMazo(ptos);
        }
    }

    public void envido(int ptos) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).envido(ptos);
        }
    }

    public void falta(int ptos) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).falta(ptos);
        }
    }

    public void noquiero(int puntosGanados) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).noquiero(puntosGanados);
        }
    }

    public void quiero() {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).quiero();
        }
    }

    public void real(int ptos) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).real(ptos);
        }
    }

    public void retruco() {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).retruco();
        }
    }

    public void truco() {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).truco();
        }
    }

    public void vale4() {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).vale4();
        }
    }

    @Override
    public void puntosEnvido(int puntosGanados, int tantoOponente) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).puntosEnvido(puntosGanados,
                    tantoOponente);
        }
    }

    @Override
    public void puntosFlor(int tantoOponente) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).puntosFlor(tantoOponente);
        }
    }

    @Override
    public void flor() {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).flor();
        }
    }

    @Override
    public void contraflor() {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).contraflor();
        }
    }

    @Override
    public void hastaAquiLlegoElOlor() {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).hastaAquiLlegoElOlor();
        }
    }

    @Override
    public void joinRoom(TrucoRoom room) {
        if (torneoHandler != null) {
            torneoHandler.joinRoom(room);
        }
    }

    @Override
    public void ganador(User ganador) {
        if (torneoHandler != null) {
            torneoHandler.ganador(ganador);
        }
    }

    @Override
    public void incomingTorneoChat(User from, String msg) {
        if (torneoHandler != null) {
            torneoHandler.incomingTorneoChat(from, msg);
        }
    }

    @Override
    public void newPartidoTorneo(int id, int ronda, String p1, String p2) {
        if (torneoHandler != null) {
            torneoHandler.newPartidoTorneo(id, ronda, p1, p2);
        }
    }

    @Override
    public void updatePartidoTorneo(int id, int puntaje1, int puntaje2) {
        if (torneoHandler != null) {
            torneoHandler.updatePartidoTorneo(id, puntaje1, puntaje2);
        }
    }

    @Override
    public void setInfoTorneo(Collection<RoomInfo> roomInfos,
            Collection<User> users, User campeon) {
        if (torneoHandler != null) {
            torneoHandler.setInfoTorneo(roomInfos, users, campeon);
        }
    }

    @Override
    public void campeonTorneo(User campeon) {
        if (torneoHandler != null) {
            torneoHandler.campeonTorneo(campeon);
        }
    }

    @Override
    public void volverAlLobby() {
        if (torneoHandler != null) {
            torneoHandler.volverAlLobby();
        }
    }

    @Override
    public void userAbandoned(User u) {
        if (torneoHandler != null) {
            torneoHandler.userAbandoned(u);
        }
    }

    @Override
    public void userJoined(User u) {
        if (torneoHandler != null) {
            torneoHandler.userJoined(u);
        }
    }
}