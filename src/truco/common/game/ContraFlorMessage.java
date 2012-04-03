package truco.common.game;

import org.apache.mina.common.IoSession;

import truco.common.ifaz.ClientGameMessage;
import truco.common.ifaz.GameHandler;
import truco.common.ifaz.GameMessage;
import truco.common.ifaz.TrucoSaloonHandler;
import truco.common.messages.TrucoClientGameMessage;

public class ContraFlorMessage extends TrucoClientGameMessage implements
      GameMessage, ClientGameMessage {

   @Override
   public void execute(IoSession session, TrucoSaloonHandler salon) {
      salon.contraflor(session);
   }

   @Override
   public void execute(GameHandler game) {
      game.contraflor();
   }

   @Override
   public String toString() {
      return "Contraflor";
   }

   @Override
   public int getContentLength() {
      return 0;
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x99;
   }
}
