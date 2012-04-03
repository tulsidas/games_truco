package truco.common.game;

import org.apache.mina.common.ByteBuffer;

import truco.common.ifaz.GameHandler;
import truco.common.ifaz.GameMessage;

import common.messages.FixedLengthMessageAdapter;

public class PuntosEnvidoMessage extends FixedLengthMessageAdapter implements
      GameMessage {

   private int puntosEnJuego;

   private int tantoOponente;

   public PuntosEnvidoMessage() {
   }

   public PuntosEnvidoMessage(int puntosEnJuego, int tantoOponente) {
      this.puntosEnJuego = puntosEnJuego;
      this.tantoOponente = tantoOponente;
   }

   @Override
   public void execute(GameHandler game) {
      game.puntosEnvido(puntosEnJuego, tantoOponente);
   }

   @Override
   public String toString() {
      return "Puntos Envido: " + tantoOponente;
   }

   @Override
   public int getContentLength() {
      return 2;
   }

   @Override
   protected void encodeContent(ByteBuffer buff) {
      buff.put((byte) puntosEnJuego).put((byte) tantoOponente);
   }

   @Override
   public void decode(ByteBuffer buff) {
      puntosEnJuego = buff.get();
      tantoOponente = buff.get();
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x89;
   }
}
