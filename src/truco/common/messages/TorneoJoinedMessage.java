package truco.common.messages;

import org.apache.mina.common.IoSession;

import truco.common.ifaz.TrucoSaloonHandler;

public class TorneoJoinedMessage extends TrucoClientGameMessage {

   @Override
   public void execute(IoSession session, TrucoSaloonHandler salon) {
      salon.joinedTorneo(session);
   }

   @Override
   public int getContentLength() {
      return 0;
   }

   @Override
   public byte getMessageId() {
      return (byte) 0xAE;
   }
   
   @Override
    public String toString() {
        return "joinedTorneo";
    }
}
