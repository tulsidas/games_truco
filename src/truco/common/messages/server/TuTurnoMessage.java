package truco.common.messages.server;

import truco.common.ifaz.GameHandler;
import truco.common.ifaz.GameMessage;

import common.messages.FixedLengthMessageAdapter;

public class TuTurnoMessage extends FixedLengthMessageAdapter implements
      GameMessage {

   public TuTurnoMessage() {
   }

   public void execute(GameHandler game) {
      game.tuTurno();
   }

   @Override
   public String toString() {
      return "Tu Turno";
   }

   @Override
   public int getContentLength() {
      return 0;
   }

   @Override
   public byte getMessageId() {
      return (byte) 0xA0;
   }
}
