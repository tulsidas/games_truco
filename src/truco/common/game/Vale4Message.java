package truco.common.game;

import org.apache.mina.common.IoSession;

import truco.common.ifaz.ClientGameMessage;
import truco.common.ifaz.GameHandler;
import truco.common.ifaz.GameMessage;
import truco.common.ifaz.TrucoSaloonHandler;
import truco.common.messages.TrucoClientGameMessage;

public class Vale4Message extends TrucoClientGameMessage implements
      GameMessage, ClientGameMessage {

   @Override
   public void execute(GameHandler game) {
      game.vale4();
   }

   @Override
   public void execute(IoSession session, TrucoSaloonHandler salon) {
      salon.vale4(session);
   }

   @Override
   public String toString() {
      return "Vale 4";
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x92;
   }

   @Override
   public int getContentLength() {
      return 0;
   }
}
