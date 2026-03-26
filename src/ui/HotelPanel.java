package ui;

import model.Area;
import model.Hotel;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dit paneel is verantwoordelijk voor het visueel weergeven van het hotel.
 * Het tekent een grid met daarin de verschillende ruimtes (areas) zoals kamers, de lobby en liften.
 */
public class HotelPanel extends JPanel {

    // Het hotel-object dat alle data bevat over de afmetingen en de ruimtes
    private Hotel hotel;

    // De vaste grootte van één "vakje" (gridcel) in pixels
    private final int VAKJE_GROOTTE = 50;

    /**
     * Constructor die het paneel initialiseert met een hotel.
     */
    public HotelPanel(Hotel hotel) {
        this.hotel = hotel;
        updateDimensions(); // Zorg dat het paneel direct de juiste grootte krijgt
    }

    /**
     * Update het huidige hotel met een nieuw hotel en hertekent het paneel.
     */
    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
        updateDimensions();
        repaint(); // Vraagt Swing om paintComponent() opnieuw uit te voeren met de nieuwe data
    }

    /**
     * Berekent de benodigde breedte en hoogte van dit paneel op basis van
     * de hotelafmetingen en de ingestelde grootte per vakje.
     */
    private void updateDimensions() {
        int breedte = hotel.getBreedte() * VAKJE_GROOTTE;
        int hoogte = hotel.getHoogte() * VAKJE_GROOTTE;
        this.setPreferredSize(new Dimension(breedte, hoogte));
    }

    /**
     * Dit is de kernmethode waar het daadwerkelijke tekenen (renderen) gebeurt.
     */
    @Override
    protected void paintComponent(Graphics g) {
        // Altijd de super-methode aanroepen om de standaard achtergrond netjes leeg te maken
        super.paintComponent(g);

        // 1. Teken eerst de algemene achtergrond (dit fungeert als de vloer/gangen)
        g.setColor(new Color(220, 220, 220)); // Lichtgrijs
        g.fillRect(0, 0, hotel.getBreedte() * VAKJE_GROOTTE, hotel.getHoogte() * VAKJE_GROOTTE);

        // 2. Teken de gridlijnen, zodat we een ruitjespatroon zien voor de gangen
        g.setColor(new Color(200, 200, 200));
        for (int y = 0; y < hotel.getHoogte(); y++) {
            for (int x = 0; x < hotel.getBreedte(); x++) {
                g.drawRect(x * VAKJE_GROOTTE, y * VAKJE_GROOTTE, VAKJE_GROOTTE, VAKJE_GROOTTE);
            }
        }

        // 3. Loop door alle specifieke ruimtes (areas) van het hotel en teken deze over het grid heen
        List<Area> areas = hotel.getAreas();
        for (Area area : areas) {
            // Bereken de X en Y positie in pixels.
            // We doen -1 omdat de logica waarschijnlijk 1-based is (begint bij 1),
            // terwijl het tekenen 0-based is (begint bij coördinaat 0).
            int x = (area.getX() - 1) * VAKJE_GROOTTE;
            int y = (area.getY() - 1) * VAKJE_GROOTTE;

            // Bepaal de totale breedte en hoogte van deze specifieke area in pixels
            int breedte = area.getBreedte() * VAKJE_GROOTTE;
            int hoogte = area.getHoogte() * VAKJE_GROOTTE;

            // --- A. Vul de area met de juiste achtergrondkleur ---
            g.setColor(getKleur(area));
            g.fillRect(x, y, breedte, hoogte);

            // --- B. Teken een donkere rand om de area heen ---
            g.setColor(Color.DARK_GRAY);
            Graphics2D g2 = (Graphics2D) g; // Cast naar Graphics2D voor dikkere lijnen
            g2.setStroke(new BasicStroke(2)); // Zet lijndikte op 2 pixels
            g.drawRect(x, y, breedte, hoogte);
            g2.setStroke(new BasicStroke(1)); // Zet lijndikte weer terug naar standaard (1)

            // --- C. Teken de naam/label van de area precies in het midden ---
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            String label = getLabel(area);
            FontMetrics fm = g.getFontMetrics(); // Helpt ons meten hoe breed/hoog de tekst is

            // Bereken het exacte middelpunt voor de tekst
            int textX = x + (breedte - fm.stringWidth(label)) / 2;
            int textY = y + (hoogte + fm.getAscent()) / 2 - 2;

            g.drawString(label, textX, textY);
        }
    }

    /**
     * Vertaalt het type van een Area naar een leesbaar label voor op het scherm.
     */
    private String getLabel(Area area) {
        switch (area.getAreaType()) {
            case "Room":
                // Vervangt de tekst " Star" door een echt ster-symbooltje, of toont "Kamer" als er geen classificatie is
                return area.getClassification() != null ?
                        area.getClassification().replace(" Star", "★") : "Kamer";
            case "Cinema": return "Bioscoop";
            case "Restaurant": return "Restaurant";
            case "Fitness": return "Fitness";
            case "Lobby": return "Lobby";
            case "Lift": return "Lift";
            case "Staircase": return "Trap";
            case "Storage": return "Opslag";
            default: return area.getAreaType(); // Fallback naar de ruwe naam
        }
    }

    /**
     * Bepaalt de kleur van een Area op basis van het type en (in het geval van kamers) de classificatie.
     */
    private Color getKleur(Area area) {
        switch (area.getAreaType()) {
            case "Room":
                String c = area.getClassification();
                if (c == null) return new Color(200, 230, 255);

                // Hoe meer sterren, hoe donkerder blauw de kamer wordt
                if (c.contains("1")) return new Color(200, 230, 255);
                if (c.contains("2")) return new Color(150, 200, 255);
                if (c.contains("3")) return new Color(100, 170, 255);
                if (c.contains("4")) return new Color(50, 130, 255);
                if (c.contains("5")) return new Color(0, 80, 200);

                return new Color(200, 230, 255); // Standaardkleur voor kamers zonder geldige sterren
            case "Lobby":    return new Color(255, 220, 50); // Geel
            case "Lift":     return new Color(255, 150, 0);  // Oranje
            case "Staircase": return new Color(100, 200, 100); // Groen
            case "Restaurant": return new Color(255, 100, 100); // Rood/Roze
            case "Cinema":   return new Color(200, 100, 200); // Paars
            case "Fitness":  return new Color(100, 220, 180); // Mintgroen
            case "Storage": return new Color(180, 140, 100);  // Bruin
            default:         return Color.WHITE; // Fallback kleur
        }
    }
}