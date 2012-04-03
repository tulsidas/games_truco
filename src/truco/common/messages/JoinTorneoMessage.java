package truco.common.messages;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

import truco.common.ifaz.ClientGameMessage;
import truco.common.ifaz.TorneoHandler;
import truco.common.ifaz.TorneoMessage;
import truco.common.ifaz.TrucoSaloonHandler;
import truco.common.ifaz.TrucoLobbyHandler;
import truco.common.ifaz.TrucoLobbyMessage;

import common.ifaz.BasicServerHandler;
import common.ifaz.LobbyHandler;
import common.messages.VariableLengthMessageAdapter;
import common.model.User;

public class JoinTorneoMessage extends VariableLengthMessageAdapter implements
        TrucoLobbyMessage, ClientGameMessage, TorneoMessage {
    private User user;

    public JoinTorneoMessage() {
    }

    public JoinTorneoMessage(User user) {
        this.user = user;
    }

    @Override
    public void execute(IoSession session, TrucoSaloonHandler salon) {
        salon.joinTorneo(session);
    }

    @Override
    public void execute(IoSession session, BasicServerHandler salon) {
        execute(session, (TrucoSaloonHandler) salon);
    }

    @Override
    public void execute(TorneoHandler torneo) {
        torneo.userJoined(user);
    }

    @Override
    public byte getMessageId() {
        return (byte) 0xAC;
    }

    @Override
    public void execute(TrucoLobbyHandler lobby) {
        lobby.joinedTorneo(user);
    }

    @Override
    public void execute(LobbyHandler lobby) {
    }

    @Override
    public ByteBuffer encodedContent() {
        ByteBuffer ret = ByteBuffer.allocate(32);
        ret.setAutoExpand(true);

        User.writeTo(user, ret);

        ret.flip();

        return ret;
    }

    @Override
    public void decode(ByteBuffer buff) {
        user = User.readFrom(buff);
    }

    @Override
    public String toString() {
        return "joinTorneo";
    }
}
