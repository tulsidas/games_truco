package truco.common.model;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Mazo {
    private List<Carta> cartas;

    private static Random srnd = new SecureRandom();

    public Mazo() {
        reset();
    }

    public void reset() {
        cartas = nuevoMazo();
    }

    private List<Carta> nuevoMazo() {
        List<Carta> ret = Collections
                .synchronizedList(new ArrayList<Carta>(40));

        // cuatros
        for (Palo p : Palo.values()) {
            ret.add(new Carta(p, Numero.CUATRO, 0));
        }

        // cincos
        for (Palo p : Palo.values()) {
            ret.add(new Carta(p, Numero.CINCO, 1));
        }

        // seis
        for (Palo p : Palo.values()) {
            ret.add(new Carta(p, Numero.SEIS, 2));
        }

        // sietes falsos
        ret.add(new Carta(Palo.BASTO, Numero.SIETE, 3));
        ret.add(new Carta(Palo.COPA, Numero.SIETE, 3));

        // sotas
        for (Palo p : Palo.values()) {
            ret.add(new Carta(p, Numero.SOTA, 4));
        }

        // equus
        for (Palo p : Palo.values()) {
            ret.add(new Carta(p, Numero.CABALLO, 5));
        }

        // regis
        for (Palo p : Palo.values()) {
            ret.add(new Carta(p, Numero.REY, 6));
        }

        // anchos falsos
        ret.add(new Carta(Palo.ORO, Numero.ANCHO, 7));
        ret.add(new Carta(Palo.COPA, Numero.ANCHO, 7));

        // dos
        for (Palo p : Palo.values()) {
            ret.add(new Carta(p, Numero.DOS, 8));
        }

        // tres
        for (Palo p : Palo.values()) {
            ret.add(new Carta(p, Numero.TRES, 9));
        }

        // siete de oro
        ret.add(new Carta(Palo.ORO, Numero.SIETE, 10));

        // siete de espada
        ret.add(new Carta(Palo.ESPADA, Numero.SIETE, 11));

        // ancho de basto
        ret.add(new Carta(Palo.BASTO, Numero.ANCHO, 12));

        // ancho de espada
        ret.add(new Carta(Palo.ESPADA, Numero.ANCHO, 13));

        Collections.shuffle(ret, srnd);

        // ret = Lists.newArrayList(Iterables.filter(ret, new Predicate<Carta>()
        // {
        // @Override
        // public boolean apply(Carta c) {
        // return c.getPalo() == Palo.ESPADA;
        // }
        // }));

        return ret;
    }

    public synchronized Mano getMano() {
        return new Mano(cartas.remove(0), cartas.remove(0), cartas.remove(0));
    }
}