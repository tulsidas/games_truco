package truco.server;

import server.AbstractSaloon;
import server.ServerSessionHandler;
import truco.common.messages.TrucoProtocolDecoder;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class TrucoSessionHandler extends ServerSessionHandler implements
        TrucoJMXHandler {

    public TrucoSessionHandler() {
        super(new TrucoProtocolDecoder());

        salones = Lists.newArrayList();
        salones.add(new TrucoSaloon(0, this));
        salones.add(new TrucoSaloon(1, this));
        salones.add(new TrucoSaloon(2, this));
    }

    private TrucoSaloon getSaloon(final int id) {
        return (TrucoSaloon) Iterables.find(salones,
                new Predicate<AbstractSaloon>() {
                    public boolean apply(AbstractSaloon sal) {
                        return sal.getId() == id;
                    }
                });
    }

    @Override
    public void crearTorneo(int salonId, int players, int puntos) {
        getSaloon(salonId).crearTorneo(players, puntos);
    }

    @Override
    public void cancelarTorneo(int salonId) {
        getSaloon(salonId).cancelarTorneo();
    }

    @Override
    protected int getCodigoJuego() {
        // truco = 1 para la base
        return 1;
    }
}