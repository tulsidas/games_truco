package truco.client;

import java.util.Random;

import pulpcore.CoreSystem;
import pulpcore.Input;
import pulpcore.Stage;
import pulpcore.animation.Easing;
import pulpcore.animation.Timeline;
import pulpcore.animation.event.TimelineEvent;
import pulpcore.image.Colors;
import pulpcore.image.CoreFont;
import pulpcore.image.CoreImage;
import pulpcore.math.CoreMath;
import pulpcore.scene.Scene;
import pulpcore.sound.Sound;
import pulpcore.sprite.Button;
import pulpcore.sprite.Group;
import pulpcore.sprite.ImageSprite;
import pulpcore.sprite.Label;
import pulpcore.sprite.Sprite;
import pulpcore.sprite.TextField;
import truco.common.game.AlMazoMessage;
import truco.common.game.CartaMessage;
import truco.common.game.ContraFlorMessage;
import truco.common.game.EnvidoMessage;
import truco.common.game.FaltaEnvidoMessage;
import truco.common.game.FlorMessage;
import truco.common.game.HastaAquiLlegoElOlorMessage;
import truco.common.game.NoQuieroMessage;
import truco.common.game.ProximaManoMessage;
import truco.common.game.QuieroMessage;
import truco.common.game.RealEnvidoMessage;
import truco.common.game.RetrucoMessage;
import truco.common.game.TrucoMessage;
import truco.common.game.Vale4Message;
import truco.common.ifaz.GameHandler;
import truco.common.model.Carta;
import truco.common.model.Mano;
import truco.common.model.Numero;
import truco.common.model.Palo;
import truco.common.model.TrucoRoom;
import client.DisconnectedScene;
import client.InGameChatArea;
import client.PingScene;
import client.PulpcoreUtils;
import client.DisconnectedScene.Reason;

import common.game.AbandonRoomMessage;
import common.game.ProximoJuegoMessage;
import common.messages.chat.RoomChatMessage;
import common.messages.server.RoomJoinedMessage;
import common.model.AbstractRoom;
import common.model.User;

public class TrucoScene extends PingScene implements GameHandler {

    // CONSTANTES
    private static final int BOTON_X_IN = 325;

    private static final int BOTON_X_OUT = 800;

    private static final int BOTON_Y = 140;

    private static final int[] RANDOM_COLORS = new int[] { 0x00e300, 0x00bff6,
            0x2bc5ad, 0xff0099 };

    // VARIABLES
    private int colorYo, colorOtro;

    private GameConnector connection;

    private TrucoRoom room;

    private InGameChatArea chatArea;

    private TextField chatTF;

    private Button sendChat, abandonGame, disableSounds;

    private User currentUser, oponente;

    private boolean soyMano;

    // me toca jugar o responder
    private boolean miTurno;

    private boolean heDeTirar;

    // debo responder un canto, no puedo tirar
    private boolean heDeResponder;

    // si el quiero/noquiero es para el envido, la flor o el truco
    private boolean envidoEnJuego, contraFlorEnJuego;

    // se puede cantar hasta 3 envidos
    private int envidosCantados;

    private boolean mustDisconnect;

    private boolean primeraMano, tireCarta, seJugoTanto, seJugoFlor;

    // si estoy jugando torneo
    private boolean torneo;

    private int fichasEnJuego;

    // mano actual
    private Mano mano;

    private Button truco, retruco, vale4, quiero, noquiero, envido, real,
            falta, alMazo, repartir, flor, florno, contraflor;

    private ImageSprite imgTruco, imgRetruco, imgVale4, imgQuiero, imgNoQuiero,
            imgEnvido, imgReal, imgFalta, imgFlor, imgFlorNo, imgContraFlor,
            imgAlMazo;

    private Button nuevoJuegoSi, nuevoJuegoNo;

    private Button carta1, carta2, carta3;

    private ImageSprite cartaOtro1, cartaOtro2, cartaOtro3;

    private CoreImage[] cartas;

    // lo que falta para que me rajen
    private int tiempoRestante;

    // el momento en que tengo que abandonar
    private long timeToGo;

    // la posicion X donde va la carta que tiro
    private int posX;

    private int fichasYo, fichasOtro;

    private Label turno;

    private Label puntajeYoLbl, puntajeOtroLbl;

    private Label oponenteLabel, diceLabel, cantoLabel;

    private Label finalLabel, finalLabel2;

    private Label timerLabel;

    // SFX
    private Sound[] teToca;

    private Sound haha, beep;

    public TrucoScene(GameConnector connection, User currentUser, TrucoRoom room) {
        this(connection, currentUser, room, false);
    }

    public TrucoScene(GameConnector connection, User currentUser,
            TrucoRoom room, boolean torneo) {
        super(connection);

        this.connection = connection;
        this.room = room;
        this.currentUser = currentUser;
        this.miTurno = false;
        this.mustDisconnect = true;
        this.torneo = torneo;

        fichasYo = 0;
        fichasOtro = 0;

        // inject
        this.connection.setGameHandler(this);
    }

    public void load() {
        // fondo
        add(new ImageSprite(CoreImage.load("imgs/bg-truco.png"), 0, 0));

        CoreFont din13 = CoreFont.load("imgs/DIN13.font.png");
        CoreFont din24 = CoreFont.load("imgs/DIN24.font.png")
                .tint(Colors.WHITE);
        CoreFont din18 = CoreFont.load("imgs/DIN18.font.png")
                .tint(Colors.WHITE);
        CoreFont din13white = din13.tint(Colors.WHITE);
        CoreFont din30 = CoreFont.load("imgs/DIN30.font.png")
                .tint(Colors.WHITE);

        if (torneo) {
            Label t1 = new Label(din18.tint(Colors.BLACK), "partido de torneo",
                    302, 4);
            Label t2 = new Label(din18, "partido de torneo", 320, 3);

            // animo los labels torneisticos
            Timeline tl = new Timeline();
            int dur = 5000;
            tl.animate(t1.x, 282, 332, dur, Easing.REGULAR_IN_OUT, 0);
            tl.animate(t1.x, 332, 282, dur, Easing.REGULAR_IN_OUT, dur);
            tl.animate(t2.x, 284, 334, dur, Easing.REGULAR_IN_OUT, 0);
            tl.animate(t2.x, 334, 284, dur, Easing.REGULAR_IN_OUT, dur);
            tl.loopForever();
            addTimeline(tl);

            add(t1);
            add(t2);
        }

        // chat box
        chatArea = new InGameChatArea(din13, 465, 221, 244, 185);
        add(chatArea);

        // campo de texto donde se chatea
        chatTF = new TextField(din13, din13white, "", 464, 420, 228, -1);
        chatTF.setMaxNumChars(200);
        add(chatTF);

        // boton para enviar el chat (asociado al ENTER)
        sendChat = new Button(CoreImage.load("imgs/send-petit.png").split(3),
                700, 420);
        sendChat.setKeyBinding(Input.KEY_ENTER);
        add(sendChat);

        abandonGame = new Button(CoreImage.load("imgs/btn-abandonar.png")
                .split(3), 484, 0);
        add(abandonGame);

        disableSounds = new Button(CoreImage.load("imgs/sonidos.png").split(6),
                2, 38, true);
        disableSounds.setSelected(CoreSystem.isMute());
        add(disableSounds);

        // mi color
        colorYo = RANDOM_COLORS[(int) (Math.random() * RANDOM_COLORS.length)];

        turno = new Label(din18, "Esperando oponente", 305, 25);

        finalLabel = new Label(din30, "", 0, 180);
        finalLabel.visible.set(false);
        add(finalLabel);
        finalLabel2 = new Label(din30, "", 0, 210);
        finalLabel2.visible.set(false);
        add(finalLabel2);

        // animo el alpha para que titile
        Timeline alphaCycle = new Timeline();
        int dur = 1000;
        alphaCycle.animate(turno.alpha, 255, 0, dur, Easing.NONE, 0);
        alphaCycle.animate(turno.alpha, 0, 255, dur, Easing.NONE, dur);
        alphaCycle.loopForever();
        addTimeline(alphaCycle);
        add(turno);

        // label con datos de la sala
        String pts = (torneo ? "" : "por " + room.getPuntosApostados()
                + " fichas, ")
                + "a "
                + room.getPuntajeJuego()
                + ", "
                + (room.isConFlor() ? "con" : "sin") + " flor";
        add(new Label(din13, pts, 132, 44));
        add(new Label(din13white, pts, 130, 42));

        // botones
        truco = new Button(CoreImage.load("imgs/btn-truco.png").split(3),
                BOTON_X_OUT, 0);
        truco.setPixelLevelChecks(false);
        add(truco);

        retruco = new Button(CoreImage.load("imgs/btn-retruco.png").split(3),
                BOTON_X_OUT, 0);
        retruco.setPixelLevelChecks(false);
        retruco.enabled.set(false);
        retruco.alpha.set(0);
        add(retruco);

        vale4 = new Button(CoreImage.load("imgs/btn-valecuatro.png").split(3),
                BOTON_X_OUT, 0);
        vale4.setPixelLevelChecks(false);
        vale4.enabled.set(false);
        vale4.alpha.set(0);
        add(vale4);

        quiero = new Button(CoreImage.load("imgs/btn-quiero.png").split(3),
                BOTON_X_OUT, 0);
        quiero.setPixelLevelChecks(false);
        quiero.enabled.set(false);
        quiero.alpha.set(0);
        add(quiero);

        noquiero = new Button(CoreImage.load("imgs/btn-noquiero.png").split(3),
                BOTON_X_OUT, 0);
        noquiero.setPixelLevelChecks(false);
        noquiero.enabled.set(false);
        noquiero.alpha.set(0);
        add(noquiero);

        envido = new Button(CoreImage.load("imgs/btn-envido.png").split(3),
                BOTON_X_OUT, 0);
        envido.setPixelLevelChecks(false);
        add(envido);

        flor = new Button(CoreImage.load("imgs/btn-flor-t.png").split(3),
                BOTON_X_OUT, 0);
        flor.setPixelLevelChecks(false);
        flor.enabled.set(false);
        flor.alpha.set(0);
        add(flor);

        florno = new Button(CoreImage.load("imgs/bt-flor-no-t.png").split(3),
                BOTON_X_OUT, 0);
        florno.setPixelLevelChecks(false);
        florno.enabled.set(false);
        florno.alpha.set(0);
        add(florno);

        contraflor = new Button(CoreImage.load("imgs/btn-contra-flor-t.png")
                .split(3), BOTON_X_OUT, 0);
        contraflor.setPixelLevelChecks(false);
        contraflor.enabled.set(false);
        contraflor.alpha.set(0);
        add(contraflor);

        real = new Button(CoreImage.load("imgs/btn-realenvido.png").split(3),
                BOTON_X_OUT, 0);
        real.setPixelLevelChecks(false);
        real.enabled.set(false);
        real.alpha.set(0);
        add(real);

        falta = new Button(CoreImage.load("imgs/btn-faltaenvido.png").split(3),
                BOTON_X_OUT, 0);
        falta.setPixelLevelChecks(false);
        falta.enabled.set(false);
        falta.alpha.set(0);
        add(falta);

        alMazo = new Button(
                CoreImage.load("imgs/btn-mevoyalmazo.png").split(3),
                BOTON_X_OUT, 0);
        alMazo.setPixelLevelChecks(false);
        add(alMazo);

        repartir = new Button(CoreImage.load("imgs/btn-repartir.png").split(3),
                BOTON_X_OUT, 0);
        repartir.setPixelLevelChecks(false);
        repartir.enabled.set(false);
        repartir.alpha.set(0);
        add(repartir);

        nuevoJuegoSi = new Button(CoreImage.load("imgs/btn-si.png").split(3),
                300, 300);
        nuevoJuegoSi.setPixelLevelChecks(false);
        nuevoJuegoSi.enabled.set(false);
        nuevoJuegoSi.alpha.set(0);
        add(nuevoJuegoSi);

        nuevoJuegoNo = new Button(CoreImage.load("imgs/btn-no.png").split(3),
                400, 300);
        nuevoJuegoNo.setPixelLevelChecks(false);
        nuevoJuegoNo.enabled.set(false);
        nuevoJuegoNo.alpha.set(0);
        add(nuevoJuegoNo);

        // imagenes
        cartas = CoreImage.load("imgs/mazo.png").split(10, 4);

        imgTruco = new ImageSprite(CoreImage.load("imgs/btn-truco.png")
                .split(3)[2], 550, 80);
        imgTruco.visible.set(false);
        add(imgTruco);

        imgRetruco = new ImageSprite(CoreImage.load("imgs/btn-retruco.png")
                .split(3)[2], 550, 80);
        imgRetruco.visible.set(false);
        add(imgRetruco);

        imgVale4 = new ImageSprite(CoreImage.load("imgs/btn-valecuatro.png")
                .split(3)[2], 550, 80);
        imgVale4.visible.set(false);
        add(imgVale4);

        imgQuiero = new ImageSprite(CoreImage.load("imgs/btn-quiero.png")
                .split(3)[2], 550, 80);
        imgQuiero.visible.set(false);
        add(imgQuiero);

        imgNoQuiero = new ImageSprite(CoreImage.load("imgs/btn-noquiero.png")
                .split(3)[2], 550, 80);
        imgNoQuiero.visible.set(false);
        add(imgNoQuiero);

        imgEnvido = new ImageSprite(CoreImage.load("imgs/btn-envido.png")
                .split(3)[2], 550, 80);
        imgEnvido.visible.set(false);
        add(imgEnvido);

        imgReal = new ImageSprite(CoreImage.load("imgs/btn-realenvido.png")
                .split(3)[2], 550, 80);
        imgReal.visible.set(false);
        add(imgReal);

        imgFalta = new ImageSprite(CoreImage.load("imgs/btn-faltaenvido.png")
                .split(3)[2], 550, 80);
        imgFalta.visible.set(false);
        add(imgFalta);

        imgFlor = new ImageSprite(CoreImage.load("imgs/btn-flor-t.png")
                .split(3)[2], 550, 80);
        imgFlor.visible.set(false);
        add(imgFlor);

        imgContraFlor = new ImageSprite(CoreImage.load(
                "imgs/btn-contra-flor-t.png").split(3)[2], 550, 80);
        imgContraFlor.visible.set(false);
        add(imgContraFlor);

        imgFlorNo = new ImageSprite(CoreImage.load("imgs/bt-flor-no-t.png")
                .split(3)[2], 550, 80);
        imgFlorNo.visible.set(false);
        add(imgFlorNo);

        imgAlMazo = new ImageSprite(CoreImage.load("imgs/btn-mevoyalmazo.png")
                .split(3)[2], 550, 80);
        imgAlMazo.visible.set(false);
        add(imgAlMazo);

        // dummy labels
        puntajeYoLbl = new Label("", 0, 0);
        puntajeOtroLbl = new Label("", 0, 0);
        oponenteLabel = new Label("", 0, 0);

        diceLabel = new Label(din24.tint(0xffffff), "dice", 0, 75);
        diceLabel.x.set(570 - diceLabel.width.getAsInt() - 10);
        diceLabel.visible.set(false);
        add(diceLabel);

        cantoLabel = new Label(din24.tint(0xffffff), "", 570, 75);
        cantoLabel.visible.set(false);
        add(cantoLabel);

        // timer (en un nuevo layer para estar encima de las cartas)
        timerLabel = new Label(din13white, "", 0, 0);
        Group g = new Group();
        g.add(timerLabel);
        addLayer(g);

        teToca = new Sound[7];
        for (int i = 1; i < 8; i++) {
            teToca[i - 1] = Sound.load("sfx/s" + i + ".wav");
        }

        haha = Sound.load("sfx/haha.wav");
        beep = Sound.load("sfx/beep.wav");

        // el otro (si hay)
        for (User otro : room.getPlayers()) {
            if (!otro.equals(currentUser)) {
                oponente = otro;
                drawNames();
                break;
            }
        }

        // envio mensaje que me uni a la sala correctamente
        connection.send(new RoomJoinedMessage());
    }

    public void unload() {
        if (mustDisconnect) {
            connection.disconnect();
        }
    }

    public void update(int elapsedTime) {
        super.update(elapsedTime); // PingScene

        if (sendChat.isClicked() && chatTF.getText().trim().length() > 0) {
            String txt = chatTF.getText();
            connection.send(new RoomChatMessage(txt));
            chatArea.addLine(currentUser.getName() + ": " + txt);

            chatTF.setText("");
        }
        else if (repartir.isClicked()) {
            // saco el boton
            disable(repartir);

            connection.send(new ProximaManoMessage());

            // no es mi turno hasta que llegue el mensaje del server
            setMiTurno(false);
        }
        else if (disableSounds.isClicked()) {
            CoreSystem.setMute(disableSounds.isSelected());
        }
        else if (nuevoJuegoSi.enabled.get() && nuevoJuegoSi.isClicked()) {
            disable(nuevoJuegoSi, nuevoJuegoNo);

            finalLabel.setText("Esperando respuesta del oponente...");
            finalLabel.visible.set(true);
            finalLabel2.visible.set(false);
            PulpcoreUtils.centerSprite(finalLabel, 235, 319);

            fichasYo = fichasOtro = 0;
            updatePuntaje();

            connection.send(new ProximoJuegoMessage(true));
        }
        else if (nuevoJuegoNo.enabled.get() && nuevoJuegoNo.isClicked()) {
            // aviso que no
            connection.send(new ProximoJuegoMessage(false));

            invokeLater(new Runnable() {
                public void run() {
                    // y me rajo al lobby
                    setScene(new LobbyScene(currentUser, connection));
                }
            });
        }
        else if (abandonGame.enabled.get() && abandonGame.isClicked()) {
            abandonGame();
        }

        if (miTurno) {
            timeToGo -= elapsedTime;
            // actualizacion del timer
            int t = Math.round(timeToGo / 1000);

            if (t < 0) {
                abandonGame();
            }
            else if (t != tiempoRestante) {
                tiempoRestante = t;

                timerLabel.setText(Integer.toString(t));
                timerLabel.alpha.set(0xff);

                if (t >= 10) {
                    timerLabel.x.set(268);
                    timerLabel.y.set(25);
                }
                else if (t < 10) {
                    timerLabel.x.set(180);
                    timerLabel.y.set(180);

                    beep.play();

                    timerLabel.alpha.animateTo(0, 500);
                    timerLabel.width.animateTo(100, 500);
                    timerLabel.height.animateTo(100, 500);
                    timerLabel.x.animateTo(timerLabel.x.get() - 50, 500);
                    timerLabel.y.animateTo(timerLabel.y.get() - 50, 500);
                }
            }

            if (truco.isClicked()) {
                connection.send(new TrucoMessage());
                envidoEnJuego = false;
                contraFlorEnJuego = false;
                disable(envido, real, falta, truco);
                setMiTurno(false);
                setCanto(null);
            }
            else if (retruco.isClicked()) {
                connection.send(new RetrucoMessage());
                disable(retruco, envido, real, falta, flor, florno);
                setMiTurno(false);
                setCanto(null);
            }
            else if (vale4.isClicked()) {
                connection.send(new Vale4Message());
                disable(vale4);
                setMiTurno(false);
                setCanto(null);
            }
            else if (envido.isClicked()) {
                connection.send(new EnvidoMessage());

                disable(retruco);

                envidosCantados++;

                setMiTurno(false);
                envidoEnJuego = true;
                seJugoTanto = true;
                setCanto(null);
            }
            else if (real.isClicked()) {
                connection.send(new RealEnvidoMessage());
                envidoEnJuego = true;
                seJugoTanto = true;

                disable(retruco);

                envidosCantados++;

                setMiTurno(false);
                setCanto(null);
            }
            else if (falta.isClicked()) {
                connection.send(new FaltaEnvidoMessage());
                envidoEnJuego = true;
                seJugoTanto = true;

                disable(envido, real, falta, retruco);

                setMiTurno(false);
                setCanto(null);
            }
            else if (flor.isClicked()) {
                connection.send(new FlorMessage());
                envidoEnJuego = false;
                seJugoFlor = true;

                setHeDeResponder(false);

                disable(envido, real, falta);

                setMiTurno(false);
                setCanto(null);
            }
            else if (florno.isClicked()) {
                connection.send(new HastaAquiLlegoElOlorMessage());
                setHeDeResponder(false);
                seJugoFlor = true;

                disable(envido, real, falta, flor, florno, contraflor, quiero,
                        noquiero);
                enable(truco);

                // los puntos al otro por no haber querido el tanto
                fichasOtro += 3;
                updatePuntaje();

                // si me toca tirar, mantengo el turno, sino no
                setMiTurno(heDeTirar);
                setCanto(null);
            }
            else if (contraflor.isClicked()) {
                connection.send(new ContraFlorMessage());
                envidoEnJuego = false;
                contraFlorEnJuego = true;
                seJugoFlor = true;

                setHeDeResponder(false);

                disable(envido, real, falta, flor, florno);

                setMiTurno(false);
                setCanto(null);
            }
            else if (alMazo.isClicked()) {
                setMiTurno(false);
                setCanto(null);

                connection.send(new AlMazoMessage());
            }
            else if (quiero.isClicked()) {
                connection.send(new QuieroMessage());
                setHeDeResponder(false);

                disable(quiero, noquiero);
                if (envidoEnJuego) {
                    seJugoTanto = true;
                }
                else {
                    // deshabilito el tanto y la flor si quise truco
                    disable(envido, real, falta, flor);
                }

                // si me toca tirar, mantengo el turno, sino no
                setMiTurno(heDeTirar);
                setCanto(null);
            }
            else if (noquiero.isClicked()) {
                connection.send(new NoQuieroMessage());
                setHeDeResponder(false);

                if (envidoEnJuego) {
                    seJugoTanto = true;

                    disable(envido, real, falta, flor, florno, contraflor,
                            quiero, noquiero);
                    enable(truco);

                    // los puntos al otro por no haber querido el tanto
                    fichasOtro += fichasEnJuego;
                    updatePuntaje();
                }
                else if (contraFlorEnJuego) {
                    disable(envido, real, falta, flor, florno, contraflor,
                            quiero, noquiero);
                    enable(truco);

                    // los puntos al otro por no haber querido la contraflor
                    fichasOtro += 6;
                    updatePuntaje();
                }

                // si me toca tirar, mantengo el turno, sino no
                setMiTurno(heDeTirar);
                setCanto(null);
            }

            // si me toca tirar y no tengo que responder ningun canto
            if (heDeTirar && !heDeResponder) {
                if ((carta1.enabled.get() && carta1.isClicked())
                        || (carta2.enabled.get() && carta2.isClicked())
                        || (carta3.enabled.get() && carta3.isClicked())) {
                    // estoy tirando una carta

                    Button tacar = null;
                    Carta c = null;

                    if (carta1.enabled.get() && carta1.isClicked()) {
                        c = mano.getCarta1();
                        tacar = carta1;
                    }
                    else if (carta2.enabled.get() && carta2.isClicked()) {
                        c = mano.getCarta2();
                        tacar = carta2;
                    }
                    else if (carta3.enabled.get() && carta3.isClicked()) {
                        c = mano.getCarta3();
                        tacar = carta3;
                    }

                    setMiTurno(false);
                    setHeDeTirar(false);

                    // si el otro ya tiró, se acaba la 1ra mano
                    tireCarta = true;
                    primeraMano = cartaOtro1 == null;

                    tacar.enabled.set(false);
                    tacar.y.animateTo(194, 300);
                    tacar.x.animateTo(posX, 300);
                    posX += 100;

                    disable(envido, real, falta, flor);

                    connection.send(new CartaMessage(c));
                }
            }
        }
    }

    public void roomJoined(AbstractRoom room, final User user) {
        invokeLater(new Runnable() {
            public void run() {
                if (!user.equals(currentUser)) {
                    oponente = user;
                    drawNames();
                }
            }
        });
    }

    public void incomingChat(final User from, final String msg) {
        invokeLater(new Runnable() {
            public void run() {
                chatArea.addLine(from.getName() + ": " + msg);
            }
        });
    }

    private void setMiTurno(boolean miTurno) {
        if (miTurno) {
            turno.setText("Te toca");

            if (!this.miTurno) {
                // no era mi turno y ahora es

                teToca[(int) (Math.random() * teToca.length)].play();
            }

            // en 30s abandonamos
            timeToGo = 30 * 1000;
        }
        else {
            turno.setText("Esperando jugada");
        }

        moveButtons(miTurno);
        this.miTurno = miTurno;
    }

    private void setHeDeTirar(boolean heDeTirar) {
        this.heDeTirar = heDeTirar;
    }

    private void setHeDeResponder(boolean heDeResponder) {
        this.heDeResponder = heDeResponder;
    }

    /***************************************************************************
     * GameHandler
     **************************************************************************/
    public void startMano(final Mano mano, final boolean sosMano) {
        invokeLater(new Runnable() {
            public void run() {
                TrucoScene.this.mano = mano;
                TrucoScene.this.soyMano = sosMano;

                // guardo aca los valores por si el runnable de abajo es llamado
                // después (tiene un delay) de que otro hilo los modifique
                TrucoScene.this.miTurno = sosMano;
                TrucoScene.this.heDeTirar = sosMano;
                TrucoScene.this.heDeResponder = false;

                // visibilizo y habilito el boton de abandonar y label turno
                abandonGame.visible.set(true);
                abandonGame.enabled.set(true);
                turno.visible.set(true);

                posX = 28;

                primeraMano = true;
                tireCarta = false;
                seJugoTanto = false;
                seJugoFlor = false;

                enable(truco, envido, real, falta, alMazo);

                if (room.isConFlor() && mano.tieneFlor()) {
                    enable(flor);
                }

                setCanto(null);

                // saco cartas viejas (si habia)
                sacarCartas();

                // saco el mensaje final
                finalLabel.visible.set(false);
                finalLabel2.visible.set(false);

                int[] xy = getRandomOutside();
                CoreImage img1 = getCartaImage(mano.getCarta1());
                carta1 = new Button(new CoreImage[] { img1, img1, img1 },
                        xy[0], xy[1]);
                carta1.angle.setAsFixed(CoreMath.rand(0, CoreMath.TWO_PI));
                add(carta1);

                xy = getRandomOutside();
                CoreImage img2 = getCartaImage(mano.getCarta2());
                carta2 = new Button(new CoreImage[] { img2, img2, img2 },
                        xy[0], xy[1]);
                carta2.angle.setAsFixed(CoreMath.rand(0, CoreMath.TWO_PI));
                add(carta2);

                xy = getRandomOutside();
                CoreImage img3 = getCartaImage(mano.getCarta3());
                carta3 = new Button(new CoreImage[] { img3, img3, img3 },
                        xy[0], xy[1]);
                carta3.angle.setAsFixed(CoreMath.rand(0, CoreMath.TWO_PI));
                add(carta3);

                int time = 500;

                // animo
                carta1.x.animateTo(42, time);
                carta1.y.animateTo(330, time);
                carta1.angle.animateTo(0, time);

                carta2.x.animateTo(128, time, null, time);
                carta2.y.animateTo(330, time, null, time);
                carta2.angle.animateTo(0, time, null, time);

                carta3.x.animateTo(218, time, null, time * 2);
                carta3.y.animateTo(330, time, null, time * 2);
                carta3.angle.animateTo(0, time, null, time * 2);

                addEvent(new TimelineEvent(time) {
                    @Override
                    public void run() {
                        setMiTurno(miTurno);
                        setHeDeTirar(heDeTirar);
                        setHeDeResponder(heDeResponder);

                        envidoEnJuego = false;
                        contraFlorEnJuego = false;
                        envidosCantados = 0;
                    }
                });
            }
        });
    }

    public void carta(final Carta c, final boolean teToca) {
        invokeLater(new Runnable() {
            public void run() {

                showCartaDelOtro(c);

                // si yo ya tire, se acabo la primera mano
                primeraMano = !tireCarta;

                setCanto(null);

                setMiTurno(teToca);
                setHeDeTirar(teToca);
            }
        });
    }

    public void finMano(final int yo, final int otro) {
        invokeLater(new Runnable() {
            public void run() {

                disable(truco, retruco, vale4, flor, florno, contraflor,
                        quiero, noquiero, envido, real, falta, alMazo);

                // si fui mano, me toca repartir
                if (soyMano) {
                    enable(repartir);
                }
                setMiTurno(soyMano);

                fichasYo += yo;
                fichasOtro += otro;

                updatePuntaje();

                setHeDeTirar(false);
            }
        });
    }

    public void finMano(final int puntosYo, final int puntosOtro, final Carta c) {
        invokeLater(new Runnable() {
            public void run() {
                showCartaDelOtro(c);
                finMano(puntosYo, puntosOtro);
            }
        });
    }

    public void alMazo(final int puntosGanados) {
        invokeLater(new Runnable() {
            public void run() {
                setCanto(imgAlMazo);
                finMano(puntosGanados, 0);
            }
        });
    }

    public void updatePoints(int puntos) {
        // actualizo puntos
        currentUser.setPuntos(puntos);

        invokeLater(new Runnable() {
            public void run() {
                // obligo a contestar o que vuelva al lobby
                setMiTurno(true);

                if (currentUser.getPuntos() >= room.getPuntosApostados()) {
                    enable(nuevoJuegoSi, nuevoJuegoNo);

                    finalLabel2.setText("¿Otro partido?");
                    finalLabel2.visible.set(true);
                    PulpcoreUtils.centerSprite(finalLabel2, 235, 319);
                }
                else {
                    // no me alcanza para jugar otro

                    // aviso que no
                    connection.send(new ProximoJuegoMessage(false));

                    // y me rajo al lobby
                    setScene(new LobbyScene(currentUser, connection));
                }
            }
        });
    }

    public void finJuego(final boolean victoria) {
        invokeLater(new Runnable() {
            public void run() {
                sacarCartas();
                disable(truco, retruco, vale4, quiero, noquiero, envido, real,
                        falta, alMazo, repartir);

                if (victoria) {
                    // gane
                    finalLabel.setText("¡Ganaste! ¡Capo!");
                }
                else {
                    // perdi
                    finalLabel.setText("¡Perdiste, sos un gil!");
                    haha.play();
                }

                finalLabel.visible.set(true);
                PulpcoreUtils.centerSprite(finalLabel, 235, 319);

                // invisibilizo y deshabilito el boton de abandonar
                abandonGame.visible.set(false);
                abandonGame.enabled.set(false);

                // espero a que lleguen los puntos
                setMiTurno(false);

                if (torneo) {
                    // vuelta al TorneoScene
                    addEvent(new TimelineEvent(5000) {
                        @Override
                        public void run() {
                            setScene(new TorneoScene(connection, currentUser));
                        }
                    });
                }
                else {
                    // cambio texto del cartel
                    turno.setText("Actualizando puntos");
                }
            }
        });
    }

    public void envido(final int puntosEnJuego) {
        invokeLater(new Runnable() {
            public void run() {
                envidoEnJuego = true;
                envidosCantados++;
                TrucoScene.this.fichasEnJuego = puntosEnJuego;

                setCanto(imgEnvido);

                if (room.isConFlor() && mano.tieneFlor()) {
                    enable(flor);
                }
                if (envidosCantados < 3) {
                    enable(envido, real, falta);
                }
                else {
                    disable(envido, real, falta);
                }

                enable(quiero, noquiero);
                disable(truco, retruco, vale4);

                setHeDeResponder(true);
                setMiTurno(true);
            }
        });
    }

    public void real(final int puntosEnJuego) {
        invokeLater(new Runnable() {
            public void run() {
                envidoEnJuego = true;
                envidosCantados++;
                TrucoScene.this.fichasEnJuego = puntosEnJuego;

                setCanto(imgReal);

                if (envidosCantados < 3) {
                    enable(real, falta);
                }
                else {
                    disable(real, falta);
                }
                enable(quiero, noquiero);
                disable(envido, truco, retruco, vale4);

                setHeDeResponder(true);
                setMiTurno(true);
            }
        });
    }

    public void falta(final int puntosEnJuego) {
        invokeLater(new Runnable() {
            public void run() {

                envidoEnJuego = true;
                TrucoScene.this.fichasEnJuego = puntosEnJuego;

                setCanto(imgFalta);

                enable(quiero, noquiero);
                disable(envido, real, falta, truco, retruco, vale4);

                setHeDeResponder(true);
                setMiTurno(true);
            }
        });
    }

    @Override
    public void flor() {
        invokeLater(new Runnable() {
            public void run() {

                setCanto(imgFlor);

                enable(florno);
                if (room.isConFlor() && mano.tieneFlor()) {
                    enable(flor, contraflor);
                }

                disable(envido, real, falta, truco, retruco, vale4, quiero,
                        noquiero);

                setMiTurno(true);
                setHeDeResponder(true);
            }
        });
    }

    @Override
    public void contraflor() {
        invokeLater(new Runnable() {
            public void run() {

                contraFlorEnJuego = true;

                setCanto(imgContraFlor);
                setHeDeResponder(true);

                enable(quiero, noquiero);
                disable(flor, contraflor, florno, truco, retruco, vale4);
                setMiTurno(true);
            }
        });
    }

    @Override
    public void hastaAquiLlegoElOlor() {
        invokeLater(new Runnable() {
            public void run() {

                setCanto(imgFlorNo);

                fichasYo += 3;
                updatePuntaje();

                disable(envido, real, falta, flor, florno, contraflor, quiero,
                        noquiero);
                enable(truco);
                setMiTurno(heDeTirar);
            }
        });
    }

    public void noquiero(final int puntosGanados) {
        invokeLater(new Runnable() {
            public void run() {
                setCanto(imgNoQuiero);
                setHeDeResponder(false);

                if (envidoEnJuego) {
                    fichasYo += puntosGanados;
                    updatePuntaje();

                    disable(envido, real, falta, flor, quiero, noquiero);
                    enable(truco);
                    setMiTurno(heDeTirar);
                }
                else if (contraFlorEnJuego) {
                    fichasYo += 6;
                    updatePuntaje();

                    disable(envido, real, falta, flor, contraflor, florno,
                            quiero, noquiero);
                    enable(truco);
                    setMiTurno(heDeTirar);
                }
                else {
                    finMano(puntosGanados, 0);
                }
            }
        });
    }

    public void quiero() {
        invokeLater(new Runnable() {
            public void run() {

                setCanto(imgQuiero);
                setHeDeResponder(false);

                disable(quiero, noquiero);

                // si me toca tirar, mantengo el turno, sino no
                setMiTurno(heDeTirar);
            }
        });
    }

    public void truco() {
        invokeLater(new Runnable() {
            public void run() {

                envidoEnJuego = false;
                contraFlorEnJuego = false;

                setCanto(imgTruco);

                enable(quiero, noquiero, retruco);
                disable(envido, real, falta, truco, vale4);

                if (primeraMano && !soyMano && !seJugoTanto && !seJugoFlor) {
                    // habilito el envido porque el envido esta primero!
                    enable(envido, real, falta);
                }

                setHeDeResponder(true);
                setMiTurno(true);
            }
        });
    }

    public void retruco() {
        invokeLater(new Runnable() {
            public void run() {

                setCanto(imgRetruco);
                setHeDeResponder(true);

                enable(quiero, noquiero, vale4);
                disable(retruco, envido, real, falta);

                setMiTurno(true);
            }
        });
    }

    public void vale4() {
        invokeLater(new Runnable() {
            public void run() {

                setCanto(imgVale4);
                setHeDeResponder(true);

                disable(vale4);
                enable(quiero, noquiero);

                setMiTurno(true);
            }
        });
    }

    public void puntosEnvido(final int puntosGanados, final int tantoOponente) {
        invokeLater(new Runnable() {
            public void run() {
                // envido querido
                disable(envido, real, falta, flor, florno, contraflor, quiero,
                        noquiero);
                enable(truco);
                setHeDeResponder(false);

                if (mano.getValorEnvido() > tantoOponente
                        || (mano.getValorEnvido() == tantoOponente && soyMano)) {
                    // gane yo
                    fichasYo += puntosGanados;
                    updatePuntaje();

                    if (soyMano) {
                        cantoLabel.setText(" son buenas");
                    }
                    else {
                        cantoLabel.setText(tantoOponente + ", son buenas");
                    }
                    setCanto(cantoLabel);
                }
                else {
                    // gano el otro
                    fichasOtro += puntosGanados;
                    updatePuntaje();

                    cantoLabel.setText(tantoOponente + " son mejores");
                    setCanto(cantoLabel);
                }

                setMiTurno(heDeTirar);
            }
        });
    }

    @Override
    public void tuTurno() {
        invokeLater(new Runnable() {
            public void run() {
                setMiTurno(true);
                setHeDeTirar(true);
            }
        });
    }

    @Override
    public void puntosFlor(final int tantoOponente) {
        invokeLater(new Runnable() {
            public void run() {
                disable(envido, real, falta, flor, florno, contraflor, quiero,
                        noquiero);
                enable(truco);
                setHeDeResponder(false);

                if (mano.getValorFlor() > tantoOponente
                        || (mano.getValorFlor() == tantoOponente && soyMano)) {
                    // gane yo

                    if (contraFlorEnJuego) {
                        fichasYo = room.getPuntajeJuego();
                    }
                    else {
                        fichasYo += 6;
                    }
                    updatePuntaje();

                    if (soyMano) {
                        cantoLabel.setText(" son buenas");
                    }
                    else {
                        cantoLabel.setText(tantoOponente + ", son buenas");
                    }
                    setCanto(cantoLabel);
                }
                else {
                    // gano el otro
                    if (contraFlorEnJuego) {
                        fichasOtro = room.getPuntajeJuego();
                    }
                    else {
                        fichasOtro += 6;
                    }
                    updatePuntaje();

                    cantoLabel.setText(tantoOponente + " son mejores");
                    setCanto(cantoLabel);
                }

                setMiTurno(heDeTirar);
            }
        });
    }

    private CoreImage getCartaImage(Carta carta) {
        int fila, columna;

        if (carta.getPalo() == Palo.ESPADA) {
            fila = 0;
        }
        else if (carta.getPalo() == Palo.BASTO) {
            fila = 1;
        }
        else if (carta.getPalo() == Palo.COPA) {
            fila = 2;
        }
        else { // Palo.ORO)
            fila = 3;
        }

        if (carta.getNum() == Numero.ANCHO) {
            columna = 0;
        }
        else if (carta.getNum() == Numero.DOS) {
            columna = 1;
        }
        else if (carta.getNum() == Numero.TRES) {
            columna = 2;
        }
        else if (carta.getNum() == Numero.CUATRO) {
            columna = 3;
        }
        else if (carta.getNum() == Numero.CINCO) {
            columna = 4;
        }
        else if (carta.getNum() == Numero.SEIS) {
            columna = 5;
        }
        else if (carta.getNum() == Numero.SIETE) {
            columna = 6;
        }
        else if (carta.getNum() == Numero.SOTA) {
            columna = 7;
        }
        else if (carta.getNum() == Numero.CABALLO) {
            columna = 8;
        }
        else { // Numero.REY)
            columna = 9;
        }

        return cartas[fila * 10 + columna];
    }

    private void showCartaDelOtro(final Carta c) {
        if (cartaOtro1 == null) {
            cartaOtro1 = new ImageSprite(getCartaImage(c), 30, 72);
            add(cartaOtro1);
        }
        else if (cartaOtro2 == null) {
            cartaOtro2 = new ImageSprite(getCartaImage(c), 130, 72);
            add(cartaOtro2);
        }
        else { // cartaOtro3
            cartaOtro3 = new ImageSprite(getCartaImage(c), 230, 72);
            add(cartaOtro3);
        }
    }

    private void disable(Button... botones) {
        for (Button b : botones) {
            b.enabled.set(false);
            b.alpha.animateTo(0, 300);
        }
    }

    private void enable(Button... botones) {
        for (Button b : botones) {
            b.enabled.set(true);
            b.alpha.animateTo(255, 300);
        }
    }

    private void moveButtons(boolean in) {
        moveButtons(in, truco, retruco, vale4, quiero, noquiero, envido, real,
                falta, flor, florno, contraflor, alMazo, repartir);
    }

    private void moveButtons(boolean in, Button... botones) {
        int x = in ? BOTON_X_IN : BOTON_X_OUT;
        int y = BOTON_Y;
        for (Button b : botones) {
            if (b.enabled.get()) {
                b.x.animateTo(x, 300, Easing.REGULAR_OUT);
                b.y.animateTo(y, 300, Easing.REGULAR_OUT);
                y += b.height.getAsInt() + 5;
            }
        }
    }

    private void updatePuntaje() {
        puntajeYoLbl.setText(Integer.toString(fichasYo));
        puntajeOtroLbl.setText(Integer.toString(fichasOtro));

        if (Math.random() < 0.5) {
            puntajeYoLbl.height.animate(Math.random() * 100,
                    puntajeYoLbl.height.getAsInt(), 800, Easing.ELASTIC_OUT);
        }
        if (Math.random() < 0.5) {
            puntajeYoLbl.width.animate(Math.random() * 100, puntajeYoLbl.width
                    .getAsInt(), 800, Easing.ELASTIC_OUT);
        }

        if (Math.random() < 0.5) {
            puntajeOtroLbl.height.animate(Math.random() * 100,
                    puntajeOtroLbl.height.getAsInt(), 800, Easing.ELASTIC_OUT);
        }
        if (Math.random() < 0.5) {
            puntajeOtroLbl.width.animate(Math.random() * 100,
                    puntajeOtroLbl.width.getAsInt(), 800, Easing.ELASTIC_OUT);
        }
    }

    private void sacarCartas() {
        // saco las cartas del otro
        remove(cartaOtro1);
        cartaOtro1 = null;
        remove(cartaOtro2);
        cartaOtro2 = null;
        remove(cartaOtro3);
        cartaOtro3 = null;

        // saco mis cartas
        remove(carta1);
        remove(carta2);
        remove(carta3);
    }

    public void oponenteAbandono(final boolean enJuego, User user) {
        invokeLater(new Runnable() {
            public void run() {
                if (enJuego) {
                    finalLabel.setText("¡Tu oponente abandono!");
                }
                else {
                    finalLabel.setText("No quiso jugar otro");
                }
                PulpcoreUtils.centerSprite(finalLabel, 235, 319);
                finalLabel.visible.set(true);
                finalLabel2.visible.set(false);

                // TODO boton ACK

                if (torneo) {
                    // vuelta al TorneoScene
                    addEvent(new TimelineEvent(2000) {
                        @Override
                        public void run() {
                            setScene(new TorneoScene(connection, currentUser));
                        }
                    });
                }
                else {
                    // back to lobby
                    setScene(new LobbyScene(currentUser, connection));
                }
            }
        });
    }

    private void drawNames() {
        int MAX_SIZE = 15;

        String yo = currentUser.getName();
        if (yo.length() > MAX_SIZE) {
            yo = yo.substring(0, MAX_SIZE);
        }
        String otro = oponente.getName();
        if (otro.length() > MAX_SIZE) {
            otro = otro.substring(0, MAX_SIZE);
        }

        do {
            // uno distinto!
            colorOtro = RANDOM_COLORS[(int) (Math.random() * RANDOM_COLORS.length)];
        }
        while (colorOtro == colorYo);

        CoreFont din24 = CoreFont.load("imgs/DIN24.font.png");

        // nombre para el puntaje
        Label lYo = new Label(din24, yo, 0, 135);
        PulpcoreUtils.centerSprite(lYo, 467, 242);
        add(lYo);

        Label lOtro = new Label(din24, otro, 0, 175);
        PulpcoreUtils.centerSprite(lOtro, 467, 242);
        add(lOtro);

        oponenteLabel = new Label(din24.tint(colorOtro), otro, 0, 75);
        // centrado a la derecha, a la izquierda de diceLabel
        oponenteLabel.x.set(diceLabel.x.getAsInt()
                - oponenteLabel.width.getAsInt() - 15);

        oponenteLabel.visible.set(false);
        add(oponenteLabel);

        // puntajes
        puntajeYoLbl = new Label(din24, Integer.toString(fichasYo), lYo.x
                .getAsInt()
                + lYo.width.getAsInt() + 10, lYo.y.getAsInt());
        puntajeOtroLbl = new Label(din24, Integer.toString(fichasOtro), lOtro.x
                .getAsInt()
                + lOtro.width.getAsInt() + 10, lOtro.y.getAsInt());

        add(puntajeYoLbl);
        add(puntajeOtroLbl);
    }

    private final void setCanto(Sprite spr) {
        // saco lo que haya
        oponenteLabel.visible.set(false);
        diceLabel.visible.set(false);
        cantoLabel.visible.set(false);
        imgTruco.visible.set(false);
        imgRetruco.visible.set(false);
        imgVale4.visible.set(false);
        imgQuiero.visible.set(false);
        imgNoQuiero.visible.set(false);
        imgEnvido.visible.set(false);
        imgReal.visible.set(false);
        imgFalta.visible.set(false);
        imgFlor.visible.set(false);
        imgFlorNo.visible.set(false);
        imgContraFlor.visible.set(false);
        imgAlMazo.visible.set(false);

        if (spr != null) {
            oponenteLabel.visible.set(true);
            diceLabel.visible.set(true);
            spr.visible.set(true);

            // animo ancho|alto|angulo aleatorio
            if (Math.random() < 0.33) {
                spr.angle.animate(Math.PI, spr.angle.getAsInt(), 800,
                        Easing.ELASTIC_OUT);
            }
            if (Math.random() < 0.33) {
                spr.height.animate(0, spr.height.getAsInt(), 800,
                        Easing.ELASTIC_OUT);
            }
            if (Math.random() < 0.33) {
                spr.width.animate(0, spr.width.getAsInt(), 800,
                        Easing.ELASTIC_OUT);
            }
        }
    }

    public void disconnected() {
        invokeLater(new Runnable() {
            public void run() {
                Stage.setScene(new DisconnectedScene(Reason.FAILED));
            }
        });
    }

    private final void setScene(final Scene s) {
        mustDisconnect = false;
        Stage.setScene(s);
    }

    private void abandonGame() {
        // envio abandono
        connection.send(new AbandonRoomMessage());

        invokeLater(new Runnable() {
            public void run() {
                // me rajo al lobby
                setScene(new LobbyScene(currentUser, connection));
            }
        });
    }

    private int[] getRandomOutside() {
        Random r = new Random();

        int w = Stage.getWidth();
        int h = Stage.getHeight();

        int ret[] = null;

        int rand = r.nextInt(4);

        if (rand == 0) { // arriba
            ret = new int[] { r.nextInt(w), -200 };
        }
        else if (rand == 1) { // abajo
            ret = new int[] { r.nextInt(w), h + 200 };
        }
        else if (rand == 2) { // izq
            ret = new int[] { -200, r.nextInt(h) };
        }
        else if (rand == 3) { // der
            ret = new int[] { w + 200, r.nextInt(h) };
        }

        return ret;
    }

    @Override
    public void newGame() {
    }

    @Override
    public void startGame(boolean start) {
    }
}

// TODO boton jengibre cliqueable y que abra ventanita