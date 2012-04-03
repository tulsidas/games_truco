package truco.common.messages;

import org.apache.mina.common.IoSession;

import truco.common.ifaz.ClientGameMessage;
import truco.common.ifaz.TrucoSaloonHandler;

import common.ifaz.BasicServerHandler;
import common.messages.FixedLengthMessageAdapter;

public abstract class TrucoClientGameMessage extends FixedLengthMessageAdapter
        implements ClientGameMessage {
    public abstract void execute(IoSession session, TrucoSaloonHandler salon);

    public void execute(IoSession session, BasicServerHandler serverHandler) {
        execute(session, (TrucoSaloonHandler) serverHandler);
    }
}