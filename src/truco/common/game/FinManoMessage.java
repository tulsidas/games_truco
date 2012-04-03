package truco.common.game;

import org.apache.mina.common.ByteBuffer;

import truco.common.ifaz.GameHandler;
import truco.common.ifaz.GameMessage;

import common.messages.FixedLengthMessageAdapter;

public class FinManoMessage extends FixedLengthMessageAdapter implements
      GameMessage {

   private int puntosYo, puntosOtro;

   public FinManoMessage() {
   }

   public FinManoMessage(int puntosYo, int puntosOtro) {
      this.puntosYo = puntosYo;
      this.puntosOtro = puntosOtro;
   }

   public int getPuntosYo() {
      return puntosYo;
   }

   public void setPuntosYo(int puntosYo) {
      this.puntosYo = puntosYo;
   }

   public int getPuntosOtro() {
      return puntosOtro;
   }

   public void setPuntosOtro(int puntosOtro) {
      this.puntosOtro = puntosOtro;
   }

   // @Override
   public void execute(GameHandler game) {
      game.finMano(puntosYo, puntosOtro);
   }

   @Override
   public String toString() {
      return "Fin Mano";
   }

   @Override
   public int getContentLength() {
      return 2;
   }

   @Override
   protected void encodeContent(ByteBuffer buff) {
      buff.put((byte) puntosYo).put((byte) puntosOtro);
   }

   @Override
   public void decode(ByteBuffer buff) {
      puntosYo = buff.get();
      puntosOtro = buff.get();
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x85;
   }
}
