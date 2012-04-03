package truco.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.mina.common.ByteBuffer;

/**
 * Tres cartas
 * 
 * @author Tulsi
 */
public class Mano {

    private static final int BASE_ENVIDO = 20;

    private Carta c1, c2, c3;

    public Mano(Carta c1, Carta c2, Carta c3) {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
    }

    public int getValorEnvido() {
        if (c1.getPalo() == c2.getPalo() && c2.getPalo() == c3.getPalo()) {
            // FLOR
            List<Carta> mano = new ArrayList<Carta>(3);
            mano.add(c1);
            mano.add(c2);
            mano.add(c3);

            Collections.sort(mano, Carta.getEnvidoComparator());
            return mano.get(1).getValorEnvido() + mano.get(2).getValorEnvido()
                    + BASE_ENVIDO;
        }
        else {
            if (c1.getPalo() == c2.getPalo()) {
                return c1.getValorEnvido() + c2.getValorEnvido() + BASE_ENVIDO;
            }
            else if (c1.getPalo() == c3.getPalo()) {
                return c1.getValorEnvido() + c3.getValorEnvido() + BASE_ENVIDO;
            }
            else if (c2.getPalo() == c3.getPalo()) {
                return c2.getValorEnvido() + c3.getValorEnvido() + BASE_ENVIDO;
            }
            else {
                // todas distintas
                List<Carta> mano = new ArrayList<Carta>(3);
                mano.add(c1);
                mano.add(c2);
                mano.add(c3);

                return Collections.max(mano, Carta.getEnvidoComparator())
                        .getValorEnvido();
            }
        }
    }

    public boolean tieneCarta(Carta c) {
        return c1.equals(c) || c2.equals(c) || c3.equals(c);
    }

    public boolean tieneFlor() {
        return c1.getPalo() == c2.getPalo() && c2.getPalo() == c3.getPalo();
    }

    public int getValorFlor() {
        return c1.getValorEnvido() + c2.getValorEnvido() + c3.getValorEnvido()
                + BASE_ENVIDO;
    }

    public Carta getCarta1() {
        return c1;
    }

    public Carta getCarta2() {
        return c2;
    }

    public Carta getCarta3() {
        return c3;
    }

    @Override
    public String toString() {
        return c1 + " | " + c2 + " | " + c3;
    }

    public static void writeTo(Mano mano, ByteBuffer buff) {
        Carta.writeTo(mano.getCarta1(), buff);
        Carta.writeTo(mano.getCarta2(), buff);
        Carta.writeTo(mano.getCarta3(), buff);
    }

    public static Mano readFrom(ByteBuffer buff) {
        Carta c1 = Carta.readFrom(buff);
        Carta c2 = Carta.readFrom(buff);
        Carta c3 = Carta.readFrom(buff);

        return new Mano(c1, c2, c3);
    }

}
