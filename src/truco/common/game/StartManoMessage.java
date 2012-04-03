package truco.common.game;

import org.apache.mina.common.ByteBuffer;

import truco.common.ifaz.GameHandler;
import truco.common.ifaz.GameMessage;
import truco.common.model.Mano;

import common.messages.FixedLengthMessageAdapter;
import common.messages.TaringaProtocolEncoder;

/**
 * Mensaje del server que da comienzo al juego
 */
public class StartManoMessage extends FixedLengthMessageAdapter implements
        GameMessage {

    private boolean empiezo;

    private Mano mano;

    public StartManoMessage() {
    }

    public StartManoMessage(Mano mano, boolean empiezo) {
        super();
        this.mano = mano;
        this.empiezo = empiezo;
    }

    public void execute(GameHandler game) {
        game.startMano(mano, empiezo);
    }

    @Override
    public String toString() {
        return "Start Mano (" + mano + ", " + empiezo + ")";
    }

    @Override
    protected void encodeContent(ByteBuffer buff) {
        Mano.writeTo(mano, buff);
        buff.put(empiezo ? TaringaProtocolEncoder.TRUE
                : TaringaProtocolEncoder.FALSE);
    }

    @Override
    public void decode(ByteBuffer buff) {
        mano = Mano.readFrom(buff);
        empiezo = buff.get() == TaringaProtocolEncoder.TRUE;
    }

    @Override
    public byte getMessageId() {
        return (byte) 0x93;
    }

    @Override
    public int getContentLength() {
        // 3 bytes por Carta + empiezo
        return 10;
    }
}
