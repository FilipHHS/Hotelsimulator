package model;

import hotelevents.HotelEvent;
import hotelevents.HotelEventListener;
import hotelevents.HotelEventType;
import java.util.ArrayList;
import java.util.List;

/**
 * EventBus - Centraal event management systeem
 * Integreert HotelEventType en HotelEventListener van school project
 * Events kunnen door de Simulator of andere klasses getriggerd worden
 * * UPDATE: Toegevoegd voor US4.1 Externe DLL Events (systemState & handleExternalDLLEvent)
 */
public class EventBusImpl implements HotelEventListener {

    private final List<TickListener> listeners = new ArrayList<>();
    private final List<HotelEventListener> hotelListeners = new ArrayList<>();
    private final List<String> eventLog = new ArrayList<>();
    private final List<String> errorLog = new ArrayList<>();
    private int eventCounter = 0;

    // --- US4.1 STATUS MANAGEMENT ---
    // Dit houdt de daadwerkelijke status van het systeem bij volgens de acceptatiecriteria
    private String systemState = "IDLE";

    /**
     * US4.1: Geeft de huidige status van het systeem terug voor de acceptatietest
     */
    public String getSystemState() {
        return this.systemState;
    }

    /**
     * US4.1: Verwerkt de specifieke events uit de externe library (DLL)
     * en voert de bijbehorende interne logica uit.
     * US4.2.A: Robuuste foutafhandeling voor null/corrupte data
     */
    public void handleExternalDLLEvent(String dllEventName) {
        try {
            // NULL check
            if (dllEventName == null || dllEventName.trim().isEmpty()) {
                String errorMsg = "⚠️ [EventBus] NULL/EMPTY event ontvangen van DLL";
                System.err.println(errorMsg);
                logError(errorMsg);
                return;
            }

            eventCounter++;
            logEvent("DLL_EVENT: " + dllEventName);
            System.out.println("\n⚙️ [EventBus] DLL Event ontvangen: " + dllEventName);

            // Schakel tussen de specifieke events genoemd in de User Story
            switch (dllEventName) {
                case "Initialization":
                    System.out.println("  → [Interne Logica] Systeem initialiseren en kamers controleren...");
                    this.systemState = "INITIALIZING";
                    break;

                case "DataReceived":
                    System.out.println("  → [Interne Logica] Externe data inladen in de hotelsimulator...");
                    this.systemState = "PROCESSING_DATA";
                    break;

                case "ProcessComplete":
                    System.out.println("  → [Interne Logica] Sequentie succesvol afgerond!");
                    // HARDE EIS: Systeem status markeren als "Sequence Processed"
                    this.systemState = "Sequence Processed";
                    break;

                default:
                    System.out.println("  → [Interne Logica] Onbekend DLL event overgeslagen.");
                    break;
            }
        } catch (Exception e) {
            String errorMsg = "❌ Exception in handleExternalDLLEvent: " + e.getMessage();
            System.err.println(errorMsg);
            logError(errorMsg);
        }
    }

    /**
     * Subscribe een listener op alle events (legacy)
     */
    public void subscribe(TickListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            logEvent("SUBSCRIBE: " + listener.getClass().getSimpleName());
        }
    }

    /**
     * Subscribe een HotelEventListener
     */
    public void registerHotelEventListener(HotelEventListener listener) {
        if (!hotelListeners.contains(listener)) {
            hotelListeners.add(listener);
            logEvent("REGISTER HOTEL LISTENER: " + listener.getClass().getSimpleName());
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
     * Deregister een HotelEventListener
     */
    public void deregisterHotelEventListener(HotelEventListener listener) {
        hotelListeners.remove(listener);
        logEvent("DEREGISTER HOTEL LISTENER: " + listener.getClass().getSimpleName());
    }

    /**
     * Publish een event naar alle listeners (legacy string-based)
     */
    public void publishEvent(String eventName, Object data) {
        System.out.println("\n🎬 EVENT PUBLISHED: " + eventName);
        logEvent("EVENT: " + eventName);

        for (TickListener listener : listeners) {
            System.out.println("  → Notificeer: " + listener.getClass().getSimpleName());
        }
    }

    /**
     * Trigger een HotelEvent met type, guestId, en data
     * US4.2.A: Exception handling
     */
    public void triggerHotelEvent(HotelEventType eventType, int guestId, int data) {
        triggerHotelEvent(eventType, guestId, (int) System.currentTimeMillis(), data);
    }

    /**
     * Trigger een HotelEvent met type, guestId, tijd, en data
     * US4.2.A: Robuuste foutafhandeling
     */
    public void triggerHotelEvent(HotelEventType eventType, int guestId, int time, int data) {
        try {
            // NULL check
            if (eventType == null) {
                String errorMsg = "⚠️ [EventBus] NULL event type bij triggerHotelEvent";
                System.err.println(errorMsg);
                logError(errorMsg);
                return;
            }

            eventCounter++;
            HotelEvent event = new HotelEvent(guestId, eventType, time, data);

            String emoji = getEventEmoji(eventType);
            System.out.println("\n" + emoji + " HOTEL EVENT TRIGGERED: " + eventType.name());
            logEvent("HOTEL_EVENT: " + eventType.name() + " (Guest: " + guestId + ", Data: " + data + ")");

            // Notificeer alle hotel event listeners
            for (HotelEventListener listener : hotelListeners) {
                try {
                    System.out.println("  → Notificeer: " + listener.getClass().getSimpleName());
                    listener.notify(event);
                } catch (Exception e) {
                    String errorMsg = "⚠️ Error notifying listener: " + e.getMessage();
                    System.err.println(errorMsg);
                    logError(errorMsg);
                }
            }

            // Ook het centrale systeem notificeren
            notify(event);
        } catch (Exception e) {
            String errorMsg = "❌ Exception in triggerHotelEvent: " + e.getMessage();
            System.err.println(errorMsg);
            logError(errorMsg);
        }
    }

    /**
     * HotelEventListener interface - ontvang en verwerk events
     * US4.2.A: Robuuste foutafhandeling voor null/corrupte event data
     */
    @Override
    public void notify(HotelEvent event) {
        try {
            // NULL check
            if (event == null) {
                String errorMsg = "⚠️ [EventBus] NULL HotelEvent ontvangen";
                System.err.println(errorMsg);
                logError(errorMsg);
                return;
            }

            // NULL type check
            if (event.getEventType() == null) {
                String errorMsg = "⚠️ [EventBus] Null event type in HotelEvent";
                System.err.println(errorMsg);
                logError(errorMsg);
                return;
            }

            switch (event.getEventType()) {
                case CHECK_IN:
                    logEvent("✅ CHECK_IN: Guest " + event.getGuestId() + " checked in");
                    break;
                case CHECK_OUT:
                    logEvent("🔓 CHECK_OUT: Guest " + event.getGuestId() + " checked out");
                    break;
                case CLEANING_EMERGENCY:
                    logEvent("🚨 CLEANING_EMERGENCY: Room " + event.getData() + " needs urgent cleaning");
                    break;
                case EVACUATE:
                    logEvent("🏃 EVACUATE: Emergency evacuation initiated");
                    break;
                case GODZILLA:
                    logEvent("🦖 GODZILLA: Monster attack! Guest " + event.getGuestId());
                    break;
                case NEED_FOOD:
                    logEvent("🍽️ NEED_FOOD: Guest " + event.getGuestId() + " wants food");
                    break;
                case GOTO_CINEMA:
                    logEvent("🎬 GOTO_CINEMA: Guest " + event.getGuestId() + " going to cinema");
                    break;
                case GOTO_FITNESS:
                    logEvent("💪 GOTO_FITNESS: Guest " + event.getGuestId() + " going to fitness");
                    break;
                case START_CINEMA:
                    logEvent("🎞️ START_CINEMA: Cinema show starting");
                    break;
                case NONE:
                default:
                    logEvent("⚠️ NONE/UNKNOWN: Event type not handled");
                    break;
            }
        } catch (Exception e) {
            String errorMsg = "❌ Exception in notify: " + e.getMessage();
            System.err.println(errorMsg);
            logError(errorMsg);
        }
    }

    /**
     * Get emoji voor event type
     */
    private String getEventEmoji(HotelEventType type) {
        switch (type) {
            case CHECK_IN: return "✅";
            case CHECK_OUT: return "🔓";
            case CLEANING_EMERGENCY: return "🚨";
            case EVACUATE: return "🏃";
            case GODZILLA: return "🦖";
            case NEED_FOOD: return "🍽️";
            case GOTO_CINEMA: return "🎬";
            case GOTO_FITNESS: return "💪";
            case START_CINEMA: return "🎞️";
            default: return "⚠️";
        }
    }

    /**
     * Log een event voor debugging
     */
    private void logEvent(String message) {
        eventLog.add("[" + System.currentTimeMillis() + "] " + message);
    }

    /**
     * Log een error voor debugging
     */
    private void logError(String message) {
        errorLog.add("[" + System.currentTimeMillis() + "] " + message);
    }

    /**
     * Geef event log terug
     */
    public List<String> getEventLog() {
        return new ArrayList<>(eventLog);
    }

    /**
     * Geef error log terug
     */
    public List<String> getErrorLog() {
        return new ArrayList<>(errorLog);
    }

    /**
     * Wis event log
     */
    public void clearEventLog() {
        eventLog.clear();
    }

    /**
     * Wis error log
     */
    public void clearErrorLog() {
        errorLog.clear();
    }

    /**
     * Geef aantal getriggerde events
     */
    public int getEventCount() {
        return eventCounter;
    }

    private Simulator simulator;

    public void setSimulator(Simulator simulator) {
        this.simulator = simulator;
    }
}

