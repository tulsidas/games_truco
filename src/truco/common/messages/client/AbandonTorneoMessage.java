package truco.common.messages.client;

import org.apache.mina.common.IoSession;

import truco.common.ifaz.TorneoHandler;
import truco.common.ifaz.TrucoSaloonHandler;
import truco.common.ifaz.TorneoMessage;
import truco.common.messages.TrucoClientGameMessage;

public class AbandonTorneoMessage extends TrucoClientGameMessage implements
        TorneoMessage {

    @Override
    public void execute(IoSession session, TrucoSaloonHandler salon) {
        salon.abandonTorneo(session);
    }

    @Override
    public void execute(TorneoHandler torneo) {
        torneo.volverAlLobby();
    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public byte getMessageId() {
        return (byte) 0xB1;
    }
    
    @Override
    public String toString() {
        return "AbandonTorneo";
    }
}