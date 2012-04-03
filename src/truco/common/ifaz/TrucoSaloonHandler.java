package truco.common.ifaz;

import org.apache.mina.common.IoSession;

import truco.common.model.Carta;

/**
 * Interfaz de los mensajes que recibe el Saloon de los clientes
 */
public interface TrucoSaloonHandler {
    public void alMazo(IoSession session);

    public void carta(IoSession session, Carta carta);

    public void envido(IoSession session);

    public void falta(IoSession session);

    public void flor(IoSession session);

    public void contraflor(IoSession session);

    public void hastaAquiLlegoElOlor(IoSession session);

    public void noquiero(IoSession session);

    public void quiero(IoSession session);

    public void real(IoSession session);

    public void truco(IoSession session);

    public void retruco(IoSession session);

    public void vale4(IoSession session);

    public void proximaMano(IoSession session);

    /***************************************************************************
     * Torneos
     **************************************************************************/
    public void joinTorneo(IoSession session);

    public void joinedTorneo(IoSession session);

    public void abandonTorneo(IoSession session);

    public void torneoChat(IoSession session, String msg);
}