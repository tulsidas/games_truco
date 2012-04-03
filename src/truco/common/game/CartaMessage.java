package truco.common.game;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

import common.messages.TaringaProtocolEncoder;

import truco.common.ifaz.ClientGameMessage;
import truco.common.ifaz.GameHandler;
import truco.common.ifaz.GameMessage;
import truco.common.ifaz.TrucoSaloonHandler;
import truco.common.messages.TrucoClientGameMessage;
import truco.common.model.Carta;

public class CartaMessage extends TrucoClientGameMessage implements
      GameMessage, ClientGameMessage {

   private Carta carta;

   private boolean teToca;

   public CartaMessage() {
   }

   // constructor para TrucoScene, el boolean no importa
   public CartaMessage(Carta carta) {
      this(carta, false);
   }

   public CartaMessage(Carta carta, boolean teToca) {
      this.carta = carta;
      this.teToca = teToca;
   }

   @Override
   public void execute(GameHandler game) {
      game.carta(carta, teToca);
   }

   @Override
   public void execute(IoSession session, TrucoSaloonHandler salon) {
      salon.carta(session, carta);
   }

   @Override
   public String toString() {
      return "CartaMessage: " + carta;
   }

   @Override
   public int getContentLength() {
      // 3 de la carta + 1 te toca ||
      return 4;
   }

   @Override
   protected void encodeContent(ByteBuffer buff) {
      Carta.writeTo(carta, buff);
      buff.put(teToca ? TaringaProtocolEncoder.TRUE
            : TaringaProtocolEncoder.FALSE);
   }

   @Override
   public void decode(ByteBuffer buff) {
      carta = Carta.readFrom(buff);
      teToca = buff.get() == TaringaProtocolEncoder.TRUE;
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x94;
   }
}
