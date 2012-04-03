package truco.server;

import server.JMXHandler;

public interface TrucoJMXHandler extends JMXHandler {
    public void crearTorneo(int salonId, int players, int puntos);

    public void cancelarTorneo(int salonId);
}
