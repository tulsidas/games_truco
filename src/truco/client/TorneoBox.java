package truco.client;

import java.util.List;

import pulpcore.image.Colors;
import pulpcore.image.CoreFont;
import pulpcore.image.CoreGraphics;
import pulpcore.sprite.FilledSprite;
import pulpcore.sprite.Label;
import truco.common.model.RoomInfo;
import client.PulpcoreUtils;
import client.Scrollable;

public class TorneoBox extends Scrollable<RoomInfo> {

    private final CoreFont font, fontVerde, fontRoja;

    private final int mitad;

    public TorneoBox(int x, int y, int w, int h, CoreFont font) {
        super(x, y, w, h);

        this.font = font;
        this.fontVerde = font.tint(Colors.darker(Colors.GREEN));
        this.fontRoja = font.tint(Colors.darker(Colors.RED));
        this.mitad = getAvailableSpace() / 2;

        setScrollOnRefresh(false);
        setSorted(true);
    }

    @Override
    public void createContent(List<RoomInfo> objects) {
        if (objects.isEmpty()) {
            return;
        }
        else {
            int posY = 2;

            int ronda = objects.get(0).getRonda();
            addSeparador(ronda, 1);

            posY += getLineSpacing();
            for (RoomInfo ri : objects) {
                if (ri.getRonda() != ronda) {
                    ronda = ri.getRonda();
                    addSeparador(ronda, posY);
                    posY += getLineSpacing();
                }

                add(createLabel(ri, posY));
                posY += getLineSpacing();
            }
        }

        scrollEnd();
    }

    private void addSeparador(int ronda, int posY) {
        Label l = new Label(font.tint(Colors.RED), getStringRonda(ronda), 0,
                posY);
        PulpcoreUtils.centerSprite(l, 0, getAvailableSpace());
        FilledSprite fs = new FilledSprite(0, posY - 1, width.getAsInt(),
                l.height.getAsInt() + 1, Colors.ORANGE);
        fs.borderColor.set(Colors.BLACK);
        fs.setBorderSize(2);

        add(fs);
        add(l);
    }

    private String getStringRonda(int ronda) {
        if (ronda == 0) {
            return "FINAL";
        }
        else if (ronda == 1) {
            return "SEMIFINAL";
        }
        else if (ronda == 2) {
            return "CUARTOS";
        }
        else if (ronda == 3) {
            return "OCTAVOS";
        }
        else {
            return ((int) Math.pow(2, ronda)) + "os";
        }
    }

    private Label createLabel(final RoomInfo ri, int posY) {
        final String s1 = ri.getUser1()
                + (ri.getPuntaje1() < 0 ? "" : " " + ri.getPuntaje1());
        final String s2 = " | ";
        final String s3 = (ri.getPuntaje2() < 0 ? "" : ri.getPuntaje2() + " ")
                + ri.getUser2();

        int pos = mitad - font.getStringWidth(s1 + s2);

        return new Label(font, s1 + s2 + s3, pos, posY) {
            @Override
            protected void drawText(CoreGraphics g, String text) {
                // FIXME hardcodeado a 15
                if (ri.getPuntaje1() >= 0 && ri.getPuntaje1() < 15
                        && ri.getPuntaje2() >= 0 && ri.getPuntaje2() < 15) {
                    super.drawText(g, text);
                }
                else {
                    CoreFont font1 = fontRoja;
                    CoreFont font2 = fontVerde;
                    // verde y rojo a ganador|perdedor
                    if (ri.getPuntaje1() >= 15 || ri.getPuntaje2() < 0) {
                        font1 = fontVerde;
                        font2 = fontRoja;
                    }

                    g.setFont(font1);
                    g.drawString(s1);

                    g.setFont(font);
                    g.drawString(s2, font1.getStringWidth(s1), 0);

                    g.setFont(font2);
                    g.drawString(s3, font1.getStringWidth(s1)
                            + font.getStringWidth(s2), 0);
                }
            }
        };
    }

    public void addRoom(int id, int ronda, String p1, String p2) {
        RoomInfo ri = new RoomInfo(id, ronda, p1, p2);
        addItem(ri);
    }

    public void addRoom(RoomInfo ri) {
        addItem(ri);
    }

    public void removeRoom(int id) {
        RoomInfo ri = getRoomInfo(id);
        if (ri != null) {
            removeItem(ri);
        }
    }

    public void updatePuntaje(int id, int p1, int p2) {
        RoomInfo ri = getRoomInfo(id);
        if (ri != null) {
            if (p1 < 0) {
                ri.setUser1(null);
                ri.setPuntaje1(-1);
            }
            else {
                ri.setPuntaje1(p1);
            }

            if (p2 < 0) {
                ri.setUser2(null);
                ri.setPuntaje2(-1);
            }
            else {
                ri.setPuntaje2(p2);
            }
        }

        needsRefresh();
    }

    private RoomInfo getRoomInfo(int id) {
        RoomInfo ri = null;
        for (Object o : getObjects()) {
            if (((RoomInfo) o).getId() == id) {
                ri = (RoomInfo) o;
                break;
            }
        }

        return ri;
    }

    @Override
    public int getLineSpacing() {
        return font.getHeight() + 2;
    }
}
