package truco.client;

import static pulpcore.image.Colors.WHITE;
import static pulpcore.image.Colors.rgb;

import java.util.Collection;

import pulpcore.Input;
import pulpcore.Stage;
import pulpcore.image.Colors;
import pulpcore.image.CoreFont;
import pulpcore.image.CoreImage;
import pulpcore.sprite.Button;
import pulpcore.sprite.FilledSprite;
import pulpcore.sprite.Label;
import pulpcore.sprite.TextField;
import truco.common.ifaz.TorneoHandler;
import truco.common.messages.TorneoChatMessage;
import truco.common.messages.TorneoJoinedMessage;
import truco.common.messages.client.AbandonTorneoMessage;
import truco.common.model.RoomInfo;
import truco.common.model.TrucoRoom;
import client.ColoredChatArea;
import client.PingScene;
import client.PlayersBox;

import common.model.User;

/**
 * @author Tulsi
 */
public class TorneoScene extends PingScene implements TorneoHandler {

    protected boolean mustDisconnect;

    private GameConnector connection;

    private User currentUser;

    private Button salir;

    private TorneoBox tBox;

    private PlayersBox pBox;

    /* chat */
    private ColoredChatArea chat;

    private TextField chatTF;

    private Button send;

    public TorneoScene(GameConnector connection, User currentUser) {
        super(connection);
        this.connection = connection;
        this.currentUser = currentUser;
    }

    @Override
    public void load() {
        super.load();

        CoreFont din13 = CoreFont.load("imgs/DIN13.font.png");
        CoreFont din15 = CoreFont.load("imgs/DIN15.font.png");

        add(new FilledSprite(Colors.LIGHTGRAY));

        salir = Button.createLabeledButton(" - Salir - ", 550, 20);
        add(salir);

        tBox = new TorneoBox(20, 20, 270, 420, din13);
        add(new FilledSprite(tBox.x.get(), tBox.y.get(), tBox.width.get(),
                tBox.height.get(), Colors.WHITE));
        // tBox.addRoom(0, 3, new User("u1"), new User("u2"));
        // tBox.addRoom(1, 3, new User("u3"), new User("u4"));
        // tBox.addRoom(2, 3, new User("u5"), new User("u6"));
        // tBox.addRoom(3, 3, new User("u7"), new User("u8"));
        // tBox.addRoom(4, 3, new User("u9"), new User("u10"));
        // tBox.addRoom(5, 3, new User("u11"), new User("u12"));
        // tBox.addRoom(6, 2, new User("u1"), new User("u3"));
        // tBox.addRoom(7, 3, new User("u13"), new User("u14"));
        // tBox.addRoom(8, 3, new User("u15"), new User("u16"));
        // tBox.addRoom(9, 2, new User("u5"), new User("u7"));
        // tBox.addRoom(10, 2, new User("u9"), new User("u11"));
        // tBox.addRoom(11, 2, new User("u13"), new User("u15"));
        // tBox.addRoom(12, 1, new User("u1"), new User("u5"));
        // tBox.addRoom(13, 1, new User("u9"), new User("u13"));
        // tBox.addRoom(14, 0, new User("u1"), new User("u9"));
        add(tBox);

        pBox = new PlayersBox(300, 20, 155, 420, din13);
        add(new FilledSprite(pBox.x.get(), pBox.y.get(), pBox.width.get(),
                pBox.height.get(), Colors.WHITE));
        add(pBox);
        // me agrego a mi mismo
        pBox.addPlayer(currentUser);

        chat = new ColoredChatArea(467, 90, 235, 289, din15, din15
                .tint(rgb(0xaa0000)), ':', currentUser.getName());
        add(new FilledSprite(chat.x.get(), chat.y.get(), chat.width.get(),
                chat.height.get(), Colors.WHITE));
        add(chat);

        chatTF = new TextField(din15, din15.tint(WHITE), "", 467, 393, 217, -1);
        chatTF.setMaxNumChars(150);
        add(new FilledSprite(chatTF.x.get(), chatTF.y.get(),
                chatTF.width.get(), chatTF.height.get(), Colors.WHITE));
        add(chatTF);

        // boton para enviar el chat (asociado al ENTER)
        send = new Button(CoreImage.load("imgs/btn-send.png").split(3), 688,
                392);
        send.setKeyBinding(Input.KEY_ENTER);
        add(send);

        connection.setTorneoHandler(this);

        // mando el ACK que me uni
        connection.send(new TorneoJoinedMessage());
    }

    public void unload() {
        if (mustDisconnect) {
            connection.disconnect();
        }
    }

    @Override
    public void update(int elapsedTime) {
        super.update(elapsedTime);
        if (salir.isClicked()) {
            salir();
        }
        else if (send.isClicked()) {
            if (chatTF.getText().trim().length() > 0) {
                // mando
                connection.send(new TorneoChatMessage(chatTF.getText()));

                chat.addLine(currentUser.getName() + ": " + chatTF.getText());
                chatTF.setText("");
            }
        }
    }

    @Override
    public void joinRoom(final TrucoRoom room) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                mustDisconnect = false;
                Stage.setScene(new TrucoScene(connection, currentUser, room,
                        true));
            }
        });
    }

    @Override
    public void ganador(final User ganador) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                add(new Label(ganador + " es el campeon", 200, 100));
            }
        });
    }

    private void salir() {
        // envio abandono
        connection.send(new AbandonTorneoMessage());

        volverAlLobby();
    }

    public void volverAlLobby() {
        invokeLater(new Runnable() {
            public void run() {
                // me rajo al lobby
                mustDisconnect = false;
                Stage.setScene(new LobbyScene(currentUser, connection));
            }
        });
    }

    public void incomingTorneoChat(final User usr, final String msg) {
        invokeLater(new Runnable() {
            public void run() {
                chat.addLine(usr.getName() + ": " + msg);
            }
        });
    }

    @Override
    public void newPartidoTorneo(final int id, final int ronda,
            final String p1, final String p2) {
        invokeLater(new Runnable() {
            public void run() {
                tBox.addRoom(id, ronda, p1, p2);
            }
        });
    }

    @Override
    public void updatePartidoTorneo(final int id, final int puntaje1,
            final int puntaje2) {
        invokeLater(new Runnable() {
            public void run() {
                tBox.updatePuntaje(id, puntaje1, puntaje2);
            }
        });
    }

    @Override
    public void setInfoTorneo(final Collection<RoomInfo> roomInfos,
            final Collection<User> users, final User campeon) {
        invokeLater(new Runnable() {
            public void run() {
                for (RoomInfo ri : roomInfos) {
                    tBox.addRoom(ri);
                }

                for (User u : users) {
                    pBox.addPlayer(u);
                }

                if (campeon != null) {
                    campeonTorneo(campeon);
                }
            }
        });
    }

    @Override
    public void campeonTorneo(final User campeon) {
        invokeLater(new Runnable() {
            public void run() {
                if (campeon != null) {
                    add(new Label(campeon.getName() + " es el campeon!", 450, 5));
                }
            }
        });
    }

    @Override
    public void userAbandoned(final User u) {
        invokeLater(new Runnable() {
            public void run() {
                pBox.removePlayer(u);
            }
        });
    }

    @Override
    public void userJoined(final User u) {
        invokeLater(new Runnable() {
            public void run() {
                pBox.addPlayer(u);
            }
        });
    }
}