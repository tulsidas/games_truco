package truco.common.game;

import org.apache.mina.common.IoSession;

import truco.common.ifaz.ClientGameMessage;
import truco.common.ifaz.TrucoSaloonHandler;
import truco.common.messages.TrucoClientGameMessage;

/**
 * Mensaje del server que da comienzo al proximo turno
 */
public class ProximaManoMessage extends TrucoClientGameMessage implements
      ClientGameMessage {

   // @Override
   public void execute(IoSession session, TrucoSaloonHandler salon) {
      salon.proximaMano(session);
   }

   @Override
   public String toString() {
      return "Proxima Mano";
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x88;
   }

   @Override
   public int getContentLength() {
      return 0;
   }
}