package model;

import java.util.ArrayList;
import java.util.List;

/**
 * EventBus - Centraal event management systeem
 * Events kunnen door de Simulator of andere klasses getriggerd worden
 * Alle TickListeners worden op de hoogte gesteld
 */
public class EventBusImpl {
    
    private List<TickListener> listeners = new ArrayList<>();
    private List<String> eventLog = new ArrayList<>();
    
    /**
     * Subscribe een listener op alle events
     */
    public void subscribe(TickListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            logEvent("SUBSCRIBE: " + listener.getClass().getSimpleName());
        }
    }
    
    /**
     * Unsubscribe een listener
     */
    public void unsubscribe(TickListener listener) {
        listeners.remove(listener);
        logEvent("UNSUBSCRIBE: " + listener.getClass().getSimpleName());
    }
    
    /**
     * Publish een event naar alle listeners
     */
    public void publishEvent(String eventName, Object data) {
        System.out.println("\n🎬 EVENT PUBLISHED: " + eventName);
        logEvent("EVENT: " + eventName);
        
        // Notificeer alle listeners
        for (TickListener listener : listeners) {
            System.out.println("  → Notificeer: " + listener.getClass().getSimpleName());
        }
    }
    
    /**
     * Log een event voor debugging
     */
    private void logEvent(String message) {
        eventLog.add("[" + System.currentTimeMillis() + "] " + message);
    }
    
    /**
     * Geef event log terug
     */
    public List<String> getEventLog() {
        return new ArrayList<>(eventLog);
    }
    
    /**
     * Wis event log
     */
    public void clearEventLog() {
        eventLog.clear();
    }
}

