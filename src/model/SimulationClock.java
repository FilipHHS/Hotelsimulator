package model;

/**
 * HTE-klok die ticks genereert voor de simulatie.
 * Deze klok zorgt ervoor dat de simulatie in vaste tijdstappen verloopt.
 */
public class SimulationClock {
    
    private long timestep;  // Oplopende teller: t = 1, 2, 3...
    private boolean running;
    private long tickInterval; // Interval tussen ticks in milliseconden
    private long lastTickTime;
    
    /**
     * Constructor voor de SimulationClock.
     * @param tickInterval Interval tussen ticks in milliseconden (bijv. 100 ms)
     */
    public SimulationClock(long tickInterval) {
        this.timestep = 0;
        this.running = false;
        this.tickInterval = tickInterval;
        this.lastTickTime = System.currentTimeMillis();
    }
    
    /**
     * Start de klok.
     */
    public void start() {
        running = true;
        lastTickTime = System.currentTimeMillis();
    }
    
    /**
     * Stop de klok.
     */
    public void stop() {
        running = false;
    }
    
    /**
     * Reset de klok (timestep terug naar 0).
     */
    public void reset() {
        timestep = 0;
        running = false;
        lastTickTime = System.currentTimeMillis();
    }
    
    /**
     * Controleer of er een tick zou moeten plaatsvinden.
     * @return true als er een tick zou moeten plaatsvinden
     */
    public boolean shouldTick() {
        if (!running) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastTickTime;
        
        if (elapsedTime >= tickInterval) {
            return true;
        }
        return false;
    }
    
    /**
     * Voer een tick uit als deze nodig is.
     * @return true als er een tick heeft plaatsvonden
     */
    public boolean tick() {
        if (shouldTick()) {
            timestep++;
            lastTickTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }
    
    /**
     * Haal de huidige timestep op.
     * @return De huidige timestep (t = 1, 2, 3...)
     */
    public long getTimestep() {
        return timestep;
    }
    
    /**
     * Controleer of de klok draait.
     * @return true als de klok draait
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Stel het tick interval in.
     * @param tickInterval Nieuw interval in milliseconden
     */
    public void setTickInterval(long tickInterval) {
        this.tickInterval = tickInterval;
    }
    
    /**
     * Haal het tick interval op.
     * @return Het huidige interval in milliseconden
     */
    public long getTickInterval() {
        return tickInterval;
    }
}

