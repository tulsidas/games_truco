package truco.common.messages;

import org.apache.mina.common.ByteBuffer;

import truco.common.ifaz.TorneoHandler;
import truco.common.ifaz.TorneoMessage;

import common.messages.VariableLengthMessageAdapter;
import common.model.User;

public class AbandonedTorneoMessage extends VariableLengthMessageAdapter
        implements TorneoMessage {
    private User user;

    public AbandonedTorneoMessage() {
    }

    public AbandonedTorneoMessage(User user) {
        this.user = user;
    }

    @Override
    public void execute(TorneoHandler torneo) {
        torneo.userAbandoned(user);
    }

    @Override
    public byte getMessageId() {
        return (byte) 0xB7;
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
        return user + " abandoned torneo";
    }
}
