package truco.common.game;

import org.apache.mina.common.ByteBuffer;

import truco.common.ifaz.GameHandler;
import truco.common.ifaz.GameMessage;

import common.messages.FixedLengthMessageAdapter;

public class PuntosFlorMessage extends FixedLengthMessageAdapter implements
      GameMessage {

   private int tantoOponente;

   public PuntosFlorMessage() {
   }

   public PuntosFlorMessage(int tantoOponente) {
      this.tantoOponente = tantoOponente;
   }

   @Override
   public void execute(GameHandler game) {
      game.puntosFlor(tantoOponente);
   }

   @Override
   public String toString() {
      return "Puntos Flor: " + tantoOponente;
   }

   @Override
   public int getContentLength() {
      return 1;
   }

   @Override
   protected void encodeContent(ByteBuffer buff) {
      buff.put((byte) tantoOponente);
   }

   @Override
   public void decode(ByteBuffer buff) {
      tantoOponente = buff.get();
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x98;
   }
}
