package truco.common.messages.server;

import org.apache.mina.common.ByteBuffer;

import truco.common.ifaz.TorneoHandler;
import truco.common.ifaz.TorneoMessage;

import common.messages.VariableLengthMessageAdapter;
import common.model.User;

public class GanadorTorneoMessage extends VariableLengthMessageAdapter
        implements TorneoMessage {

    private User ganador;

    public GanadorTorneoMessage() {
    }

    public GanadorTorneoMessage(User ganador) {
        this.ganador = ganador;
    }

    @Override
    public String toString() {
        return "Gandor Torneo -> " + ganador;
    }

    @Override
    public void execute(TorneoHandler torneo) {
        torneo.ganador(ganador);
    }

    @Override
    public ByteBuffer encodedContent() {
        ByteBuffer ret = ByteBuffer.allocate(32);
        ret.setAutoExpand(true);

        User.writeTo(ganador, ret);

        return ret.flip();
    }

    @Override
    public void decode(ByteBuffer buff) {
        ganador = User.readFrom(buff);
    }

    @Override
    public byte getMessageId() {
        return (byte) 0xB0;
    }
}