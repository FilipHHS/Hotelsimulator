package model;

import ui.HotelPanel;

public class Simulator {

    private boolean running;
    private HotelPanel hotelPanel;
    private Hotel hotel;

    public Simulator(Hotel hotel, HotelPanel hotelPanel) {
        this.hotel = hotel;
        this.hotelPanel = hotelPanel;
        this.running = false;
    }

    public void start() {
        running = true;
        hotelPanel.setRunning(true);
    }

    public void pause() {
        running = false;
        hotelPanel.setRunning(false);
    }

    public boolean isRunning() {
        return running;
    }

    public void tick() {
<<<<<<< HEAD
        if (running) {
            // UPDATE FASE - Alleen als simulatie loopt
            for (Persoon persoon : hotel.getPersonen()) {
                if (persoon instanceof Gast) {
                    ((Gast) persoon).update();
                }
=======
        if (!running) return;

        /// new
        int testX = 0;
        testX++;
        System.out.println("Persoon beweegt naar: " + testX);

        hotelPanel.repaint();

        // Update all personen (guests move according to their logic)
        for (Persoon persoon : hotel.getPersonen()) {
            if (persoon instanceof Gast) {
                ((Gast) persoon).update();
>>>>>>> 8412da97cd77cb7bffadff3473854c797a572c1b
            }
        }
        
        // RENDER FASE - Altijd tekenen (ook wanneer pauzeerd!)
        hotelPanel.repaint();  // ← Nu altijd, zodat PAUSED overlay verschijnt
    }
}