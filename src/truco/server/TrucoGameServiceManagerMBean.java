package truco.server;

import server.GameServiceManagerMBean;

public interface TrucoGameServiceManagerMBean extends GameServiceManagerMBean {
   public void crearTorneo(int salonId, int players, int puntos);
}
