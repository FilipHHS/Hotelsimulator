package model;

import hotelevents.HotelEvent;
import hotelevents.HotelEventListener;

/**
 * US4.1 - Geautomatiseerde Acceptatietest
 * Dit bestand test of de EventProvider.dll correct laadt, de events verwerkt
 * en of de status aan het einde "Sequence Processed" wordt.
 */
public class ExternalEventTest implements HotelEventListener {

    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🧪 US4.1: Externe DLL Events - Acceptatietest");
        System.out.println("=".repeat(60));

        // [1] Stap 1: Maak EventBus aan
        System.out.println("\n[1] EventBus aanmaken...");
        EventBusImpl eventBus = new EventBusImpl();
        System.out.println("    ✅ Done");

        // [1b] Extra: Registreer de test als luisteraar op de EventBus
        System.out.println("    [1b] TestListener registreren op EventBus...");
        ExternalEventTest testInstance = new ExternalEventTest();
        eventBus.registerHotelEventListener(testInstance);

        // [2] Stap 2: Maak ExternalEventProvider (GIVEN: EventProvider.dll wordt geladen)
        System.out.println("\n[2] ExternalEventProvider aanmaken...");
        ExternalEventProvider provider = new ExternalEventProvider(eventBus);
        System.out.println("    ✅ Done");

        // [3] Stap 3: Check of de library succesvol geladen is
        System.out.println("\n[3] Check of library geladen is...");
        if (provider.isLoaded()) {
            System.out.println("    ✅ GIVEN: EventProvider.dll is succesvol geladen.");
        } else {
            System.out.println("    ❌ Library niet geladen");
            return;
        }

        // [4] Stap 4: Start event sequentie (WHEN: DLL triggert Initialization, DataReceived, ProcessComplete)
        System.out.println("\n[4] Start event sequentie vanuit de DLL...");
        provider.start();

        // [5] Stap 5: Wacht op events (2 seconden) zodat de achtergrond-thread de tijd heeft
        System.out.println("\n[5] Wachten op verwerking van de events...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println("Onderbroken: " + e.getMessage());
        }

        // [6] Stap 6: Check resultaat (THEN: Is de status veranderd naar "Sequence Processed"?)
        System.out.println("\n[6] Check resultaat...");
        int count = provider.getEventCount();

        // VRAAG DE ECHTE STATUS OP UIT DE EVENTBUS (Cruciaal voor acceptatiecriterium!)
        String eindStatus = eventBus.getSystemState();

        System.out.println("    Totaal events afgevuurd door DLL: " + count + "/3");
        System.out.println("    Uiteindelijke systeemstatus: \"" + eindStatus + "\"");

        System.out.println("\n" + "=".repeat(60));

        // De uiteindelijke geautomatiseerde check op basis van jouw acceptatiecriteria
        if (count >= 3 && "Sequence Processed".equals(eindStatus)) {
            System.out.println("✅✅✅ TEST GESLAAGD ✅✅✅");
            System.out.println("Status: De User Story voldoet aan ALLE acceptatiecriteria!");
        } else {
            System.out.println("❌ TEST GEFAALD: Systeemstatus is niet correct veranderd naar 'Sequence Processed'.");
        }
        System.out.println("=".repeat(60) + "\n");
    }

    @Override
    public void notify(HotelEvent event) {
        // Deze methode wordt aangeroepen als er standaard HotelEvents binnenkomen
        System.out.println("    🔔 [Test Log] Standaard HotelEvent opgevangen! Type: " + event.getEventType());
    }
}