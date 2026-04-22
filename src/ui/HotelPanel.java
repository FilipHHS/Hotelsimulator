package ui;

import model.Area;
import model.Hotel;
import model.Gast;
import model.Persoon;
import model.Lift;
import model.Schoonmaker;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dit paneel is verantwoordelijk voor het visueel weergeven van het hotel.
 */
public class HotelPanel extends JPanel {

    private Hotel hotel;
    private Lift lift;  // Referentie naar de lift om deze te tekenen

    private final int VAKJE_GROOTTE = 50;

    // status van simulatie (optioneel)
    private boolean running = true;

    public HotelPanel(Hotel hotel) {
        this.hotel = hotel;
        updateDimensions();
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
        // Dit vervangt de statische 1x1 Lift area uit Areas!
        if (lift != null) {
            // Teken lift als 1x1 vakje
            int liftX = (int)(lift.getX() * VAKJE_GROOTTE) + offsetX - VAKJE_GROOTTE/2;
            int liftY = (int)(lift.getY() * VAKJE_GROOTTE) + offsetY - VAKJE_GROOTTE/2;
            
            // Teken lift als oranje vierkant (VAKJE_GROOTTE groot)
            g.setColor(new Color(200, 100, 0));  // Oranje
            g.fillRect(liftX, liftY, VAKJE_GROOTTE, VAKJE_GROOTTE);
            
            // Rand
            g.setColor(Color.BLACK);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(2));
            g.drawRect(liftX, liftY, VAKJE_GROOTTE, VAKJE_GROOTTE);
            
            // Teken "L" erin (groot)
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
                // Gebruik doubles voor smooth pixel-gebaseerde coördinaten
                int px = (int)(gast.getX() * VAKJE_GROOTTE) + offsetX - 8;
                int py = (int)(gast.getY() * VAKJE_GROOTTE) + offsetY - 8;

                // Teken met gast's eigen kleur
                g.setColor(gast.getKleur());
                g.fillOval(px, py, 16, 16);

                // Teken rand (donkerder versie van dezelfde kleur)
                g.setColor(new Color(
                    Math.max(0, gast.getKleur().getRed() - 80),
                    Math.max(0, gast.getKleur().getGreen() - 80),
                    Math.max(0, gast.getKleur().getBlue() - 80)
                ));
                g.drawOval(px, py, 16, 16);

                // Teken naam als tooltip (optioneel, heel klein)
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.PLAIN, 8));
                g.drawString(gast.getNaam().substring(0, Math.min(3, gast.getNaam().length())), px + 2, py + 8);
            } else if (persoon instanceof Schoonmaker) {
                Schoonmaker schoonmaker = (Schoonmaker) persoon;
                // Teken schoonmaker op dezelfde manier als gast
                int px = (int)(schoonmaker.getX() * VAKJE_GROOTTE) + offsetX - 8;
                int py = (int)(schoonmaker.getY() * VAKJE_GROOTTE) + offsetY - 8;

                // Teken met schoonmaker's kleur (grijs)
                g.setColor(schoonmaker.getKleur());
                g.fillRect(px, py, 16, 16);  // Vierkant i.p.v. cirkel

                // Teken rand
                g.setColor(Color.BLACK);
                g.drawRect(px, py, 16, 16);

                // Teken label
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.PLAIN, 8));
                g.drawString("S", px + 5, py + 8);
            }
        }

        // toon status (pause/active)
        if (!running) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("PAUSED", getWidth() / 2 - 70, getHeight() / 2);
        }
    }

    private String getLabel(Area area) {
        // ...existing code...
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
            case "Schacht": return new Color(220, 220, 220);  // Grijze schacht
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