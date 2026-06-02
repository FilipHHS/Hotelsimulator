package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit-test voor de gesimuleerde externe EventProvider.dll.
 * Elke test gebruikt het Arrange-Act-Assert patroon.
 */
public class ExternalEventTest {

    @Test
    void externalEventProviderLaadtDeDllSuccesvol() {
        // Arrange: maak een EventBus aan die de externe events ontvangt.
        EventBusImpl eventBus = new EventBusImpl();

        // Act: maak de provider aan; de constructor simuleert het laden van de DLL.
        ExternalEventProvider provider = new ExternalEventProvider(eventBus);

        // Assert: de provider meldt dat de externe library geladen is.
        assertTrue(provider.isLoaded(), "De gesimuleerde EventProvider.dll moet succesvol geladen zijn.");
    }

    @Test
    void externalEventProviderVerwerktDeVolledigeEventSequentie() throws InterruptedException {
        // Arrange: maak EventBus en provider klaar voor de event-sequentie.
        EventBusImpl eventBus = new EventBusImpl();
        ExternalEventProvider provider = new ExternalEventProvider(eventBus);

        // Act: start de sequentie Initialization -> DataReceived -> ProcessComplete.
        provider.start();
        wachtTotSequentieKlaarIs(provider);

        // Assert: alle drie events zijn verwerkt en de eindstatus voldoet aan de acceptatie-eis.
        assertEquals(3, provider.getEventCount(), "De provider moet exact drie DLL-events afvuren.");
        assertEquals("Sequence Processed", eventBus.getSystemState(), "Na ProcessComplete moet de sequentie afgerond zijn.");
        assertTrue(
                eventBus.getEventLog().stream().anyMatch(log -> log.contains("DLL_EVENT: Initialization")),
                "Initialization moet in de event-log staan."
        );
        assertTrue(
                eventBus.getEventLog().stream().anyMatch(log -> log.contains("DLL_EVENT: DataReceived")),
                "DataReceived moet in de event-log staan."
        );
        assertTrue(
                eventBus.getEventLog().stream().anyMatch(log -> log.contains("DLL_EVENT: ProcessComplete")),
                "ProcessComplete moet in de event-log staan."
        );
    }

    private void wachtTotSequentieKlaarIs(ExternalEventProvider provider) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 2500;
        while (provider.getEventCount() < 3 && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
    }
}
