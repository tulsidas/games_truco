package truco.common.model;

import java.util.Comparator;

import org.apache.mina.common.ByteBuffer;

public class Carta implements Comparable<Carta> {

    private Palo palo;

    private Numero num;

    private int valor;

    public Carta(Palo palo, Numero num, int valor) {
        this.palo = palo;
        this.num = num;
        this.valor = valor;
    }

    public Palo getPalo() {
        return palo;
    }

    public int getValorEnvido() {
        return num.getValorEnvido();
    }

    public Numero getNum() {
        return num;
    }

    public int getValor() {
        return valor;
    }

    // @Override
    public int compareTo(Carta o) {
        return valor - o.valor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((num == null) ? 0 : num.hashCode());
        result = prime * result + ((palo == null) ? 0 : palo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Carta other = (Carta) obj;
        if (num == null) {
            if (other.num != null) {
                return false;
            }
        }
        else if (!num.equals(other.num)) {
            return false;
        }
        if (palo == null) {
            if (other.palo != null) {
                return false;
            }
        }
        else if (!palo.equals(other.palo)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return num + " de " + palo;
    }

    public static Comparator<Carta> getEnvidoComparator() {
        return new Comparator<Carta>() {
            public int compare(Carta c, Carta d) {
                return c.getValorEnvido() - d.getValorEnvido();
            }
        };
    }

    public static void writeTo(Carta c, ByteBuffer buff) {
        switch (c.palo) {
        case ESPADA:
            buff.put((byte) 0x01);
            break;
        case BASTO:
            buff.put((byte) 0x02);
            break;
        case ORO:
            buff.put((byte) 0x03);
            break;
        case COPA:
            buff.put((byte) 0x04);
            break;
        }

        switch (c.num) {
        case ANCHO:
            buff.put((byte) 0x01);
            break;
        case DOS:
            buff.put((byte) 0x02);
            break;
        case TRES:
            buff.put((byte) 0x03);
            break;
        case CUATRO:
            buff.put((byte) 0x04);
            break;
        case CINCO:
            buff.put((byte) 0x05);
            break;
        case SEIS:
            buff.put((byte) 0x06);
            break;
        case SIETE:
            buff.put((byte) 0x07);
            break;
        case SOTA:
            buff.put((byte) 0x0A);
            break;
        case CABALLO:
            buff.put((byte) 0x0B);
            break;
        case REY:
            buff.put((byte) 0x0C);
            break;
        }

        // FIXME optimizar, no hace falta pasar el valor
        buff.put((byte) c.valor);
    }

    public static Carta readFrom(ByteBuffer buff) {
        Palo palo = null;

        Numero num = null;

        byte pb = buff.get();

        if (pb == 0x01) {
            palo = Palo.ESPADA;
        }
        else if (pb == 0x02) {
            palo = Palo.BASTO;
        }
        else if (pb == 0x03) {
            palo = Palo.ORO;
        }
        else if (pb == 0x04) {
            palo = Palo.COPA;
        }

        byte nb = buff.get();

        if (nb == 0x01) {
            num = Numero.ANCHO;
        }
        else if (nb == 0x02) {
            num = Numero.DOS;
        }
        else if (nb == 0x03) {
            num = Numero.TRES;
        }
        else if (nb == 0x04) {
            num = Numero.CUATRO;
        }
        else if (nb == 0x05) {
            num = Numero.CINCO;
        }
        else if (nb == 0x06) {
            num = Numero.SEIS;
        }
        else if (nb == 0x07) {
            num = Numero.SIETE;
        }
        else if (nb == 0x0A) {
            num = Numero.SOTA;
        }
        else if (nb == 0x0B) {
            num = Numero.CABALLO;
        }
        else if (nb == 0x0C) {
            num = Numero.REY;
        }

        int valor = buff.get();

        return new Carta(palo, num, valor);
    }
}