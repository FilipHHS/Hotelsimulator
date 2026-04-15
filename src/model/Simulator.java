package model;

import model.SimulationClock;
import model.HTEClock;
import model.TickListener;
import model.Persoon;
import ui.HotelPanel;

/**
 * Simulator - Stuurt de simulatie aan via HTE-ticks
 */
public class Simulator {

    private boolean running;
    private HotelPanel hotelPanel;
    private Hotel hotel;
    private SimulationClock clock;
    private HTEClock hteClock;

    private static final long TICK_INTERVAL = 100; // 100 ms

    public Simulator(Hotel hotel, HotelPanel hotelPanel) {
        this.hotel = hotel;
        this.hotelPanel = hotelPanel;
        this.running = false;
        this.clock = new SimulationClock(TICK_INTERVAL);
        this.hteClock = new HTEClock();
        
        // Registreer alle personen als listeners op HTEClock
        for (Persoon p : hotel.getPersonen()) {
            if (p instanceof TickListener) {
                hteClock.addListener((TickListener) p);
            }
        }
    }

    public void start() {
        running = true;
        clock.start(); // start interne tijdmeting
        hotelPanel.setRunning(true);
        System.out.println("[Simulator] Simulatie gestart");
    }

    public void pause() {
        running = false;
        clock.stop();
        hotelPanel.setRunning(false);
        System.out.println("[Simulator] Simulatie gepauzeerd");
    }

    public boolean isRunning() {
        return running;
    }

    public SimulationClock getClock() {
        return clock;
    }

    public HTEClock getHteClock() {
        return hteClock;
    }

    public void resetClock() {
        clock.reset();
        hteClock = new HTEClock();
        for (Persoon p : hotel.getPersonen()) {
            if (p instanceof TickListener) {
                hteClock.addListener((TickListener) p);
            }
        }
    }

    /**
     * HOOFDMETHODE: Wordt aangeroepen door Timer in main
     */
    public void tick() {

        // Check of er een echte HTE-tick plaatsvindt
        if (running && clock.tick()) {
            System.out.println("[Simulator] HTE-tick #" + clock.getTimestep());
            
            // Roep HTEClock.tick() aan (die roept alle listeners.onTick() aan)
            hteClock.tick();
        }

        // UI altijd updaten (ook bij pauze)
        hotelPanel.repaint();
    }
}