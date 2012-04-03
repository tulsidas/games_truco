package truco.client;

import pulpcore.Stage;
import pulpcore.image.Colors;
import pulpcore.image.CoreImage;
import pulpcore.scene.Scene;
import pulpcore.sprite.Button;
import pulpcore.sprite.ImageSprite;
import pulpcore.sprite.Label;
import pulpcore.sprite.Sprite;
import truco.common.ifaz.TrucoLobbyHandler;
import truco.common.messages.JoinTorneoMessage;
import truco.common.messages.client.CreateTrucoRoomMessage;
import truco.common.model.TrucoRoom;
import client.AbstractGameConnector;
import client.AbstractLobbyScene;

import common.messages.Message;
import common.model.AbstractRoom;
import common.model.User;

/**
 * @author Tulsi
 */
public class LobbyScene extends AbstractLobbyScene implements TrucoLobbyHandler {

    private Button a30, conFlor;

    private Button torneo;

    private Label labelTorneo;

    private int puntosTorneo, faltanTorneo;

    public LobbyScene(User user, AbstractGameConnector connection) {
        super(user, connection);
    }

    @Override
    public void load() {
        super.load();

        a30 = new Button(CoreImage.load("imgs/btn-a30.png").split(6), 80, 385,
                true);
        add(a30);

        conFlor = new Button(CoreImage.load("imgs/btn-flor.png").split(6), 80,
                415, true);
        conFlor.setSelected(true); // por default con flor
        add(conFlor);
    }

    @Override
    public void update(int elapsedTime) {
        super.update(elapsedTime);

        if (torneo != null && torneo.isClicked()
                && currentUser.getPuntos() >= puntosTorneo) {
            // un solo click
            torneo.enabled.set(false);

            connection.send(new JoinTorneoMessage());
        }
    }

    @Override
    protected Scene getGameScene(AbstractGameConnector connection, User usr,
            AbstractRoom room) {
        return new TrucoScene((GameConnector) connection, usr, (TrucoRoom) room);
    }

    @Override
    protected Sprite getGameImage() {
        return new ImageSprite(CoreImage.load("imgs/logo-truco.png"), 515, 10);
    }

    @Override
    protected Message createRoomMessage(int value) {
        return new CreateTrucoRoomMessage(value, a30.isSelected(), conFlor
                .isSelected());
    }

    /***************************************************************************
     * TrucoLobbyHandler
     **************************************************************************/
    @Override
    public void infoTorneo(final int faltan, final int puntos) {
        if (!currentUser.isGuest()) { // invitados no juegan torneos
            this.puntosTorneo = puntos;
            this.faltanTorneo = faltan;
            invokeLater(new Runnable() {
                public void run() {
                    // puedo estar creando torneo o actualizando de los que
                    // faltan

                    if (torneo == null) {
                        torneo = Button
                                .createLabeledButton("Torneo!", 425, 420);
                        add(torneo);
                    }

                    if (labelTorneo == null) {
                        labelTorneo = new Label("", 515, 425);
                        labelTorneo.setFont(labelTorneo.getFont().tint(
                                Colors.WHITE));
                        add(labelTorneo);
                    }

                    updateLabelTorneo();
                }
            });
        }
    }

    private final void updateLabelTorneo() {
        if (labelTorneo != null) {
            String text = "x" + puntosTorneo + " fichas, "
                    + (faltanTorneo > 1 ? "faltan " : "falta ") + faltanTorneo;

            labelTorneo.setText(text);
        }
    }

    @Override
    public void joinedTorneo(final User user) {
        invokeLater(new Runnable() {
            public void run() {
                if (user.equals(currentUser)) {
                    mustDisconnect = false;
                    Stage.setScene(new TorneoScene((GameConnector) connection,
                            currentUser));
                }
                else {
                    // lo saco del lobby
                    players.removePlayer(user);

                    faltanTorneo--;
                    updateLabelTorneo();
                }
            }
        });
    }

    @Override
    public void torneoStatus(final boolean started) {
        invokeLater(new Runnable() {
            public void run() {
                remove(torneo);
                torneo = null;
                puntosTorneo = 0;
                faltanTorneo = 0;

                if (started) {
                    labelTorneo.setText("torneo en curso");
                }
                else {
                    remove(labelTorneo);
                    labelTorneo = null;
                }
            }
        });
    }
}