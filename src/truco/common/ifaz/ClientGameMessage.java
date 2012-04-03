package truco.common.ifaz;

import org.apache.mina.common.IoSession;

import common.ifaz.BasicClientGameMessage;

public interface ClientGameMessage extends BasicClientGameMessage {
    // mensaje del cliente se ejecuta en el server
    public void execute(IoSession session, TrucoSaloonHandler salon);
}
