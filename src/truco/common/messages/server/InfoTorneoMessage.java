package truco.common.messages.server;

import org.apache.mina.common.ByteBuffer;

import truco.common.ifaz.TrucoLobbyHandler;
import truco.common.ifaz.TrucoLobbyMessage;

import common.ifaz.LobbyHandler;
import common.messages.FixedLengthMessageAdapter;

public class InfoTorneoMessage extends FixedLengthMessageAdapter implements
        TrucoLobbyMessage {

    private int puntos;

    private int faltan;

    public InfoTorneoMessage() {
    }

    public InfoTorneoMessage(int faltan, int puntos) {
        this.faltan = faltan;
        this.puntos = puntos;
    }

    @Override
    public void execute(TrucoLobbyHandler lobby) {
        lobby.infoTorneo(faltan, puntos);
    }

    @Override
    public void execute(LobbyHandler lobby) {
    }

    @Override
    public int getContentLength() {
        return 8;
    }

    @Override
    public void decode(ByteBuffer buff) {
        faltan = buff.getInt();
        puntos = buff.getInt();
    }

    @Override
    protected void encodeContent(ByteBuffer buff) {
        buff.putInt(faltan);
        buff.putInt(puntos);
    }

    @Override
    public byte getMessageId() {
        return (byte) 0xAB;
    }

}
