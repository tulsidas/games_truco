package truco.server;

import org.apache.mina.common.IoService;

import server.GameServiceManager;

public class TrucoGameServiceManager extends GameServiceManager implements
        TrucoGameServiceManagerMBean {

    private TrucoJMXHandler server;

    public TrucoGameServiceManager(IoService service, TrucoJMXHandler server) {
        super(service, server);
        this.server = server;
    }

    @Override
    public void crearTorneo(int salonId, int players, int puntos) {
        server.crearTorneo(salonId, players, puntos);
    }

    public void cancelarTorneo(int salonId) {
        server.cancelarTorneo(salonId);
    }
}
