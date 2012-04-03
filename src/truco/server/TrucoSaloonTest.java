package truco.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.mina.common.IoSession;
import org.junit.Test;

import truco.common.messages.JoinTorneoMessage;
import truco.common.messages.server.GanadorTorneoMessage;
import truco.common.messages.server.PartidosTorneoMessage;
import truco.common.messages.server.TorneoRoomJoinedMessage;
import truco.common.messages.server.TorneoStatusMessage;

import common.game.OponentAbandonedMessage;
import common.ifaz.POSTHandler;
import common.model.User;

public class TrucoSaloonTest {

    @Test
    public void testUnirseSinTorneo() {
        POSTHandler ph = mock(POSTHandler.class);
        when(ph.validateUser(any(String.class), any(String.class))).thenReturn(
                100);

        TrucoSaloon ts = new TrucoSaloon(11, ph);

        IoSession p1 = mock(IoSession.class);
        when(p1.toString()).thenReturn("p1");

        ts.login(p1, new User("p1"), "p1");

        ts.joinTorneo(p1);

        verify(p1).write(any(TorneoStatusMessage.class));
    }

    @Test
    public void testTorneoSimpleAbandonaEnJuego() {
        POSTHandler ph = mock(POSTHandler.class);
        when(ph.validateUser(any(String.class), any(String.class))).thenReturn(
                100);

        TrucoSaloon ts = new TrucoSaloon(11, ph);

        IoSession p1 = mock(IoSession.class);
        when(p1.toString()).thenReturn("p1");
        IoSession p2 = mock(IoSession.class);
        when(p2.toString()).thenReturn("p2");

        ts.login(p1, new User("p1"), "p1");
        ts.login(p2, new User("p2"), "p2");

        ts.crearTorneo(2, 10);

        ts.joinTorneo(p1); // el pedido
        ts.joinedTorneo(p1); // la confirmacion
        verify(p1).write(any(PartidosTorneoMessage.class));

        assertEquals(1, ts.enTorneo().size());

        ts.joinTorneo(p2); // el pedido
        ts.joinedTorneo(p2); // la confirmacion
        verify(p1).write(any(JoinTorneoMessage.class));
        verify(p2).write(any(PartidosTorneoMessage.class));

        verify(p1).write(any(TorneoRoomJoinedMessage.class));
        verify(p2).write(any(TorneoRoomJoinedMessage.class));

        assertEquals(0, ts.enTorneo().size());

        ts.playerLeftRoom(p1);

        // p2 vuelve al torneoscene
        ts.joinedTorneo(p2); // vuelven al lobby

        assertEquals(1, ts.enTorneo().size());

        verify(p2).write(any(OponentAbandonedMessage.class));
        verify(p2, times(2)).write(any(PartidosTorneoMessage.class));

        // verifyNoMoreInteractions(p1, p2);
    }

    @Test
    public void testTorneoSimple() {
        POSTHandler ph = mock(POSTHandler.class);
        when(ph.validateUser(any(String.class), any(String.class))).thenReturn(
                100);

        TrucoSaloon ts = new TrucoSaloon(11, ph);

        IoSession p1 = mock(IoSession.class);
        when(p1.toString()).thenReturn("p1");
        IoSession p2 = mock(IoSession.class);
        when(p2.toString()).thenReturn("p2");

        ts.login(p1, new User("p1"), "p1");
        ts.login(p2, new User("p2"), "p2");

        ts.crearTorneo(2, 10);

        ts.joinTorneo(p1); // el pedido
        ts.joinedTorneo(p1); // la confirmacion
        verify(p1).write(any(PartidosTorneoMessage.class));

        assertEquals(1, ts.enTorneo().size());

        ts.joinTorneo(p2); // el pedido
        ts.joinedTorneo(p2); // la confirmacion
        verify(p2).write(any(PartidosTorneoMessage.class));
        verify(p1).write(any(JoinTorneoMessage.class));

        verify(p1).write(any(TorneoRoomJoinedMessage.class));
        verify(p2).write(any(TorneoRoomJoinedMessage.class));

        // gano p1
        ts.finPartidoTorneo(p1);

        ts.joinedTorneo(p1); // vuelven al lobby
        verify(p1, times(2)).write(any(PartidosTorneoMessage.class));

        ts.joinedTorneo(p2); // vuelven al lobby
        verify(p2, times(2)).write(any(PartidosTorneoMessage.class));
        verify(p1, times(2)).write(any(JoinTorneoMessage.class));

        assertEquals(2, ts.enTorneo().size());

        // verifyNoMoreInteractions(p1, p2);
    }

    @Test
    public void testTorneoSimpleConfirmaTarde() {
        POSTHandler ph = mock(POSTHandler.class);
        when(ph.validateUser(any(String.class), any(String.class))).thenReturn(
                100);

        TrucoSaloon ts = new TrucoSaloon(11, ph);

        IoSession p1 = mock(IoSession.class);
        when(p1.toString()).thenReturn("p1");
        IoSession p2 = mock(IoSession.class);
        when(p2.toString()).thenReturn("p2");

        ts.login(p1, new User("p1"), "p1");
        ts.login(p2, new User("p2"), "p2");

        ts.crearTorneo(2, 10);

        ts.joinTorneo(p1); // el pedido
        ts.joinTorneo(p2); // el pedido

        ts.joinedTorneo(p1); // la confirmacion (tardia)
        verify(p1, times(1)).write(any(PartidosTorneoMessage.class));

        ts.joinedTorneo(p2); // la confirmacion (tardia)
        verify(p2, times(1)).write(any(PartidosTorneoMessage.class));
        verify(p1, times(1)).write(any(JoinTorneoMessage.class));

        verify(p1).write(any(TorneoRoomJoinedMessage.class));
        verify(p2).write(any(TorneoRoomJoinedMessage.class));

        // gano p1
        ts.finPartidoTorneo(p1);

        // verifyNoMoreInteractions(p1, p2);
    }

    @Test
    public void testTorneoSimple4() {
        POSTHandler ph = mock(POSTHandler.class);
        when(ph.validateUser(any(String.class), any(String.class))).thenReturn(
                100);

        TrucoSaloon ts = new TrucoSaloon(11, ph);

        IoSession p1 = mock(IoSession.class);
        when(p1.toString()).thenReturn("p1");
        IoSession p2 = mock(IoSession.class);
        when(p2.toString()).thenReturn("p2");
        IoSession p3 = mock(IoSession.class);
        when(p3.toString()).thenReturn("p3");
        IoSession p4 = mock(IoSession.class);
        when(p4.toString()).thenReturn("p4");

        ts.login(p1, new User("p1"), "p1");
        ts.login(p2, new User("p2"), "p2");
        ts.login(p3, new User("p3"), "p3");
        ts.login(p4, new User("p4"), "p4");

        ts.crearTorneo(4, 10);

        ts.joinTorneo(p1); // el pedido
        ts.joinedTorneo(p1); // la confirmacion
        verify(p1, times(1)).write(any(PartidosTorneoMessage.class));

        assertEquals(1, ts.enTorneo().size());

        ts.joinTorneo(p2); // el pedido
        ts.joinedTorneo(p2); // la confirmacion
        verify(p2, times(1)).write(any(PartidosTorneoMessage.class));
        verify(p1, times(1)).write(any(JoinTorneoMessage.class));

        assertEquals(2, ts.enTorneo().size());

        ts.joinTorneo(p3); // el pedido
        ts.joinedTorneo(p3); // la confirmacion
        verify(p3, times(1)).write(any(PartidosTorneoMessage.class));
        verify(p2, times(1)).write(any(JoinTorneoMessage.class));
        verify(p1, times(2)).write(any(JoinTorneoMessage.class));

        assertEquals(3, ts.enTorneo().size());

        ts.joinTorneo(p4); // el pedido
        ts.joinedTorneo(p4); // la confirmacion
        verify(p4, times(1)).write(any(PartidosTorneoMessage.class));
        verify(p3, times(1)).write(any(JoinTorneoMessage.class));
        verify(p2, times(2)).write(any(JoinTorneoMessage.class));
        verify(p1, times(3)).write(any(JoinTorneoMessage.class));

        verify(p1).write(any(TorneoRoomJoinedMessage.class));
        verify(p2).write(any(TorneoRoomJoinedMessage.class));
        verify(p3).write(any(TorneoRoomJoinedMessage.class));
        verify(p4).write(any(TorneoRoomJoinedMessage.class));

        // verifyNoMoreInteractions(p2, p3);

        // gano p1
        ts.finPartidoTorneo(p1);

        // p1 y p2 vuelven al lobby torneo
        ts.joinedTorneo(p1);
        ts.joinedTorneo(p2);

        assertEquals(2, ts.enTorneo().size()); // p1 y p2

        // gano p4
        ts.finPartidoTorneo(p4);

        // p3 y p4 vuelven al lobby torneo
        ts.joinedTorneo(p3);
        ts.joinedTorneo(p4);

        assertEquals(2, ts.enTorneo().size()); // p2 y p3

        verify(p1, times(2)).write(any(TorneoRoomJoinedMessage.class));
        verify(p4, times(2)).write(any(TorneoRoomJoinedMessage.class));

        // gano p1
        ts.finPartidoTorneo(p1);

        // p1 y p4 vuelven al lobby torneo
        ts.joinedTorneo(p1);
        ts.joinedTorneo(p4);

        assertEquals(4, ts.enTorneo().size()); // todos

        // verify(p1).write(any(PartidosTorneoMessage.class));

        // verifyNoMoreInteractions(p1, p2);
    }

    @Test
    public void testAbandonoSimple() {
        POSTHandler ph = mock(POSTHandler.class);
        when(ph.validateUser(any(String.class), any(String.class))).thenReturn(
                100);

        TrucoSaloon ts = new TrucoSaloon(11, ph);

        IoSession p1 = mock(IoSession.class);
        when(p1.toString()).thenReturn("p1");
        IoSession p2 = mock(IoSession.class);
        when(p2.toString()).thenReturn("p2");

        ts.login(p1, new User("p1"), "p1");
        ts.login(p2, new User("p2"), "p2");

        ts.crearTorneo(2, 10);

        ts.joinTorneo(p1); // el pedido
        ts.joinedTorneo(p1); // la confirmacion
        ts.userDisconnected(p1); // abandona

        ts.joinTorneo(p1); // el pedido
        ts.joinedTorneo(p1); // la confirmacion
        ts.userDisconnected(p1); // abandona

        ts.joinTorneo(p2); // el pedido
        ts.joinedTorneo(p2); // la confirmacion

        ts.joinTorneo(p1); // el pedido
        ts.joinedTorneo(p1); // la confirmacion

        verify(p1).write(any(TorneoRoomJoinedMessage.class));
        verify(p2).write(any(TorneoRoomJoinedMessage.class));

        // gano p1
        ts.finPartidoTorneo(p1);

        verify(p1).write(any(GanadorTorneoMessage.class));

        // verifyNoMoreInteractions(p1, p2);
    }

    @Test
    public void testAbandon4() {
        POSTHandler ph = mock(POSTHandler.class);
        when(ph.validateUser(any(String.class), any(String.class))).thenReturn(
                100);

        TrucoSaloon ts = new TrucoSaloon(11, ph);

        IoSession p1 = mock(IoSession.class);
        when(p1.toString()).thenReturn("p1");
        IoSession p2 = mock(IoSession.class);
        when(p2.toString()).thenReturn("p2");
        IoSession p3 = mock(IoSession.class);
        when(p3.toString()).thenReturn("p3");
        IoSession p4 = mock(IoSession.class);
        when(p4.toString()).thenReturn("p4");

        ts.login(p1, new User("p1"), "p1");
        ts.login(p2, new User("p2"), "p2");
        ts.login(p3, new User("p3"), "p3");
        ts.login(p4, new User("p4"), "p4");

        ts.crearTorneo(4, 10);

        ts.joinTorneo(p1); // el pedido
        ts.joinedTorneo(p1); // la confirmacion

        ts.joinTorneo(p2); // el pedido
        ts.joinedTorneo(p2); // la confirmacion

        ts.joinTorneo(p3); // el pedido
        ts.joinedTorneo(p3); // la confirmacion

        ts.joinTorneo(p4); // el pedido
        ts.joinedTorneo(p4); // la confirmacion

        verify(p1).write(any(TorneoRoomJoinedMessage.class));
        verify(p2).write(any(TorneoRoomJoinedMessage.class));
        verify(p3).write(any(TorneoRoomJoinedMessage.class));
        verify(p4).write(any(TorneoRoomJoinedMessage.class));

        // verifyNoMoreInteractions(p2, p3);

        // gano p1
        ts.finPartidoTorneo(p1);

        // p1 y p2 vuelven al lobby torneo
        ts.joinedTorneo(p1);
        ts.joinedTorneo(p2);

        // 1 ganador abandona
        ts.abandonTorneo(p1);

        // verifyNoMoreInteractions(p1);

        // gano p4
        ts.finPartidoTorneo(p4);

        // p3 y p4 vuelven al lobby torneo
        ts.joinedTorneo(p3);
        ts.joinedTorneo(p4);

        verify(p4).write(any(GanadorTorneoMessage.class));

        // verifyNoMoreInteractions(p4);
    }

    @Test
    public void testAbandonPostVictoria() {
        POSTHandler ph = mock(POSTHandler.class);
        when(ph.validateUser(any(String.class), any(String.class))).thenReturn(
                100);

        TrucoSaloon ts = new TrucoSaloon(11, ph);

        IoSession p1 = mock(IoSession.class);
        when(p1.toString()).thenReturn("p1");
        IoSession p2 = mock(IoSession.class);
        when(p2.toString()).thenReturn("p2");
        IoSession p3 = mock(IoSession.class);
        when(p3.toString()).thenReturn("p3");
        IoSession p4 = mock(IoSession.class);
        when(p4.toString()).thenReturn("p4");

        ts.login(p1, new User("p1"), "p1");
        ts.login(p2, new User("p2"), "p2");
        ts.login(p3, new User("p3"), "p3");
        ts.login(p4, new User("p4"), "p4");

        ts.crearTorneo(4, 10);

        ts.joinTorneo(p1); // el pedido
        ts.joinedTorneo(p1); // la confirmacion

        ts.joinTorneo(p2); // el pedido
        ts.joinedTorneo(p2); // la confirmacion

        ts.joinTorneo(p3); // el pedido
        ts.joinedTorneo(p3); // la confirmacion

        ts.joinTorneo(p4); // el pedido
        ts.joinedTorneo(p4); // la confirmacion

        // verifyNoMoreInteractions(p2, p3);

        // gano p1
        ts.finPartidoTorneo(p1);

        // p1 abandona justo justo
        ts.removePlayerFromRoom(p1);

        // p2 vuelve al torneo
        ts.joinedTorneo(p2);

        // verifyNoMoreInteractions(p1);

        // gano p4
        ts.finPartidoTorneo(p4);

        // p3 y p4 vuelven al lobby torneo
        ts.joinedTorneo(p3);
        ts.joinedTorneo(p4);
    }

    @Test
    public void testAbandonJustoPreStart() {
        POSTHandler ph = mock(POSTHandler.class);
        when(ph.validateUser(any(String.class), any(String.class))).thenReturn(
                100);

        final TrucoSaloon ts = new TrucoSaloon(11, ph);

        final IoSession p1 = mock(IoSession.class);
        when(p1.toString()).thenReturn("p1");
        final IoSession p2 = mock(IoSession.class);
        when(p2.toString()).thenReturn("p2");
        final IoSession p3 = mock(IoSession.class);
        when(p3.toString()).thenReturn("p3");
        final IoSession p4 = mock(IoSession.class);
        when(p4.toString()).thenReturn("p4");

        ts.login(p1, new User("p1"), "p1");
        ts.login(p2, new User("p2"), "p2");
        ts.login(p3, new User("p3"), "p3");
        ts.login(p4, new User("p4"), "p4");

        ts.crearTorneo(4, 10);

        ts.joinTorneo(p1); // el pedido
        ts.joinedTorneo(p1); // la confirmacion

        ts.joinTorneo(p2); // el pedido
        ts.joinedTorneo(p2); // la confirmacion

        ts.joinTorneo(p3); // el pedido
        ts.joinedTorneo(p3); // la confirmacion

        ts.joinTorneo(p4); // el pedido

        ts.joinedTorneo(p4); // la confirmacion

        // cliquea "salir" en TorneoScene y el mensaje llega después que se
        // armaron las salas pero antes que entre a la sala
        // abandonTorneo en vez de playerLeftRoom
        ts.abandonTorneo(p1);
        ts.abandonTorneo(p2);

        ts.finPartidoTorneo(p3);

        // p3 debe ser el campeon ya que no tiene oponente

        // casos a testear
        // - abandona y el otro esta en sala
        // - abandona y el otro no esta en sala
        // - ambos abandonan
    }

    // FIXME puede pasar que abandonen los dos de la misma llave

    @Test
    public void testAbandonInGameAntesQueGaneSuLlave() {
        POSTHandler ph = mock(POSTHandler.class);
        when(ph.validateUser(any(String.class), any(String.class))).thenReturn(
                100);

        TrucoSaloon ts = new TrucoSaloon(11, ph);

        IoSession p1 = mock(IoSession.class);
        when(p1.toString()).thenReturn("p1");
        IoSession p2 = mock(IoSession.class);
        when(p2.toString()).thenReturn("p2");
        IoSession p3 = mock(IoSession.class);
        when(p3.toString()).thenReturn("p3");
        IoSession p4 = mock(IoSession.class);
        when(p4.toString()).thenReturn("p4");

        ts.login(p1, new User("p1"), "p1");
        ts.login(p2, new User("p2"), "p2");
        ts.login(p3, new User("p3"), "p3");
        ts.login(p4, new User("p4"), "p4");

        ts.crearTorneo(4, 10);

        ts.joinTorneo(p1); // el pedido
        ts.joinedTorneo(p1); // la confirmacion

        ts.joinTorneo(p2); // el pedido
        ts.joinedTorneo(p2); // la confirmacion

        ts.joinTorneo(p3); // el pedido
        ts.joinedTorneo(p3); // la confirmacion

        ts.joinTorneo(p4); // el pedido
        ts.joinedTorneo(p4); // la confirmacion

        // gano p1
        ts.finPartidoTorneo(p1);
        ts.joinedTorneo(p1);
        ts.joinedTorneo(p2);

        ts.finPartidoTorneo(p3);
        ts.joinedTorneo(p4);
        // p3 nunca vuelve
        ts.abandonGame(p3);

        // p1 campeon
    }

    @Test
    public void test2Febrero() {
        POSTHandler ph = mock(POSTHandler.class);
        when(ph.validateUser(any(String.class), any(String.class))).thenReturn(
                100);

        TrucoSaloon ts = new TrucoSaloon(11, ph);

        IoSession cigwin = mock(IoSession.class);
        when(cigwin.toString()).thenReturn("cigwin");
        IoSession BingoBongo17 = mock(IoSession.class);
        when(BingoBongo17.toString()).thenReturn("BingoBongo17");
        IoSession claudia_09 = mock(IoSession.class);
        when(claudia_09.toString()).thenReturn("claudia_09");
        IoSession eskli = mock(IoSession.class);
        when(eskli.toString()).thenReturn("eskli");
        IoSession nicobruna14 = mock(IoSession.class);
        when(nicobruna14.toString()).thenReturn("nicobruna14");
        IoSession Plochys = mock(IoSession.class);
        when(Plochys.toString()).thenReturn("Plochys");
        IoSession gab22romero = mock(IoSession.class);
        when(gab22romero.toString()).thenReturn("gab22romero");
        IoSession karetinga = mock(IoSession.class);
        when(karetinga.toString()).thenReturn("karetinga");
        IoSession Nadie_Es_Perfecto = mock(IoSession.class);
        when(Nadie_Es_Perfecto.toString()).thenReturn("Nadie_Es_Perfecto");
        IoSession diego36arg = mock(IoSession.class);
        when(diego36arg.toString()).thenReturn("diego36arg");
        IoSession santy_akdmia = mock(IoSession.class);
        when(santy_akdmia.toString()).thenReturn("santy_akdmia");
        IoSession Arrufelipe = mock(IoSession.class);
        when(Arrufelipe.toString()).thenReturn("Arrufelipe");
        IoSession Truqero22 = mock(IoSession.class);
        when(Truqero22.toString()).thenReturn("Truqero22");
        IoSession _ubu_ = mock(IoSession.class);
        when(_ubu_.toString()).thenReturn("_ubu_");
        IoSession dolorezzz = mock(IoSession.class);
        when(dolorezzz.toString()).thenReturn("dolorezzz");
        IoSession LuisDani = mock(IoSession.class);
        when(LuisDani.toString()).thenReturn("LuisDani");

        ts.login(cigwin, new User("cigwin"), "cigwin");
        ts.login(BingoBongo17, new User("BingoBongo17"), "BingoBongo17");
        ts.login(claudia_09, new User("claudia_09"), "claudia_09");
        ts.login(eskli, new User("eskli"), "eskli");
        ts.login(nicobruna14, new User("nicobruna14"), "nicobruna14");
        ts.login(Plochys, new User("Plochys"), "Plochys");
        ts.login(gab22romero, new User("gab22romero"), "gab22romero");
        ts.login(karetinga, new User("karetinga"), "karetinga");
        ts.login(Nadie_Es_Perfecto, new User("Nadie_Es_Perfecto"),
                "Nadie_Es_Perfecto");
        ts.login(diego36arg, new User("diego36arg"), "diego36arg");
        ts.login(santy_akdmia, new User("santy_akdmia"), "santy_akdmia");
        ts.login(Arrufelipe, new User("Arrufelipe"), "Arrufelipe");
        ts.login(Truqero22, new User("Truqero22"), "Truqero22");
        ts.login(_ubu_, new User("_ubu_"), "_ubu_");
        ts.login(dolorezzz, new User("dolorezzz"), "dolorezzz");
        ts.login(LuisDani, new User("LuisDani"), "LuisDani");

        ts.crearTorneo(16, 10);

        ts.joinTorneo(cigwin); // el pedido
        ts.joinedTorneo(cigwin); // la confirmacion
        ts.joinTorneo(BingoBongo17); // el pedido
        ts.joinedTorneo(BingoBongo17); // la confirmacion
        ts.joinTorneo(claudia_09); // el pedido
        ts.joinedTorneo(claudia_09); // la confirmacion
        ts.joinTorneo(eskli); // el pedido
        ts.joinedTorneo(eskli); // la confirmacion
        ts.joinTorneo(nicobruna14); // el pedido
        ts.joinedTorneo(nicobruna14); // la confirmacion
        ts.joinTorneo(Plochys); // el pedido
        ts.joinedTorneo(Plochys); // la confirmacion
        ts.joinTorneo(gab22romero); // el pedido
        ts.joinedTorneo(gab22romero); // la confirmacion
        ts.joinTorneo(karetinga); // el pedido
        ts.joinedTorneo(karetinga); // la confirmacion
        ts.joinTorneo(Nadie_Es_Perfecto); // el pedido
        ts.joinedTorneo(Nadie_Es_Perfecto); // la confirmacion
        ts.joinTorneo(diego36arg); // el pedido
        ts.joinedTorneo(diego36arg); // la confirmacion
        ts.joinTorneo(santy_akdmia); // el pedido
        ts.joinedTorneo(santy_akdmia); // la confirmacion
        ts.joinTorneo(Arrufelipe); // el pedido
        ts.joinedTorneo(Arrufelipe); // la confirmacion
        ts.joinTorneo(Truqero22); // el pedido
        ts.joinedTorneo(Truqero22); // la confirmacion
        ts.joinTorneo(_ubu_); // el pedido
        ts.joinedTorneo(_ubu_); // la confirmacion
        ts.joinTorneo(dolorezzz); // el pedido
        ts.joinedTorneo(dolorezzz); // la confirmacion
        ts.joinTorneo(LuisDani); // el pedido
        ts.joinedTorneo(LuisDani); // la confirmacion

        ts.finPartidoTorneo(dolorezzz);
        ts.joinedTorneo(LuisDani);
        ts.joinedTorneo(dolorezzz);
        ts.abandonTorneo(LuisDani);
        ts.finPartidoTorneo(Plochys);
        ts.joinedTorneo(nicobruna14);
        ts.joinedTorneo(Plochys);
        ts.abandonTorneo(nicobruna14);
        ts.finPartidoTorneo(_ubu_);
        ts.finPartidoTorneo(claudia_09);
        ts.joinedTorneo(_ubu_);
        ts.joinedTorneo(Truqero22);
        ts.finPartidoTorneo(BingoBongo17);
        ts.joinedTorneo(claudia_09);
        ts.joinedTorneo(eskli);
        ts.joinedTorneo(BingoBongo17);
        ts.joinedTorneo(cigwin);
        ts.abandonTorneo(Truqero22);
        ts.abandonTorneo(cigwin);
        ts.abandonTorneo(eskli);
        ts.finPartidoTorneo(Arrufelipe);
        ts.finPartidoTorneo(Nadie_Es_Perfecto);
        ts.joinedTorneo(santy_akdmia);
        ts.joinedTorneo(Arrufelipe);
        ts.joinedTorneo(Nadie_Es_Perfecto);
        ts.joinedTorneo(diego36arg);
        ts.abandonTorneo(diego36arg);
        ts.abandonTorneo(santy_akdmia);
        // Torneo - gab22romero abandono la partida
        ts.playerLeftRoom(gab22romero);
        ts.finPartidoTorneo(dolorezzz);
        ts.joinedTorneo(_ubu_);
        ts.joinedTorneo(dolorezzz);
        // 10:54:03,896| - Torneo - null abandono el torneo (3)
        ts.abandonGame(karetinga);
        // 10:54:03,896| - Torneo - Plochys gano la partida
        ts.finPartidoTorneo(Nadie_Es_Perfecto);
        ts.joinedTorneo(Nadie_Es_Perfecto);
        ts.joinedTorneo(Arrufelipe);
        ts.finPartidoTorneo(BingoBongo17);
        ts.finPartidoTorneo(dolorezzz);
        ts.joinedTorneo(BingoBongo17);
        ts.joinedTorneo(claudia_09);
        ts.joinedTorneo(Nadie_Es_Perfecto);
        ts.joinedTorneo(dolorezzz);
        ts.abandonTorneo(Nadie_Es_Perfecto);
        ts.abandonTorneo(Arrufelipe);
        ts.abandonTorneo(_ubu_);
        ts.abandonTorneo(Plochys); // Torneo - Plochys abandono el torneo (3)
        ts.abandonTorneo(claudia_09);
        ts.abandonTorneo(BingoBongo17);

        // 11:18:06,746| - Torneo - dolorezzz gano la partida
        // 11:18:06,746| - Torneo - dolorezzz es el campeon
        // 11:18:51,871| - -> AbandonTorneo (dolorezzz)
        ts.abandonTorneo(dolorezzz);
    }
}