package truco.common.ifaz;

import common.ifaz.LobbyHandler;
import common.model.User;

public interface TrucoLobbyHandler extends LobbyHandler {

    public void infoTorneo(int players, int puntos);

    public void torneoStatus(boolean started);

    public void joinedTorneo(User user);
}