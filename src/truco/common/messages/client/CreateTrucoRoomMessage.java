package truco.common.messages.client;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

import truco.common.ifaz.TrucoServerHandler;

import common.ifaz.BasicClientGameMessage;
import common.ifaz.BasicServerHandler;
import common.messages.TaringaProtocolEncoder;
import common.messages.client.CreateRoomMessage;

/**
 * Pedido de un usuario de crear una sala
 */
public class CreateTrucoRoomMessage extends CreateRoomMessage implements
      BasicClientGameMessage {

   private boolean a30, conFlor;

   public CreateTrucoRoomMessage() {
   }

   public CreateTrucoRoomMessage(int puntos, boolean a30, boolean conFlor) {
      super(puntos);
      this.a30 = a30;
      this.conFlor = conFlor;
   }

   @Override
   public void execute(IoSession session, BasicServerHandler server) {
      ((TrucoServerHandler) server).createRoom(session, puntos, a30, conFlor);
   }

   @Override
   public String toString() {
      return "Create Room (" + puntos + " pts), a " + (a30 ? "30" : "15")
            + ", " + (conFlor ? "con" : "sin") + " flor";
   }

   @Override
   public int getContentLength() {
      return super.getContentLength() + 2;
   }

   @Override
   public void decode(ByteBuffer buff) {
      super.decode(buff);
      a30 = buff.get() == TaringaProtocolEncoder.TRUE;
      conFlor = buff.get() == TaringaProtocolEncoder.TRUE;
   }

   @Override
   protected void encodeContent(ByteBuffer buff) {
      super.encodeContent(buff);
      buff
            .put(a30 ? TaringaProtocolEncoder.TRUE
                  : TaringaProtocolEncoder.FALSE);
      buff.put(conFlor ? TaringaProtocolEncoder.TRUE
            : TaringaProtocolEncoder.FALSE);
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x95;
   }
}
