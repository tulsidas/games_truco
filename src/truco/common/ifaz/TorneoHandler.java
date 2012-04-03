package truco.common.ifaz;

import java.util.Collection;

import truco.common.model.RoomInfo;
import truco.common.model.TrucoRoom;

import common.model.User;

public interface TorneoHandler {
    void joinRoom(TrucoRoom room);

    void ganador(User ganador);

    void incomingTorneoChat(User from, String msg);

    void newPartidoTorneo(int id, int ronda, String p1, String p2);

    /**
     * si puntaje < 0 => el user abandono
     */
    void updatePartidoTorneo(int id, int puntaje1, int puntaje2);

    void setInfoTorneo(Collection<RoomInfo> roomInfos, Collection<User> users,
            User campeon);

    void campeonTorneo(User campeon);

    /** cuando termina el torneo los mando a todos de vuelta al lobby */
    void volverAlLobby();

    /** notificacion de nuevo user */
    void userJoined(User u);

    void userAbandoned(User u);
}