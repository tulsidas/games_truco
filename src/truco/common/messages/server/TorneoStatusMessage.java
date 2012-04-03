package truco.common.messages.server;

import org.apache.mina.common.ByteBuffer;

import truco.common.ifaz.TrucoLobbyHandler;
import truco.common.ifaz.TrucoLobbyMessage;

import common.ifaz.LobbyHandler;
import common.messages.FixedLengthMessageAdapter;
import common.messages.TaringaProtocolEncoder;

public class TorneoStatusMessage extends FixedLengthMessageAdapter implements
        TrucoLobbyMessage {

    boolean started;

    public TorneoStatusMessage() {
    }

    public TorneoStatusMessage(boolean started) {
        this.started = started;
    }

    @Override
    public int getContentLength() {
        return 1;
    }

    @Override
    public byte getMessageId() {
        return (byte) 0xAD;
    }

    @Override
    public void execute(TrucoLobbyHandler lobby) {
        lobby.torneoStatus(started);
    }

    @Override
    public void execute(LobbyHandler lobby) {
    }

    @Override
    public void decode(ByteBuffer buff) {
        started = buff.get() == TaringaProtocolEncoder.TRUE;
    }

    @Override
    protected void encodeContent(ByteBuffer buff) {
        buff.put(started ? TaringaProtocolEncoder.TRUE
                : TaringaProtocolEncoder.FALSE);
    }
}
