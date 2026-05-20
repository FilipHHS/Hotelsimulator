package model;

/**
 * US4.1 - ExternalEventProvider
 * Deze klasse simuleert het laden van de externe "EventProvider.dll"
 * en stuurt de exacte event-sequentie door naar de EventBus.
 */
public class ExternalEventProvider {

    // Referentie naar de EventBus om de DLL-events naartoe te sturen
    private final EventBusImpl eventBus;

    // Status om bij te houden of de DLL succesvol geladen is (GIVEN)
    private boolean isLibraryLoaded = false;

    // Teller om het aantal ontvangen/verwerkte events bij te houden (THEN)
    private int eventCount = 0;

    /**
     * Constructor: Initialiseert de provider en koppelt de EventBus.
     * Direct bij de start proberen we de DLL te laden.
     */
    public ExternalEventProvider(EventBusImpl eventBus) {
        this.eventBus = eventBus;
        loadExternalLibrary(); // Start het laadproces van de DLL
    }

    /**
     * Simuleert het laden van de externe "EventProvider.dll"
     */
    private void loadExternalLibrary() {
        System.out.println("    ⚙️ [DLL] Bezig met laden van EventProvider.dll...");
        try {
            // Korte vertraging om het laden van een echt bestand na te bootsen
            Thread.sleep(500);
            this.isLibraryLoaded = true; // GIVEN: succesvol geladen
        } catch (InterruptedException e) {
            this.isLibraryLoaded = false;
        }
    }

    /**
     * Controleert of de DLL succesvol is geladen.
     * Wordt aangeroepen in Stap 3 van je testbestand.
     */
    public boolean isLoaded() {
        return this.isLibraryLoaded;
    }

    /**
     * WHEN: De DLL triggert de sequentie van events in de exacte volgorde.
     * Volgorde: Initialization -> DataReceived -> ProcessComplete
     */
    public void start() {
        if (!isLibraryLoaded) {
            System.out.println("    ❌ Kan sequentie niet starten: EventProvider.dll is niet geladen!");
            return;
        }

        // We starten een aparte Thread (achtergrondproces) zodat de simulator/test
        // niet vastloopt tijdens het wachten tussen de events door.
        new Thread(() -> {
            try {
                // --- EVENT 1: Initialization ---
                Thread.sleep(300);
                eventBus.handleExternalDLLEvent("Initialization");
                eventCount++;

                // --- EVENT 2: DataReceived ---
                Thread.sleep(500);
                eventBus.handleExternalDLLEvent("DataReceived");
                eventCount++;

                // --- EVENT 3: ProcessComplete ---
                Thread.sleep(500);
                eventBus.handleExternalDLLEvent("ProcessComplete");
                eventCount++;

            } catch (InterruptedException e) {
                System.out.println("    ❌ Event-sequentie onderbroken!");
            }
        }).start();
    }

    /**
     * Geeft het totaal aantal verwerkte events terug.
     * Wordt gebruikt in Stap 6 van je testbestand.
     */
    public int getEventCount() {
        return this.eventCount;
    }
}