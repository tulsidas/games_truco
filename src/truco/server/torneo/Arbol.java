package truco.server.torneo;

import java.util.Set;

import org.apache.mina.common.IoSession;

import truco.server.TrucoSaloon;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class Arbol {
    private Estado estado;

    private IoSession player;

    private Arbol padre, izq, der;

    public Arbol(int hijos) {
        this.estado = Estado.INDEFINIDO;
        this.player = null;

        if (hijos > 0) {
            izq = new Arbol(hijos - 1);
            izq.padre = this;
            der = new Arbol(hijos - 1);
            der.padre = this;
        }
    }

    public Arbol getPadre() {
        return padre;
    }

    public Arbol getIzq() {
        return izq;
    }

    public Arbol getDer() {
        return der;
    }

    public IoSession getPlayer() {
        return player;
    }

    public void setPlayer(IoSession player) {
        this.player = player;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public int getDepth() {
        if (getPadre() == null) {
            return 0;
        }
        else {
            return 1 + getPadre().getDepth();
        }
    }

    public boolean isCompleto() {
        if (player != null && (izq == null && der == null)) {
            return true;
        }
        else {
            if (izq != null && der != null) {
                return izq.isCompleto() && der.isCompleto();
            }
            else {
                return player != null;
            }
        }
    }

    /** que el arbol este completo y todos los nodos en estado LISTO */
    public boolean todosListos() {
        return isCompleto()
                && Iterables.all(getHojas(), new Predicate<Arbol>() {
                    @Override
                    public boolean apply(Arbol hoja) {
                        return hoja.getEstado() == Estado.LISTO;
                    }
                });
    }

    public Iterable<IoSession> getPlayers() {
        return Iterables.transform(getHojas(),
                new Function<Arbol, IoSession>() {
                    @Override
                    public IoSession apply(Arbol a) {
                        return a.player;
                    }
                });
    }

    public synchronized boolean agregar(IoSession nuevo) {
        // agrego sólo en hojas
        if (izq == null && der == null) {
            if (player != null) {
                return false;
            }
            else {
                player = nuevo;
                return true;
            }
        }
        else {
            // soy nodo intermedio
            if (izq.isCompleto()) {
                return der.agregar(nuevo);
            }
            else {
                return izq.agregar(nuevo);
            }
        }
    }

    public synchronized void sacar(IoSession sess) {
        if (contains(sess)) {
            Arbol n = getNodo(sess);
            n.player = null;
            n.estado = Estado.INDEFINIDO;
        }
    }

    /** PRE: contains(player) */
    public Arbol getNodo(IoSession player) {
        if (this.player == player) {
            return this;
        }
        else {
            if (izq != null && izq.contains(player)) {
                return izq.getNodo(player);
            }
            else if (der != null && der.contains(player)) {
                return der.getNodo(player);
            }
        }

        return null;
    }

    public boolean contains(IoSession player) {
        if (this.player == player) {
            return true;
        }
        else {
            if (izq != null && der != null) {
                return izq.contains(player) || der.contains(player);
            }
            else {
                return false;
            }
        }
    }

    @Override
    public String toString() {
        if (player != null) {
            return "[" + player + "]";
        }
        else {
            if (izq != null && der != null) {
                return "(" + izq.toString() + " | " + der.toString() + ")";
            }
            else {
                return "(|)";
            }
        }
    }

    public String toString(TrucoSaloon saloon) {
        if (player != null) {
            return "[" + saloon.getUser(player) + "]";
        }
        else {
            if (izq != null && der != null) {
                return "(" + izq.toString(saloon) + " | "
                        + der.toString(saloon) + ")";
            }
            else {
                return "(|)";
            }
        }
    }

    public static enum Estado {
        INDEFINIDO, LISTO, ABANDONO, JUGANDO
    }

    public void pruneChild() {
        this.izq = null;
        this.der = null;
    }

    public Arbol getSibling() {
        if (padre.izq == this) {
            return padre.der;
        }
        else {
            return padre.izq;
        }
    }

    public Set<Arbol> getHojas() {
        Set<Arbol> ret = Sets.newHashSet();

        if (izq == null && der == null) {
            ret.add(this);
        }
        else {
            ret.addAll(izq.getHojas());
            ret.addAll(der.getHojas());
        }
        return ret;
    }

    public Set<Arbol> getMatches() {
        Set<Arbol> ret = Sets.newHashSet();

        for (Arbol a : getHojas()) {
            if (a.getPadre() != null) {
                ret.add(a.getPadre());
            }
        }

        return ret;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((der == null) ? 0 : der.hashCode());
        result = prime * result + ((estado == null) ? 0 : estado.hashCode());
        result = prime * result + ((izq == null) ? 0 : izq.hashCode());
        result = prime * result + ((player == null) ? 0 : player.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Arbol other = (Arbol) obj;
        if (der == null) {
            if (other.der != null)
                return false;
        }
        else if (!der.equals(other.der))
            return false;
        if (estado == null) {
            if (other.estado != null)
                return false;
        }
        else if (!estado.equals(other.estado))
            return false;
        if (izq == null) {
            if (other.izq != null)
                return false;
        }
        else if (!izq.equals(other.izq))
            return false;
        if (player == null) {
            if (other.player != null)
                return false;
        }
        else if (!player.equals(other.player))
            return false;
        return true;
    }

}
