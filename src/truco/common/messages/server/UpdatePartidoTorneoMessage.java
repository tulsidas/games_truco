package truco.common.messages.server;

import org.apache.mina.common.ByteBuffer;

import truco.common.ifaz.TorneoHandler;
import truco.common.ifaz.TorneoMessage;

import common.messages.FixedLengthMessageAdapter;

public class UpdatePartidoTorneoMessage extends FixedLengthMessageAdapter
        implements TorneoMessage {

    private int id;

    private byte puntaje1, puntaje2;

    public UpdatePartidoTorneoMessage() {
    }

    public UpdatePartidoTorneoMessage(int id, int puntaje1, int puntaje2) {
        this.id = id;
        this.puntaje1 = (byte) puntaje1;
        this.puntaje2 = (byte) puntaje2;
    }

    public byte getMessageId() {
        return (byte) 0xB4;
    }

    @Override
    public int getContentLength() {
        return 6;
    }

    @Override
    protected void encodeContent(ByteBuffer buff) {
        buff.putInt(id);
        buff.put(puntaje1);
        buff.put(puntaje2);
    }

    @Override
    public void decode(ByteBuffer buff) {
        id = buff.getInt();
        puntaje1 = buff.get();
        puntaje2 = buff.get();
    }

    @Override
    public void execute(TorneoHandler torneo) {
        torneo.updatePartidoTorneo(id, puntaje1, puntaje2);
    }
}
