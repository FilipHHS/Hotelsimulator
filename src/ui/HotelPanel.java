package ui;

import model.Area;
import model.Hotel;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class HotelPanel extends JPanel {

    private Hotel hotel;
    private final int VAKJE_GROOTTE = 50;

    public HotelPanel(Hotel hotel) {
        this.hotel = hotel;
        updateDimensions();
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
        updateDimensions();
        repaint();
    }

    private void updateDimensions() {
        int breedte = hotel.getBreedte() * VAKJE_GROOTTE;
        int hoogte = hotel.getHoogte() * VAKJE_GROOTTE;
        this.setPreferredSize(new Dimension(breedte, hoogte));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Teken eerst de achtergrond (gangen)
        g.setColor(new Color(220, 220, 220));
        g.fillRect(0, 0, hotel.getBreedte() * VAKJE_GROOTTE, hotel.getHoogte() * VAKJE_GROOTTE);

        // Teken grid lijnen voor gangen
        g.setColor(new Color(200, 200, 200));
        for (int y = 0; y < hotel.getHoogte(); y++) {
            for (int x = 0; x < hotel.getBreedte(); x++) {
                g.drawRect(x * VAKJE_GROOTTE, y * VAKJE_GROOTTE, VAKJE_GROOTTE, VAKJE_GROOTTE);
            }
        }

        // Teken elke area als één groot blok
        List<Area> areas = hotel.getAreas();
        for (Area area : areas) {
            int x = (area.getX() - 1) * VAKJE_GROOTTE;
            int y = (area.getY() - 1) * VAKJE_GROOTTE;
            int breedte = area.getBreedte() * VAKJE_GROOTTE;
            int hoogte = area.getHoogte() * VAKJE_GROOTTE;

            // Vul met kleur
            g.setColor(getKleur(area));
            g.fillRect(x, y, breedte, hoogte);

            // Teken rand om de hele area
            g.setColor(Color.DARK_GRAY);
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(2));
            g.drawRect(x, y, breedte, hoogte);
            g2.setStroke(new BasicStroke(1));

            // Teken label in het midden
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            String label = getLabel(area);
            FontMetrics fm = g.getFontMetrics();
            int textX = x + (breedte - fm.stringWidth(label)) / 2;
            int textY = y + (hoogte + fm.getAscent()) / 2 - 2;
            g.drawString(label, textX, textY);
        }
    }

    private String getLabel(Area area) {
        switch (area.getAreaType()) {
            case "Room": return area.getClassification() != null ?
                    area.getClassification().replace(" Star", "★") : "Kamer";
            case "Cinema": return "Bioscoop";
            case "Restaurant": return "Restaurant";
            case "Fitness": return "Fitness";
            case "Lobby": return "Lobby";
            case "Lift": return "Lift";
            case "Staircase": return "Trap";
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
            case "Lobby":    return new Color(255, 220, 50);
            case "Lift":     return new Color(255, 150, 0);
            case "Staircase": return new Color(100, 200, 100);
            case "Restaurant": return new Color(255, 100, 100);
            case "Cinema":   return new Color(200, 100, 200);
            case "Fitness":  return new Color(100, 220, 180);
            default:         return Color.WHITE;
        }
    }
}