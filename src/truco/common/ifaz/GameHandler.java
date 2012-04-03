package truco.common.ifaz;

import common.ifaz.BasicGameHandler;

import truco.common.model.Carta;
import truco.common.model.Mano;

public interface GameHandler extends BasicGameHandler {
   /**
    * Todos los jugadores listos, empieza el juego
    */
   public void startMano(Mano mano, boolean empiezo);

   public void carta(Carta c, boolean teToca);

   public void finMano(int puntosYo, int puntosOtro);

   public void finMano(int puntosYo, int puntosOtro, Carta c);

   public void truco();

   public void retruco();

   public void vale4();

   public void quiero();

   public void noquiero(int puntosGanados);

   public void envido(int puntosEnJuego);

   public void real(int puntosEnJuego);

   public void falta(int puntosEnJuego);

   public void alMazo(int puntosOtro);

   public void puntosEnvido(int puntosGanados, int tantoOponente);

   public void flor();

   public void contraflor();

   public void hastaAquiLlegoElOlor();

   public void puntosFlor(int tantoOponente);

   public void tuTurno();
}
