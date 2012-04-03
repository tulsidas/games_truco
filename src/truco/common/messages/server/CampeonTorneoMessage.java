package truco.common.messages.server;

import org.apache.mina.common.ByteBuffer;

import truco.common.ifaz.TorneoHandler;
import truco.common.ifaz.TorneoMessage;

import common.messages.VariableLengthMessageAdapter;
import common.model.User;

public class CampeonTorneoMessage extends VariableLengthMessageAdapter
        implements TorneoMessage {

    private User campeon;

    public CampeonTorneoMessage() {
    }

    public CampeonTorneoMessage(User campeon) {
        this.campeon = campeon;
    }

    @Override
    public void execute(TorneoHandler torneo) {
        torneo.campeonTorneo(campeon);
    }

    @Override
    public ByteBuffer encodedContent() {
        ByteBuffer buf = ByteBuffer.allocate(32);
        buf.setAutoExpand(true);

        User.writeTo(campeon, buf);

        return buf.flip();
    }

    @Override
    public void decode(ByteBuffer buff) {
        campeon = User.readFrom(buff);
    }

    @Override
    public byte getMessageId() {
        return (byte) 0xB6;
    }
}
