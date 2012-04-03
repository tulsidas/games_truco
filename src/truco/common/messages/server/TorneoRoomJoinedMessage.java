package truco.common.messages.server;

import org.apache.mina.common.ByteBuffer;

import truco.common.ifaz.TorneoHandler;
import truco.common.ifaz.TorneoMessage;
import truco.common.model.TrucoRoom;

import common.messages.TaringaProtocolEncoder;
import common.messages.VariableLengthMessageAdapter;
import common.model.AbstractRoom;

public class TorneoRoomJoinedMessage extends VariableLengthMessageAdapter
      implements TorneoMessage {

   private TrucoRoom room;

   public TorneoRoomJoinedMessage() {
   }

   public TorneoRoomJoinedMessage(TrucoRoom room) {
      this.room = room;
   }

   @Override
   public String toString() {
      return "Torneo Room Joined: " + room;
   }

   @Override
   public void execute(TorneoHandler torneo) {
      torneo.joinRoom(room);
   }

   @Override
   public ByteBuffer encodedContent() {
      ByteBuffer ret = ByteBuffer.allocate(32);
      ret.setAutoExpand(true);

      if (room != null) {
         ret.put(TaringaProtocolEncoder.NON_NULL);
         ret.put(room.encode());
      }
      else {
         ret.put(TaringaProtocolEncoder.NULL);
      }

      return ret.flip();
   }

   @Override
   public void decode(ByteBuffer buff) {
      if (buff.get() == TaringaProtocolEncoder.NON_NULL) {
         room = (TrucoRoom) AbstractRoom.decodeRoom(buff);
      }
   }

   @Override
   public byte getMessageId() {
      return (byte) 0xAF;
   }
}
