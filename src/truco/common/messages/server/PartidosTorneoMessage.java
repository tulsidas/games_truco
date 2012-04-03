package truco.common.messages.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.mina.common.ByteBuffer;

import truco.common.ifaz.TorneoHandler;
import truco.common.ifaz.TorneoMessage;
import truco.common.model.RoomInfo;

import common.messages.TaringaProtocolEncoder;
import common.messages.VariableLengthMessageAdapter;
import common.model.User;

public class PartidosTorneoMessage extends VariableLengthMessageAdapter
        implements TorneoMessage {

    private Collection<RoomInfo> roomInfos;

    private Set<User> users;

    private User campeon;

    public PartidosTorneoMessage() {
    }

    public PartidosTorneoMessage(Collection<RoomInfo> roomInfos,
            Set<User> users, User campeon) {
        this.roomInfos = roomInfos;
        this.users = users;
        this.campeon = campeon;
    }

    @Override
    public ByteBuffer encodedContent() {
        ByteBuffer buf = ByteBuffer.allocate(128);
        buf.setAutoExpand(true);

        buf.putInt(roomInfos.size());
        for (RoomInfo ri : roomInfos) {
            RoomInfo.writeTo(ri, buf);
        }

        buf.putInt(users.size());
        for (User u : users) {
            User.writeTo(u, buf);
        }

        if (campeon == null) {
            buf.put(TaringaProtocolEncoder.NULL);
        }
        else {
            buf.put(TaringaProtocolEncoder.NON_NULL);
            User.writeTo(campeon, buf);
        }

        return buf.flip();
    }

    @Override
    public void decode(ByteBuffer buff) {
        int size = buff.getInt();

        roomInfos = new HashSet<RoomInfo>(size);
        for (int i = 0; i < size; i++) {
            roomInfos.add(RoomInfo.readFrom(buff));
        }

        size = buff.getInt();
        users = new HashSet<User>(size);
        for (int i = 0; i < size; i++) {
            users.add(User.readFrom(buff));
        }

        if (buff.get() == TaringaProtocolEncoder.NON_NULL) {
            campeon = User.readFrom(buff);
        }
    }

    @Override
    public void execute(TorneoHandler torneo) {
        torneo.setInfoTorneo(roomInfos, users, campeon);
    }

    @Override
    public byte getMessageId() {
        return (byte) 0xB5;
    }
}
