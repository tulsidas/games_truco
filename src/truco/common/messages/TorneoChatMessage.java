package truco.common.messages;

import org.apache.mina.common.IoSession;

import truco.common.ifaz.ClientGameMessage;
import truco.common.ifaz.TrucoSaloonHandler;
import truco.common.ifaz.TorneoHandler;
import truco.common.ifaz.TorneoMessage;

import common.ifaz.BasicServerHandler;
import common.messages.VariableLengthMessage;
import common.messages.chat.AbstractChatMessage;

/**
 * Mensaje de chat en el lobby
 */
public class TorneoChatMessage extends AbstractChatMessage implements
        VariableLengthMessage, TorneoMessage, ClientGameMessage {

    public TorneoChatMessage() {
    }

    public TorneoChatMessage(String msg) {
        super(msg);
    }

    public void execute(IoSession session, BasicServerHandler salon) {
        execute(session, (TrucoSaloonHandler) salon);
    }

    @Override
    public void execute(IoSession session, TrucoSaloonHandler salon) {
        salon.torneoChat(session, getMsg());
    }

    public void execute(TorneoHandler torneo) {
        torneo.incomingTorneoChat(getFrom(), getMsg());
    }

    @Override
    public String toString() {
        return "TorneoChat: " + msg;
    }

    @Override
    public byte getMessageId() {
        return (byte) 0xB2;
    }

}