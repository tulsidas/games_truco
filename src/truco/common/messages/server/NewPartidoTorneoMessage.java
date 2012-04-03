package truco.common.messages.server;


import org.apache.mina.common.ByteBuffer;

import truco.common.ifaz.TorneoHandler;
import truco.common.ifaz.TorneoMessage;
import truco.common.model.TrucoRoom;

import common.messages.VariableLengthMessageAdapter;
import common.util.StringUtil;

public class NewPartidoTorneoMessage extends VariableLengthMessageAdapter
        implements TorneoMessage {

    private int id, ronda;

    private String p1, p2;

    public NewPartidoTorneoMessage() {
    }

    public NewPartidoTorneoMessage(TrucoRoom room, int ronda) {
        this.id = room.getId();
        this.ronda = ronda;
        this.p1 = room.getPlayers().get(0).getName();
        this.p2 = room.getPlayers().get(1).getName();
    }

    @Override
    public void execute(TorneoHandler torneo) {
        torneo.newPartidoTorneo(id, ronda, p1, p2);
    }

    @Override
    public ByteBuffer encodedContent() {
        ByteBuffer buf = ByteBuffer.allocate(32);
        buf.setAutoExpand(true);

        buf.putInt(id);
        buf.putInt(ronda);

        StringUtil.encode(buf, p1);
        StringUtil.encode(buf, p2);

        return buf.flip();
    }

    @Override
    public void decode(ByteBuffer buff) {
        id = buff.getInt();
        ronda = buff.getInt();

        p1 = StringUtil.decode(buff);
        p2 = StringUtil.decode(buff);
    }

    @Override
    public byte getMessageId() {
        return (byte) 0xB3;
    }
}
