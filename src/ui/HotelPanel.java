package ui;

import model.Area;
import model.Hotel;
import model.Gast;
import model.Persoon;
import model.Lift;
import model.Schoonmaker;
import model.Kamer;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * Paneel verantwoordelijk voor het visueel weergeven van het hotel en de simulatie-objecten.
 */
public class HotelPanel extends JPanel implements MouseInputListener {

    private Hotel hotel;
    private Lift lift;
    private final int VAKJE_GROOTTE = 50;
    private boolean running = true;

    // Callbacks voor kliks
    private Runnable onLobbyClick;
    private Consumer<Kamer> onRoomClick;
    private Consumer<Persoon> onPersonClick;

    public HotelPanel(Hotel hotel) {
        this.hotel = hotel;
        updateDimensions();
        addMouseListener(this);
        setFocusable(true);
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
        updateDimensions();
        repaint();
    }

    public void setLift(Lift lift) { this.lift = lift; }
    public void setRunning(boolean running) { this.running = running; }
    public void setOnLobbyClick(Runnable callback) { this.onLobbyClick = callback; }
    public void setOnRoomClick(Consumer<Kamer> callback) { this.onRoomClick = callback; }
    public void setOnPersonClick(Consumer<Persoon> callback) { this.onPersonClick = callback; }

    private void updateDimensions() {
        if (hotel == null) return;
        int breedte = hotel.getBreedte() * VAKJE_GROOTTE;
        int hoogte = hotel.getHoogte() * VAKJE_GROOTTE;
        this.setPreferredSize(new Dimension(breedte, hoogte));
        revalidate();
    }

    // Centraal punt om de offsets te berekenen (voorkomt herhaling in code)
    private Point getHotelOffset() {
        int hotelBreedte = hotel.getBreedte() * VAKJE_GROOTTE;
        int hotelHoogte = hotel.getHoogte() * VAKJE_GROOTTE;
        int offsetX = (getWidth() - hotelBreedte) / 2;
        int offsetY = (getHeight() - hotelHoogte) / 2;
        return new Point(offsetX, offsetY);
    }

    // --- TEKEN LOGICA (PAINT) ---

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (hotel == null) return;

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        Point offset = getHotelOffset();
        tekenGrid(g, offset.x, offset.y);
        tekenAreas(g, offset.x, offset.y);
        tekenLift(g, offset.x, offset.y);
        tekenPersonen(g, offset.x, offset.y);
        tekenLegenda(g);

        // Pauze-scherm overlay
        if (!running) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("PAUSED", getWidth() / 2 - 70, getHeight() / 2);
        }
    }

    private void tekenGrid(Graphics g, int offsetX, int offsetY) {
        g.setColor(new Color(230, 230, 230));
        int hotelBreedte = hotel.getBreedte() * VAKJE_GROOTTE;
        int hotelHoogte = hotel.getHoogte() * VAKJE_GROOTTE;

        for (int i = 0; i <= hotel.getBreedte(); i++) {
            g.drawLine(offsetX + i * VAKJE_GROOTTE, offsetY, offsetX + i * VAKJE_GROOTTE, offsetY + hotelHoogte);
        }
        for (int j = 0; j <= hotel.getHoogte(); j++) {
            g.drawLine(offsetX, offsetY + j * VAKJE_GROOTTE, offsetX + hotelBreedte, offsetY + j * VAKJE_GROOTTE);
        }
    }

    private void tekenAreas(Graphics g, int offsetX, int offsetY) {
        Graphics2D g2 = (Graphics2D) g;

        for (Area area : hotel.getAreas()) {
            if ("Lift".equals(area.getAreaType())) continue; // Wordt dynamisch getekend

            int x = (area.getX() - 1) * VAKJE_GROOTTE + offsetX;
            int y = (area.getY() - 1) * VAKJE_GROOTTE + offsetY;
            int breedte = area.getBreedte() * VAKJE_GROOTTE;
            int hoogte = area.getHoogte() * VAKJE_GROOTTE;

            // Achtergrond en rand
            g.setColor(getKleur(area));
            g.fillRect(x, y, breedte, hoogte);
            g.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(2));
            g.drawRect(x, y, breedte, hoogte);

            // Label text
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            String label = getLabel(area);
            FontMetrics fm = g.getFontMetrics();
            int textX = x + (breedte - fm.stringWidth(label)) / 2;
            int textY = y + (hoogte + fm.getAscent()) / 2 - 2;
            g.drawString(label, textX, textY);
        }
        g2.setStroke(new BasicStroke(1)); // Reset stroke
    }

    private void tekenLift(Graphics g, int offsetX, int offsetY) {
        if (lift == null) return;

        int liftX = (int)(lift.getX() * VAKJE_GROOTTE) + offsetX - VAKJE_GROOTTE / 2;
        int liftY = (int)(lift.getY() * VAKJE_GROOTTE) + offsetY - VAKJE_GROOTTE / 2;

        g.setColor(new Color(200, 100, 0)); // Oranje
        g.fillRect(liftX, liftY, VAKJE_GROOTTE, VAKJE_GROOTTE);

        g.setColor(Color.BLACK);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));
        g.drawRect(liftX, liftY, VAKJE_GROOTTE, VAKJE_GROOTTE);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        FontMetrics fm = g.getFontMetrics();
        int lx = liftX + (VAKJE_GROOTTE - fm.stringWidth("L")) / 2;
        int ly = liftY + (VAKJE_GROOTTE + fm.getAscent()) / 2 - 5;
        g.drawString("L", lx, ly);
    }

    private void tekenPersonen(Graphics g, int offsetX, int offsetY) {
        for (Persoon persoon : hotel.getPersonen()) {
            int px = (int)(persoon.getX() * VAKJE_GROOTTE) + offsetX - 8;
            int py = (int)(persoon.getY() * VAKJE_GROOTTE) + offsetY - 8;

            if (persoon instanceof Gast gast) {
                g.setColor(gast.getKleur());
                g.fillOval(px, py, 16, 16);
                g.setColor(Color.BLACK);
                g.drawOval(px, py, 16, 16);

                // Label boven gast
                g.setFont(new Font("Arial", Font.BOLD, 9));
                String label = gast.getNaam() + (gast.getHuidigeActiviteit().isEmpty() ? "" : " " + gast.getHuidigeActiviteit());
                g.drawString(label, px - 5, py - 5);

            } else if (persoon instanceof Schoonmaker schoonmaker) {
                g.setColor(schoonmaker.getKleur());
                g.fillRect(px, py, 16, 16);
                g.setColor(Color.BLACK);
                g.drawRect(px, py, 16, 16);

                g.setFont(new Font("Arial", Font.BOLD, 10));
                g.drawString("S", px + 5, py + 8);

                // Label boven schoonmaker
                g.setFont(new Font("Arial", Font.PLAIN, 8));
                String label = "CM" + (schoonmaker.getHuidigeActiviteit().isEmpty() ? "" : " " + schoonmaker.getHuidigeActiviteit());
                g.drawString(label, px - 5, py - 3);
            }
        }
    }

    private void tekenLegenda(Graphics g) {
        int lx = 10;
        int ly = getHeight() - 70;

        g.setColor(new Color(255, 255, 255, 220));
        g.fillRect(lx, ly, 130, 60);
        g.setColor(Color.BLACK);
        g.drawRect(lx, ly, 130, 60);
        g.setFont(new Font("Arial", Font.PLAIN, 11));

        g.setColor(Color.BLUE);
        g.fillOval(lx + 10, ly + 10, 10, 10);
        g.setColor(Color.BLACK);
        g.drawString("Gast", lx + 30, ly + 20);

        g.setColor(Color.DARK_GRAY);
        g.fillRect(lx + 10, ly + 25, 10, 10);
        g.setColor(Color.BLACK);
        g.drawString("Schoonmaker", lx + 30, ly + 35);

        g.setColor(new Color(200, 100, 0));
        g.fillRect(lx + 10, ly + 40, 10, 10);
        g.setColor(Color.BLACK);
        g.drawString("Lift", lx + 30, ly + 50);
    }

    // --- KLIK DETECTIE (MOUSE LISTENER) ---

    @Override
    public void mouseClicked(MouseEvent e) {
        if (hotel == null) return;

        int clickX = e.getX();
        int clickY = e.getY();
        Point offset = getHotelOffset();

        // 1. Check personen (Hoogste prioriteit)
        for (Persoon persoon : hotel.getPersonen()) {
            if (isKlikOpPersoon(persoon, clickX, clickY, offset.x, offset.y)) {
                System.out.println("[Click] 👤 Persoon aangeklikt: " + persoon.getNaam());
                if (onPersonClick != null) onPersonClick.accept(persoon);
                return;
            }
        }

        // 2. Check kamers
        for (Area area : hotel.getAreas()) {
            if ("Room".equals(area.getAreaType()) && isKlikOpArea(area, clickX, clickY, offset.x, offset.y)) {
                for (Kamer kamer : hotel.getKamers()) {
                    if (kamer.getArea() != null && kamer.getArea().equals(area)) {
                        System.out.println("[Click] 🚪 Kamer aangeklikt: " + kamer.getKamernummer());
                        if (onRoomClick != null) onRoomClick.accept(kamer);
                        return;
                    }
                }
            }
        }

        // 3. Check lobby
        for (Area area : hotel.getAreas()) {
            if ("Lobby".equals(area.getAreaType()) && isKlikOpArea(area, clickX, clickY, offset.x, offset.y)) {
                System.out.println("[US2.1] 🏨 Lobby aangeklikt - Gastenoverzicht openen");
                if (onLobbyClick != null) onLobbyClick.run();
                return;
            }
        }
    }

    private boolean isKlikOpPersoon(Persoon persoon, int clickX, int clickY, int offsetX, int offsetY) {
        int px = (int)(persoon.getX() * VAKJE_GROOTTE) + offsetX - 8;
        int py = (int)(persoon.getY() * VAKJE_GROOTTE) + offsetY - 8;

        if (persoon instanceof Gast) {
            int centerX = px + 8;
            int centerY = py + 8;
            double afstand = Math.sqrt(Math.pow(clickX - centerX, 2) + Math.pow(clickY - centerY, 2));
            return afstand <= 10;
        }
        if (persoon instanceof Schoonmaker) {
            return clickX >= px && clickX <= px + 16 && clickY >= py && clickY <= py + 16;
        }
        return false;
    }

    private boolean isKlikOpArea(Area area, int clickX, int clickY, int offsetX, int offsetY) {
        int x = (area.getX() - 1) * VAKJE_GROOTTE + offsetX;
        int y = (area.getY() - 1) * VAKJE_GROOTTE + offsetY;
        int breedte = area.getBreedte() * VAKJE_GROOTTE;
        int hoogte = area.getHoogte() * VAKJE_GROOTTE;

        return clickX >= x && clickX <= x + breedte && clickY >= y && clickY <= y + hoogte;
    }

    // --- HULPMETHODEN VOOR LABELS EN KLEUREN (MODERNE SWITCH) ---

    private String getLabel(Area area) {
        return switch (area.getAreaType()) {
            case "Room" -> area.getClassification() != null ? area.getClassification().replace(" Star", "★") : "Kamer";
            case "Cinema" -> "Bioscoop";
            case "Restaurant" -> "Restaurant";
            case "Fitness" -> "Fitness";
            case "Lobby" -> "Lobby";
            case "Schacht" -> "Schacht";
            case "Lift" -> "Lift";
            case "Staircase" -> "Trap";
            case "Storage" -> "Opslag";
            default -> area.getAreaType();
        };
    }

    private Color getKleur(Area area) {
        return switch (area.getAreaType()) {
            case "Room" -> {
                String c = area.getClassification();
                if (c == null) yield new Color(200, 230, 255);
                if (c.contains("1")) yield new Color(200, 230, 255);
                if (c.contains("2")) yield new Color(150, 200, 255);
                if (c.contains("3")) yield new Color(100, 170, 255);
                if (c.contains("4")) yield new Color(50, 130, 255);
                if (c.contains("5")) yield new Color(0, 80, 200);
                yield new Color(200, 230, 255);
            }
            case "Lobby" -> new Color(255, 220, 50);
            case "Schacht" -> new Color(220, 220, 220);
            case "Lift" -> new Color(255, 150, 0);
            case "Staircase" -> new Color(100, 200, 100);
            case "Restaurant" -> new Color(255, 100, 100);
            case "Cinema" -> new Color(200, 100, 200);
            case "Fitness" -> new Color(100, 220, 180);
            case "Storage" -> new Color(180, 140, 100);
            default -> Color.WHITE;
        };
    }

    // --- ONGEBRUIKTE INTERFACE METHODS COMPACT ONDERAAN ---
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}