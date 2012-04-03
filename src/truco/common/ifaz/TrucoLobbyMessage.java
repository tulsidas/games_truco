package truco.common.ifaz;

import common.ifaz.LobbyMessage;

public interface TrucoLobbyMessage extends LobbyMessage {

   // mensaje del server se ejecuta en el lobby del truco
   public void execute(TrucoLobbyHandler lobby);
}
