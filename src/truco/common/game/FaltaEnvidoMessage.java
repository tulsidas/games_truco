package truco.common.game;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

import truco.common.ifaz.ClientGameMessage;
import truco.common.ifaz.GameHandler;
import truco.common.ifaz.GameMessage;
import truco.common.ifaz.TrucoSaloonHandler;
import truco.common.messages.TrucoClientGameMessage;

public class FaltaEnvidoMessage extends TrucoClientGameMessage implements
      GameMessage, ClientGameMessage {

   private int ptos;

   public FaltaEnvidoMessage() {
   }

   public FaltaEnvidoMessage(int ptos) {
      this.ptos = ptos;
   }

   // @Override
   public void execute(GameHandler game) {
      game.falta(ptos);
   }

   // @Override
   public void execute(IoSession session, TrucoSaloonHandler salon) {
      salon.falta(session);
   }

   @Override
   public String toString() {
      return "Falta Envido";
   }

   @Override
   public int getContentLength() {
      return 1;
   }

   @Override
   public void decode(ByteBuffer buff) {
      ptos = buff.get();
   }

   @Override
   protected void encodeContent(ByteBuffer buff) {
      buff.put((byte) ptos);
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x83;
   }
}
