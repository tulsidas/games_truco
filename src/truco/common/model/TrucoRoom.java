package truco.common.model;

import java.util.List;

import org.apache.mina.common.ByteBuffer;

import common.messages.TaringaProtocolEncoder;
import common.model.TwoPlayerRoom;
import common.model.User;

public class TrucoRoom extends TwoPlayerRoom {
    private int puntajeJuego;

    private boolean conFlor;

    public TrucoRoom() {
    }

    public TrucoRoom(int id, int puntosApostados, int puntajeJuego,
            boolean conFlor, List<User> players) {
        super(id, puntosApostados, players);
        this.puntajeJuego = puntajeJuego;
        this.conFlor = conFlor;
    }

    public int getPuntajeJuego() {
        return puntajeJuego;
    }

    public boolean isConFlor() {
        return conFlor;
    }

    @Override
    public ByteBuffer encode() {
        ByteBuffer parent = super.encode();

        ByteBuffer ret = ByteBuffer.allocate(parent.remaining() + 2);
        ret.put(parent);
        ret.put((byte) puntajeJuego);
        ret.put(conFlor ? TaringaProtocolEncoder.TRUE
                : TaringaProtocolEncoder.FALSE);

        return ret.flip();
    }

    @Override
    public void decode(ByteBuffer buff) {
        super.decode(buff);

        puntajeJuego = buff.get();
        conFlor = buff.get() == TaringaProtocolEncoder.TRUE;
    }

    @Override
    protected String getRoomInfo() {
        String ret = "";
        if (getPlayers().size() < 2) {
            ret = ", a " + puntajeJuego + ", " + (conFlor ? "con" : "sin");
        }
        return ret + ")";
    }
}