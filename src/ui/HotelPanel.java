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

/**
 * Dit paneel is verantwoordelijk voor het visueel weergeven van het hotel.
 * US4.4: Grafische weergave implementatie voor het volgen van de simulatie.
 * US2.1: Klik op Lobby om gastenoverzicht te openen
 */
public class HotelPanel extends JPanel implements MouseInputListener {

    private Hotel hotel;
    private Lift lift;  // Referentie naar de lift om deze te tekenen

    private final int VAKJE_GROOTTE = 50;

    // status van simulatie (optioneel)
    private boolean running = true;
    
    // US2.1: Callback voor lobby clicks
    private Runnable onLobbyClick;

    // New: Callbacks voor kamer en persoon detail windows
    private java.util.function.Consumer<model.Kamer> onRoomClick;
    private java.util.function.Consumer<model.Persoon> onPersonClick;

    public HotelPanel(Hotel hotel) {
        this.hotel = hotel;
        updateDimensions();
        
        // US2.1: Voeg mouse listener toe
        addMouseListener(this);
        setFocusable(true);
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
        updateDimensions();
        repaint();
    }

    public void setLift(Lift lift) {
        this.lift = lift;
    }

    // Simulator kan dit gebruiken
    public void setRunning(boolean running) {
        this.running = running;
    }
    
    // US2.1: Stel callback in voor lobby clicks
    public void setOnLobbyClick(Runnable callback) {
        this.onLobbyClick = callback;
    }

    // New: Stel callbacks in voor kamer en persoon details
    public void setOnRoomClick(java.util.function.Consumer<model.Kamer> callback) {
        this.onRoomClick = callback;
    }

    public void setOnPersonClick(java.util.function.Consumer<model.Persoon> callback) {
        this.onPersonClick = callback;
    }

    private void updateDimensions() {
        if (hotel == null) return;

        int breedte = hotel.getBreedte() * VAKJE_GROOTTE;
        int hoogte = hotel.getHoogte() * VAKJE_GROOTTE;
        this.setPreferredSize(new Dimension(breedte, hoogte));
        revalidate(); // belangrijk voor UI refresh
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (hotel == null) return;

        // Achtergrond (vloer)
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Bereken offsets om hotel in het midden te centreren
        int hotelBreedte = hotel.getBreedte() * VAKJE_GROOTTE;
        int hotelHoogte = hotel.getHoogte() * VAKJE_GROOTTE;
        int offsetX = (getWidth() - hotelBreedte) / 2;
        int offsetY = (getHeight() - hotelHoogte) / 2;

        // === US4.4: HULPLIJNEN (GRID) ===
        // Teken een licht grijs grid zodat de gebruiker de vakjes/afstanden ziet
        g.setColor(new Color(230, 230, 230));
        for (int i = 0; i <= hotel.getBreedte(); i++) {
            g.drawLine(offsetX + i * VAKJE_GROOTTE, offsetY, offsetX + i * VAKJE_GROOTTE, offsetY + hotelHoogte);
        }
        for (int j = 0; j <= hotel.getHoogte(); j++) {
            g.drawLine(offsetX, offsetY + j * VAKJE_GROOTTE, offsetX + hotelBreedte, offsetY + j * VAKJE_GROOTTE);
        }

        // Areas tekenen
        List<Area> areas = hotel.getAreas();
        for (Area area : areas) {
            // SKIP: De 1x1 Lift area - deze wordt dynamisch getekend!
            if (area.getAreaType().equals("Lift")) {
                continue;  // Teken NIET op vaste positie
            }

            int x = (area.getX() - 1) * VAKJE_GROOTTE + offsetX;
            int y = (area.getY() - 1) * VAKJE_GROOTTE + offsetY;

            int breedte = area.getBreedte() * VAKJE_GROOTTE;
            int hoogte = area.getHoogte() * VAKJE_GROOTTE;

            // Achtergrond kleur
            g.setColor(getKleur(area));
            g.fillRect(x, y, breedte, hoogte);

            // Rand
            g.setColor(Color.DARK_GRAY);
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(2));
            g.drawRect(x, y, breedte, hoogte);
            g2.setStroke(new BasicStroke(1));

            // Label
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            String label = getLabel(area);
            FontMetrics fm = g.getFontMetrics();

            int textX = x + (breedte - fm.stringWidth(label)) / 2;
            int textY = y + (hoogte + fm.getAscent()) / 2 - 2;

            g.drawString(label, textX, textY);
        }

        // === TEKEN LIFT (BEWEGEND OBJECT) ===
        if (lift != null) {
            int liftX = (int)(lift.getX() * VAKJE_GROOTTE) + offsetX - VAKJE_GROOTTE/2;
            int liftY = (int)(lift.getY() * VAKJE_GROOTTE) + offsetY - VAKJE_GROOTTE/2;

            g.setColor(new Color(200, 100, 0));  // Oranje
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

        // Teken alle personen
        for (Persoon persoon : hotel.getPersonen()) {
            if (persoon instanceof Gast) {
                Gast gast = (Gast) persoon;
                int px = (int)(gast.getX() * VAKJE_GROOTTE) + offsetX - 8;
                int py = (int)(gast.getY() * VAKJE_GROOTTE) + offsetY - 8;

                g.setColor(gast.getKleur());
                g.fillOval(px, py, 16, 16);

                g.setColor(new Color(
                        Math.max(0, gast.getKleur().getRed() - 80),
                        Math.max(0, gast.getKleur().getGreen() - 80),
                        Math.max(0, gast.getKleur().getBlue() - 80)
                ));
                g.drawOval(px, py, 16, 16);

                // Naam + activiteit boven de gast
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 9));
                String label = gast.getNaam();
                if (!gast.getHuidigeActiviteit().isEmpty()) {
                    label += " " + gast.getHuidigeActiviteit();
                }
                g.drawString(label, px - 5, py - 5);
            } else if (persoon instanceof Schoonmaker) {
                Schoonmaker schoonmaker = (Schoonmaker) persoon;
                int px = (int)(schoonmaker.getX() * VAKJE_GROOTTE) + offsetX - 8;
                int py = (int)(schoonmaker.getY() * VAKJE_GROOTTE) + offsetY - 8;

                g.setColor(schoonmaker.getKleur());
                g.fillRect(px, py, 16, 16);

                g.setColor(Color.BLACK);
                g.drawRect(px, py, 16, 16);

                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 10));
                g.drawString("S", px + 5, py + 8);
                
                // Activiteit label voor schoonmaker
                String label = "CM";
                if (!schoonmaker.getHuidigeActiviteit().isEmpty()) {
                    label += " " + schoonmaker.getHuidigeActiviteit();
                }
                g.setFont(new Font("Arial", Font.PLAIN, 8));
                g.drawString(label, px - 5, py - 3);
            }
        }

        // === US4.4: LEGENDA TOEVOEGEN ===
        tekenLegenda(g);

        // toon status (pause/active)
        if (!running) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("PAUSED", getWidth() / 2 - 70, getHeight() / 2);
        }
    }

    /**
     * Hulpmethode voor US4.4: Tekent een legenda zodat de gebruiker de simulatie begrijpt.
     */
    private void tekenLegenda(Graphics g) {
        int lx = 10;
        int ly = getHeight() - 70;

        // Achtergrond legenda
        g.setColor(new Color(255, 255, 255, 220));
        g.fillRect(lx, ly, 130, 60);
        g.setColor(Color.BLACK);
        g.drawRect(lx, ly, 130, 60);

        g.setFont(new Font("Arial", Font.PLAIN, 11));

        // Gast
        g.setColor(Color.BLUE);
        g.fillOval(lx + 10, ly + 10, 10, 10);
        g.setColor(Color.BLACK);
        g.drawString("Gast", lx + 30, ly + 20);

        // Schoonmaker
        g.setColor(Color.DARK_GRAY);
        g.fillRect(lx + 10, ly + 25, 10, 10);
        g.setColor(Color.BLACK);
        g.drawString("Schoonmaker", lx + 30, ly + 35);

        // Lift
        g.setColor(new Color(200, 100, 0));
        g.fillRect(lx + 10, ly + 40, 10, 10);
        g.setColor(Color.BLACK);
        g.drawString("Lift", lx + 30, ly + 50);
    }
    
    // === US2.1: MOUSE EVENT HANDLERS ===
    
    /**
     * Detecteert clicks op personen, kamers en lobby
     * Priority: Personen > Kamers > Lobby
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (hotel == null) return;
        
        int clickX = e.getX();
        int clickY = e.getY();

        // Bereken offsets
        int hotelBreedte = hotel.getBreedte() * VAKJE_GROOTTE;
        int hotelHoogte = hotel.getHoogte() * VAKJE_GROOTTE;
        int offsetX = (getWidth() - hotelBreedte) / 2;
        int offsetY = (getHeight() - hotelHoogte) / 2;
        
        // PRIORITY 1: Check personen (gasten en schoonmakers)
        for (Persoon persoon : hotel.getPersonen()) {
            if (isClickOnPerson(persoon, clickX, clickY, offsetX, offsetY)) {
                System.out.println("[Click] 👤 Persoon aangeklikt: " + persoon.getNaam());
                if (onPersonClick != null) {
                    onPersonClick.accept(persoon);
                }
                return;
            }
        }

        // PRIORITY 2: Check kamers (rooms)
        for (Area area : hotel.getAreas()) {
            if ("Room".equals(area.getAreaType())) {
                if (isClickOnArea(area, clickX, clickY, offsetX, offsetY)) {
                    // Zoek de kamer met deze area
                    for (Kamer k : hotel.getKamers()) {
                        if (k.getArea() != null && k.getArea().equals(area)) {
                            System.out.println("[Click] 🚪 Kamer aangeklikt: " + k.getKamernummer());
                            if (onRoomClick != null) {
                                onRoomClick.accept(k);
                            }
                            return;
                        }
                    }
                }
            }
        }

        // PRIORITY 3: Check lobby
        for (Area area : hotel.getAreas()) {
            if ("Lobby".equals(area.getAreaType())) {
                if (isClickOnArea(area, clickX, clickY, offsetX, offsetY)) {
                    System.out.println("[US2.1] 🏨 Lobby aangeklikt - Gastenoverzicht openen");
                    if (onLobbyClick != null) {
                        onLobbyClick.run();
                    }
                    return;
                }
            }
        }
    }

    /**
     * Helpermethod: Check of click op een persoon valt (cirkeldetectie)
     */
    private boolean isClickOnPerson(Persoon persoon, int clickX, int clickY, int offsetX, int offsetY) {
        int px = (int)(persoon.getX() * VAKJE_GROOTTE) + offsetX - 8;
        int py = (int)(persoon.getY() * VAKJE_GROOTTE) + offsetY - 8;

        // Gast: cirkel met straal ~8
        if (persoon instanceof Gast) {
            int centerX = px + 8;
            int centerY = py + 8;
            int distance = (int) Math.sqrt(Math.pow(clickX - centerX, 2) + Math.pow(clickY - centerY, 2));
            return distance <= 10;  // Margin voor usability
        }

        // Schoonmaker: vierkant 16x16
        if (persoon instanceof Schoonmaker) {
            return clickX >= px && clickX <= px + 16 && clickY >= py && clickY <= py + 16;
        }

        return false;
    }

    /**
     * Helpermethod: Check of click op een area (rechthoek) valt
     */
    private boolean isClickOnArea(Area area, int clickX, int clickY, int offsetX, int offsetY) {
        int x = (area.getX() - 1) * VAKJE_GROOTTE + offsetX;
        int y = (area.getY() - 1) * VAKJE_GROOTTE + offsetY;
        int breedte = area.getBreedte() * VAKJE_GROOTTE;
        int hoogte = area.getHoogte() * VAKJE_GROOTTE;

        return clickX >= x && clickX <= x + breedte && clickY >= y && clickY <= y + hoogte;
    }
    
    @Override
    public void mousePressed(MouseEvent e) {}
    
    @Override
    public void mouseReleased(MouseEvent e) {}
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e) {}
    
    @Override
    public void mouseDragged(MouseEvent e) {}
    
    @Override
    public void mouseMoved(MouseEvent e) {}

    private String getLabel(Area area) {
        switch (area.getAreaType()) {
            case "Room":
                return area.getClassification() != null ?
                        area.getClassification().replace(" Star", "★") : "Kamer";
            case "Cinema": return "Bioscoop";
            case "Restaurant": return "Restaurant";
            case "Fitness": return "Fitness";
            case "Lobby": return "Lobby";
            case "Schacht": return "Schacht";
            case "Lift": return "Lift";
            case "Staircase": return "Trap";
            case "Storage": return "Opslag";
            default: return area.getAreaType();
        }
    }

    private Color getKleur(Area area) {
        switch (area.getAreaType()) {
            case "Room":
                String c = area.getClassification();
                if (c == null) return new Color(200, 230, 255);
                if (c.contains("1")) return new Color(200, 230, 255);
                if (c.contains("2")) return new Color(150, 200, 255);
                if (c.contains("3")) return new Color(100, 170, 255);
                if (c.contains("4")) return new Color(50, 130, 255);
                if (c.contains("5")) return new Color(0, 80, 200);
                return new Color(200, 230, 255);
            case "Lobby": return new Color(255, 220, 50);
            case "Schacht": return new Color(220, 220, 220);
            case "Lift": return new Color(255, 150, 0);
            case "Staircase": return new Color(100, 200, 100);
            case "Restaurant": return new Color(255, 100, 100);
            case "Cinema": return new Color(200, 100, 200);
            case "Fitness": return new Color(100, 220, 180);
            case "Storage": return new Color(180, 140, 100);
            default: return Color.WHITE;
        }
    }
}