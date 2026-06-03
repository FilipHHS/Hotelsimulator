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
public class EventBusImpl implements HotelEventListener, IEventBus {

    private final List<TickListener> listeners = new ArrayList<>();
    private final List<HotelEventListener> hotelListeners = new ArrayList<>();
    private final List<String> eventLog = new ArrayList<>();
    private final List<String> errorLog = new ArrayList<>();
    private int eventCounter = 0;

    // --- US4.1 STATUS MANAGEMENT ---
    // Dit houdt de daadwerkelijke status van het systeem bij volgens de acceptatiecriteria
    private String systemState = "IDLE";

    private Simulator simulator;
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

            // --- US4.2.b VALIDATIE ---
            boolean simIsRunning = false;
            if (this.simulator != null) {
                try {
                    simIsRunning = this.simulator.isRunning();
                } catch (Exception e) {
                    System.err.println("⚠️ [EventBus] Fout bij status-check: " + e.getMessage());
                    simIsRunning = false;
                }
            }

            if ("ProcessData".equalsIgnoreCase(dllEventName)) {
                if (!simIsRunning) {
                    String warningMsg = "ILLOGICAL_DLL_EVENT: '" + dllEventName + "' geweigerd — simulatie niet in RUNNING status";
                    System.out.println("  ❌ [VALIDATIE] " + warningMsg);
                    logEvent(warningMsg);
                    return;
                }
            }

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
                    System.out.println("  → [Interne Logica] Verwerking van externe data...");
                    this.systemState = "PROCESSING_DATA";
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
                    if (event.getGuestId() == 0) {
                        logEvent("🍽️ NEED_FOOD: alle gasten naar restaurant (" + event.getData() + " gasten)");
                    } else {
                        logEvent("🍽️ NEED_FOOD: Guest " + event.getGuestId() + " wants food");
                    }
                    if (simulator != null) {
                        simulator.stuurAlleGastenNaarRestaurant();
                    }
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

    private void logEvent(String message) {
        eventLog.add("[" + System.currentTimeMillis() + "] " + message);
    }
    private void logError(String message) {
        errorLog.add("[" + System.currentTimeMillis() + "] " + message);
    }
    public List<String> getEventLog() {
        return new ArrayList<>(eventLog);
    }
    public List<String> getErrorLog() {
        return new ArrayList<>(errorLog);
    }
    public void clearEventLog() {
        eventLog.clear();
    }
    public void setSimulator(Simulator simulator) {
        this.simulator = simulator;
    }
}
