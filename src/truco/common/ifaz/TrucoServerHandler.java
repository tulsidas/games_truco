package truco.common.ifaz;

import org.apache.mina.common.IoSession;

import common.ifaz.BasicServerHandler;

public interface TrucoServerHandler extends BasicServerHandler {
   void createRoom(IoSession session, int puntos, boolean a30, boolean conFlor);
}
