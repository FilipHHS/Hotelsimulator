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
            }
        }

        // 🔥 BELANGRIJK: dit triggert jouw paintComponent()
        hotelPanel.repaint();
    }
}