package model;

import ui.HotelPanel;

public class Simulator {

    private boolean running;
    private HotelPanel hotelPanel;
    private Hotel hotel;
    private SimulationClock clock;
    private static final long TICK_INTERVAL = 100; // 100 ms tussen ticks

    public Simulator(Hotel hotel, HotelPanel hotelPanel) {
        this.hotel = hotel;
        this.hotelPanel = hotelPanel;
        this.running = false;
        this.clock = new SimulationClock(TICK_INTERVAL);
    }

    public void start() {
        running = true;
        clock.start();
        hotelPanel.setRunning(true);
    }

    public void pause() {
        running = false;
        clock.stop();
        hotelPanel.setRunning(false);
    }

    public boolean isRunning() {
        return running;
    }
    
    /**
     * Haal de simulatieklok op.
     */
    public SimulationClock getClock() {
        return clock;
    }
    
    /**
     * Reset de klok (gebruikt bij het laden van een nieuwe layout).
     */
    public void resetClock() {
        clock.reset();
    }

    public void tick() {
        // Controleer of de klok een tick moet doen
        if (clock.tick()) {
            // UPDATE FASE - Alleen als klok tick doet
            if (running) {
                for (Persoon persoon : hotel.getPersonen()) {
                    if (persoon instanceof Gast) {
                        ((Gast) persoon).update();
                    }
                }
            }
        }
        
        // RENDER FASE - Altijd tekenen (ook wanneer pauzeerd!)
        hotelPanel.repaint();
    }
}