package truco.common.messages;

import truco.common.game.AlMazoMessage;
import truco.common.game.CartaMessage;
import truco.common.game.ContraFlorMessage;
import truco.common.game.EnvidoMessage;
import truco.common.game.FaltaEnvidoMessage;
import truco.common.game.FinJuegoMessage;
import truco.common.game.FinManoMessage;
import truco.common.game.FlorMessage;
import truco.common.game.HastaAquiLlegoElOlorMessage;
import truco.common.game.NoQuieroMessage;
import truco.common.game.ProximaManoMessage;
import truco.common.game.PuntosEnvidoMessage;
import truco.common.game.PuntosFlorMessage;
import truco.common.game.QuieroMessage;
import truco.common.game.RealEnvidoMessage;
import truco.common.game.RetrucoMessage;
import truco.common.game.StartManoMessage;
import truco.common.game.TrucoMessage;
import truco.common.game.Vale4Message;
import truco.common.messages.client.AbandonTorneoMessage;
import truco.common.messages.client.CreateTrucoRoomMessage;
import truco.common.messages.server.CampeonTorneoMessage;
import truco.common.messages.server.GanadorTorneoMessage;
import truco.common.messages.server.InfoTorneoMessage;
import truco.common.messages.server.NewPartidoTorneoMessage;
import truco.common.messages.server.PartidosTorneoMessage;
import truco.common.messages.server.TorneoStatusMessage;
import truco.common.messages.server.TorneoRoomJoinedMessage;
import truco.common.messages.server.TuTurnoMessage;
import truco.common.messages.server.UpdatePartidoTorneoMessage;

import common.messages.TaringaProtocolDecoder;

public class TrucoProtocolDecoder extends TaringaProtocolDecoder {

    public TrucoProtocolDecoder() {
        classes.put(new AlMazoMessage().getMessageId(), AlMazoMessage.class);
        classes.put(new EnvidoMessage().getMessageId(), EnvidoMessage.class);
        classes.put(new RealEnvidoMessage().getMessageId(),
                RealEnvidoMessage.class);
        classes.put(new FaltaEnvidoMessage().getMessageId(),
                FaltaEnvidoMessage.class);
        classes
                .put(new FinJuegoMessage().getMessageId(),
                        FinJuegoMessage.class);
        classes.put(new FinManoMessage().getMessageId(), FinManoMessage.class);
        classes.put(new QuieroMessage().getMessageId(), QuieroMessage.class);
        classes
                .put(new NoQuieroMessage().getMessageId(),
                        NoQuieroMessage.class);
        classes.put(new ProximaManoMessage().getMessageId(),
                ProximaManoMessage.class);
        classes.put(new PuntosEnvidoMessage().getMessageId(),
                PuntosEnvidoMessage.class);
        classes.put(new TrucoMessage().getMessageId(), TrucoMessage.class);
        classes.put(new RetrucoMessage().getMessageId(), RetrucoMessage.class);
        classes.put(new Vale4Message().getMessageId(), Vale4Message.class);
        classes.put(new StartManoMessage().getMessageId(),
                StartManoMessage.class);
        classes.put(new CartaMessage().getMessageId(), CartaMessage.class);
        classes.put(new CreateTrucoRoomMessage().getMessageId(),
                CreateTrucoRoomMessage.class);
        classes.put(new FlorMessage().getMessageId(), FlorMessage.class);
        classes.put(new HastaAquiLlegoElOlorMessage().getMessageId(),
                HastaAquiLlegoElOlorMessage.class);
        classes.put(new PuntosFlorMessage().getMessageId(),
                PuntosFlorMessage.class);
        classes.put(new ContraFlorMessage().getMessageId(),
                ContraFlorMessage.class);

        // torneos
        classes.put(new TuTurnoMessage().getMessageId(), TuTurnoMessage.class);
        classes.put(new InfoTorneoMessage().getMessageId(),
                InfoTorneoMessage.class);
        classes.put(new JoinTorneoMessage().getMessageId(),
                JoinTorneoMessage.class);
        classes.put(new TorneoStatusMessage().getMessageId(),
                TorneoStatusMessage.class);
        classes.put(new TorneoJoinedMessage().getMessageId(),
                TorneoJoinedMessage.class);
        classes.put(new TorneoRoomJoinedMessage().getMessageId(),
                TorneoRoomJoinedMessage.class);
        classes.put(new GanadorTorneoMessage().getMessageId(),
                GanadorTorneoMessage.class);
        classes.put(new AbandonTorneoMessage().getMessageId(),
                AbandonTorneoMessage.class);
        classes.put(new TorneoChatMessage().getMessageId(),
                TorneoChatMessage.class);
        classes.put(new NewPartidoTorneoMessage().getMessageId(),
                NewPartidoTorneoMessage.class);
        classes.put(new UpdatePartidoTorneoMessage().getMessageId(),
                UpdatePartidoTorneoMessage.class);
        classes.put(new PartidosTorneoMessage().getMessageId(),
                PartidosTorneoMessage.class);
        classes.put(new CampeonTorneoMessage().getMessageId(),
                CampeonTorneoMessage.class);
        classes.put(new AbandonedTorneoMessage().getMessageId(),
                AbandonedTorneoMessage.class);
    }
}
