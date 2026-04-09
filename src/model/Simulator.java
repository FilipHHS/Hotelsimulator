package model;

import ui.HotelPanel;

/**
 * De Simulator klasse beheert de logica van de tijdstappen in het hotel.
 * Het zorgt ervoor dat gasten bewegen en dat het scherm ververst wordt.
 */
public class Simulator {

    private boolean running;        // Houdt bij of de simulatie momenteel loopt of gepauzeerd is
    private HotelPanel hotelPanel;  // Referentie naar de UI om deze te kunnen verversen (repaint)
    private Hotel hotel;            // Referentie naar de data (het hotel en de personen)
    private int speed = 500;        // De snelheid van de simulatie in milliseconden (standaard 500ms per tick)

    public Simulator(Hotel hotel, HotelPanel hotelPanel) {
        this.hotel = hotel;
        this.hotelPanel = hotelPanel;
        this.running = false; // Bij opstarten staat de simulatie standaard stil
    }

    /**
     * Zet de simulatie "aan".
     */
    public void start() {
        this.running = true;
        this.hotelPanel.setRunning(true);
    }

    /**
     * Zet de simulatie op pauze. De tick() methode zal nu niets meer doen.
     */
    public void pause() {
        this.running = false;
        this.hotelPanel.setRunning(false);
    }

    /**
     * Controleert of de simulatie momenteel actief is.
     * @return true als het loopt, anders false.
     */
    public boolean isRunning() {
        return this.running;
    }

    /**
     * US2.3: Methode om de snelheid van de simulatie aan te passen.
     * @param speed De nieuwe vertraging in milliseconden tussen elke tick.
     */
    public void setSpeed(int speed) {
        // Hoe lager de 'speed' (ms), hoe sneller de simulatie tikt
        this.speed = speed;
    }

    /**
     * Geeft de huidige ingestelde snelheid terug.
     * Handig om de GUI (bijv. de Timer in main) te synchroniseren.
     */
    public int getSpeed() {
        return this.speed;
    }

    /**
     * De kern van de simulatie. Deze methode wordt elke 'X' milliseconden aangeroepen.
     */
    public void tick() {
        // Alleen actie ondernemen als de simulator op 'start' staat
        if (this.running) {

            // Loop door alle aanwezige personen in het hotel
            for (Persoon persoon : this.hotel.getPersonen()) {
                // We controleren of de persoon een Gast is (omdat alleen gasten momenteel logica hebben)
                if (persoon instanceof Gast) {
                    // Update de positie van de gast (stappen zetten richting bestemming)
                    ((Gast) persoon).update();
                }
            }

            // Nadat alle posities zijn bijgewerkt, geven we de UI een seintje
            // De methode repaint() zorgt ervoor dat paintComponent() in HotelPanel wordt uitgevoerd
            this.hotelPanel.repaint();
        }
    }
}