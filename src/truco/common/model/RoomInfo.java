package truco.common.model;

import org.apache.mina.common.ByteBuffer;

import common.util.StringUtil;

public class RoomInfo implements Comparable<RoomInfo> {

    private int id;

    /**
     * la ronda a la que pertenece el juego, tomado desde la raíz del árbol, o
     * sea 0=final, 1=semi, 2=cuartos, etc
     */
    private int ronda;

    private String user1, user2;

    private int puntaje1, puntaje2;

    public RoomInfo(int id, int ronda, String p1, String p2) {
        this(id, ronda, p1, p2, 0, 0);
    }

    public RoomInfo(int id, int ronda, String p1, String p2, int puntaje1,
            int puntaje2) {
        this.id = id;
        this.ronda = ronda;
        this.user1 = p1;
        this.user2 = p2;
        this.puntaje1 = puntaje1;
        this.puntaje2 = puntaje2;
    }

    @Override
    public int hashCode() {
        return id;
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
        final RoomInfo other = (RoomInfo) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    public int compareTo(RoomInfo o) {
        if (o.ronda == ronda) {
            // return o.id - id;
            return id - o.id;
        }
        else {
            return o.ronda - ronda;
        }
    }

    public static void writeTo(RoomInfo ri, ByteBuffer buff) {
        buff.putInt(ri.id);

        buff.putInt(ri.ronda);

        buff.putInt(ri.puntaje1);
        buff.putInt(ri.puntaje2);

        StringUtil.encode(buff, ri.user1);
        StringUtil.encode(buff, ri.user2);
    }

    public static RoomInfo readFrom(ByteBuffer buff) {
        int id = buff.getInt();

        int ronda = buff.getInt();

        int p1 = buff.getInt();
        int p2 = buff.getInt();

        String u1 = StringUtil.decode(buff);
        String u2 = StringUtil.decode(buff);

        return new RoomInfo(id, ronda, u1, u2, p1, p2);
    }

    public int getId() {
        return id;
    }

    public int getRonda() {
        return ronda;
    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    public int getPuntaje1() {
        return puntaje1;
    }

    public void setPuntaje1(int puntaje1) {
        this.puntaje1 = puntaje1;
    }

    public int getPuntaje2() {
        return puntaje2;
    }

    public void setPuntaje2(int puntaje2) {
        this.puntaje2 = puntaje2;
    }
}