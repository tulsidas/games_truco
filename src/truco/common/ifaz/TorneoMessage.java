package truco.common.ifaz;

public interface TorneoMessage {
   // mensaje del server se ejecuta en el torneo
   public void execute(TorneoHandler torneo);
}
