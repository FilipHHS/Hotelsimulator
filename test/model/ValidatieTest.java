package model;

import org.junit.jupiter.api.Test;
import ui.HotelPanel;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit-test voor de validatie van onlogische DLL-events.
 * Elke test gebruikt het Arrange-Act-Assert patroon.
 */
public class ValidatieTest {

    @Test
    void processDataWordtGeweigerdAlsSimulatieGestoptIs() {
        // Arrange: maak een minimale simulator aan en zorg dat deze gestopt is.
        Hotel hotel = maakDummyHotel();
        HotelPanel panel = new HotelPanel(hotel);
        Simulator simulator = new Simulator(hotel, panel);
        EventBusImpl eventBus = simulator.getEventBus();
        simulator.pause();
        String beginStatus = eventBus.getSystemState();

        // Act: verwerk een ProcessData-event terwijl de simulator niet draait.
        eventBus.handleExternalDLLEvent("ProcessData");

        // Assert: het event wordt geweigerd, de status blijft gelijk en de waarschuwing staat in de log.
        assertFalse(simulator.isRunning(), "De simulator moet gestopt zijn voor deze test.");
        assertEquals(beginStatus, eventBus.getSystemState(), "Een ongeldig event mag de systeemstatus niet aanpassen.");
        assertTrue(
                eventBus.getEventLog().stream().anyMatch(log -> log.contains("ILLOGICAL_DLL_EVENT")),
                "De EventBus moet een waarschuwing loggen voor een onlogisch DLL-event."
        );
    }

    @Test
    void processDataWordtVerwerktAlsSimulatieDraait() {
        // Arrange: maak een minimale simulator aan en start deze.
        Hotel hotel = maakDummyHotel();
        HotelPanel panel = new HotelPanel(hotel);
        Simulator simulator = new Simulator(hotel, panel);
        EventBusImpl eventBus = simulator.getEventBus();
        simulator.start();

        // Act: verwerk hetzelfde ProcessData-event terwijl de simulator draait.
        eventBus.handleExternalDLLEvent("ProcessData");

        // Assert: het event is nu logisch en zet de status naar PROCESSING_DATA.
        assertTrue(simulator.isRunning(), "De simulator moet draaien voor deze test.");
        assertEquals("PROCESSING_DATA", eventBus.getSystemState(), "Een geldig ProcessData-event moet verwerkt worden.");
        assertFalse(
                eventBus.getEventLog().stream().anyMatch(log -> log.contains("ILLOGICAL_DLL_EVENT")),
                "Er mag geen waarschuwing gelogd worden als de simulatie draait."
        );
    }

    private Hotel maakDummyHotel() {
        int breedte = 10;
        int hoogte = 10;
        String[][] grid = new String[hoogte][breedte];
        List<Area> areas = new ArrayList<>();
        return new Hotel(grid, breedte, hoogte, areas);
    }
}
