package truco.common.game;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

import truco.common.ifaz.ClientGameMessage;
import truco.common.ifaz.GameHandler;
import truco.common.ifaz.GameMessage;
import truco.common.ifaz.TrucoSaloonHandler;
import truco.common.messages.TrucoClientGameMessage;

public class AlMazoMessage extends TrucoClientGameMessage implements
      GameMessage, ClientGameMessage {

   private int puntosGanados;

   public AlMazoMessage() {
   }

   public AlMazoMessage(int puntosGanados) {
      this.puntosGanados = puntosGanados;
   }

   @Override
   public void execute(GameHandler game) {
      game.alMazo(puntosGanados);
   }

   @Override
   public void execute(IoSession session, TrucoSaloonHandler salon) {
      salon.alMazo(session);
   }

   @Override
   public String toString() {
      return "Al Mazo (" + puntosGanados + ")";
   }

   @Override
   public void decode(ByteBuffer buff) {
      puntosGanados = buff.get();
   }

   @Override
   protected void encodeContent(ByteBuffer buff) {
      buff.put((byte) puntosGanados);
   }

   @Override
   public int getContentLength() {
      return 1;
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x80;
   }
}
