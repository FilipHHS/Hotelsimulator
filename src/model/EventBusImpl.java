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
    private int eventCounter = 0;

    // --- US4.1 STATUS MANAGEMENT ---
    // Dit houdt de daadwerkelijke status van het systeem bij volgens de acceptatiecriteria
    private String systemState = "IDLE";

    // --- US4.2.b EVENT VALIDATION ---
    // Referentie naar Simulator voor validatie van DLL-events op basis van running-status
    private Simulator simulator;

    /**
     * US4.1: Geeft de huidige status van het systeem terug voor de acceptatietest
     */
    public String getSystemState() {
        return this.systemState;
    }

    /**
     * US4.2.b: Set de Simulator referentie zodat EventBus de running-status kan checken
     * Dit wordt aangeroepen vanuit de Simulator constructor direct na EventBusImpl creatie
     */
    public void setSimulator(Simulator simulator) {
        this.simulator = simulator;
    }

    /**
     * US4.1 + US4.2.b: Verwerkt de specifieke events uit de externe library (DLL)
     * en voert de bijbehorende interne logica uit.
     * 
     * WIJZIGING US4.2.b:
     * - Controleert of binnenkomende DLL-events logisch zijn gegeven de huidige simulatiestatus
     * - Events die alleen in RUNNING geldig zijn (bijv "ProcessData") worden geweigerd als de simulator niet draait
     * - Illogical events worden gelogd en returnen vroeg zodat systemState niet wijzigt
     */
    public void handleExternalDLLEvent(String dllEventName) {
        eventCounter++;
        logEvent("DLL_EVENT: " + dllEventName);
        System.out.println("\n⚙️ [EventBus] DLL Event ontvangen: " + dllEventName);

        // --- US4.2.b VALIDATIE: Bepaal of de simulator draait ---
        boolean simIsRunning = false;
        if (this.simulator != null) {
            try {
                simIsRunning = this.simulator.isRunning();
            } catch (Exception e) {
                // Defensive: als iets misgaat, behandel als niet-running
                System.err.println("⚠️ [EventBus] Fout bij status-check van Simulator: " + e.getMessage());
                simIsRunning = false;
            }
        }

        // --- US4.2.b VALIDATIE: Check of event toegestaan is voor huidige status ---
        // Events die ALLEEN in RUNNING geldig zijn:
        // "ProcessData" mag alleen verwerkt worden als simulatie draait
        if ("ProcessData".equalsIgnoreCase(dllEventName)) {
            if (!simIsRunning) {
                // ❌ ILLOGICAL: Event geweigerd omdat simulator niet in RUNNING staat
                String warningMsg = "ILLOGICAL_DLL_EVENT: '" + dllEventName + "' geweigerd — simulatie niet in RUNNING status";
                System.out.println("  ❌ [VALIDATIE] " + warningMsg);
                logEvent(warningMsg);
                // BELANGRIJK: Return ZONDER systemState te wijzigen!
                // Dit zorgt ervoor dat de status "Stopped" / "IDLE" blijft zoals het was.
                return;
            }
        }

        // --- Bestaande event-verwerking (alleen bereikt voor geldig event) ---
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

            case "ProcessData":
                // Dit event wordt hier bereikt ALLEEN als validatie passed (simulatie draait)
                System.out.println("  → [Interne Logica] Verwerking van externe data...");
                this.systemState = "PROCESSING_DATA";
                break;

            default:
                System.out.println("  → [Interne Logica] Onbekend DLL event overgeslagen.");
                break;
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
     */
    public void triggerHotelEvent(HotelEventType eventType, int guestId, int data) {
        triggerHotelEvent(eventType, guestId, (int) System.currentTimeMillis(), data);
    }

    /**
     * Trigger een HotelEvent met type, guestId, tijd, en data
     */
    public void triggerHotelEvent(HotelEventType eventType, int guestId, int time, int data) {
        eventCounter++;
        HotelEvent event = new HotelEvent(guestId, eventType, time, data);

        String emoji = getEventEmoji(eventType);
        System.out.println("\n" + emoji + " HOTEL EVENT TRIGGERED: " + eventType.name());
        logEvent("HOTEL_EVENT: " + eventType.name() + " (Guest: " + guestId + ", Data: " + data + ")");

        // Notificeer alle hotel event listeners
        for (HotelEventListener listener : hotelListeners) {
            System.out.println("  → Notificeer: " + listener.getClass().getSimpleName());
            listener.notify(event);
        }

        // Ook het centrale systeem notificeren
        notify(event);
    }

    /**
     * HotelEventListener interface - ontvang en verwerk events
     */
    @Override
    public void notify(HotelEvent event) {
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

    /**
     * Geef aantal getriggerde events
     */
    public int getEventCount() {
        return eventCounter;
    }
}