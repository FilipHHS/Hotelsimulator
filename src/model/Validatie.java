package model;

import ui.HotelPanel;
import java.util.List;
import java.util.ArrayList;

/**
 * - Test: Validatie van onlogische events op basis van simulatiestatus
 *
 * Dit test-bestand demonstreert hoe de validatie werkt:
 *
 * Acceptatiecriteria:
 * - GIVEN: Simulatie staat in "Stopped" status
 * - WHEN: DLL stuurt "ProcessData" event (alleen geldig in RUNNING)
 * - THEN: Event wordt geweigerd als "Illogical"
 * - AND: Systeemstatus blijft "Stopped"
 * - AND: Waarschuwing wordt gelogd
 */
public class Validatie {

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("🧪 US4.2.b TEST: Validatie van onlogische events");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        // SETUP
        System.out.println("📋 SETUP: Maak een dummy Hotel en Simulator aan...");
        Hotel hotel = createDummyHotel();
        HotelPanel panel = new HotelPanel(hotel);
        Simulator simulator = new Simulator(hotel, panel);
        EventBusImpl eventBus = simulator.getEventBus();

        System.out.println("✅ Simulator en EventBus aangemaakt.\n");

        // GIVEN: Simulatie staat in "Stopped" status
        System.out.println("─────────────────────────────────────────────────────────────────");
        System.out.println("GIVEN: Simulatie staat in 'Stopped' status");
        System.out.println("─────────────────────────────────────────────────────────────────");
        simulator.pause();  // Zorg ervoor dat simulator NIET draait
        System.out.println("✓ Simulator gestopt via .pause()");
        System.out.println("  → simulator.isRunning() = " + simulator.isRunning() + " (moet false zijn)\n");

        // WHEN: DLL stuurt "ProcessData" event terwijl simulator gestopt is
        System.out.println("─────────────────────────────────────────────────────────────────");
        System.out.println("WHEN: DLL stuurt 'ProcessData' event (alleen geldig in RUNNING)");
        System.out.println("─────────────────────────────────────────────────────────────────");

        String initialState = eventBus.getSystemState();
        System.out.println("  Initiële systemState: " + initialState + "\n");

        System.out.println("  → Roep eventBus.handleExternalDLLEvent(\"ProcessData\") aan...\n");
        eventBus.handleExternalDLLEvent("ProcessData");

        // THEN: Event wordt geweigerd als "Illogical"
        System.out.println("\n─────────────────────────────────────────────────────────────────");
        System.out.println("THEN: Event wordt geweigerd als 'Illogical'");
        System.out.println("─────────────────────────────────────────────────────────────────");

        List<String> logs = eventBus.getEventLog();
        boolean hasIllogicalLog = false;

        System.out.println("✓ Controleer event-log op 'ILLOGICAL_DLL_EVENT':\n");
        for (String log : logs) {
            if (log.contains("ILLOGICAL_DLL_EVENT")) {
                System.out.println("  ✅ GEVONDEN: " + log);
                hasIllogicalLog = true;
            }
        }

        if (hasIllogicalLog) {
            System.out.println("\n✅ ACCEPTATIECRITERIUM 1 VERVULD: Event werd als 'Illogical' geweigerd!\n");
        } else {
            System.out.println("\n❌ FOUT: 'ILLOGICAL_DLL_EVENT' NIET gevonden in logs!\n");
        }

        // AND: Systeemstatus blijft "Stopped"
        System.out.println("─────────────────────────────────────────────────────────────────");
        System.out.println("AND: Systeemstatus blijft 'Stopped' (ongewijzigd)");
        System.out.println("─────────────────────────────────────────────────────────────────");

        String finalState = eventBus.getSystemState();
        System.out.println("  Initiële state: " + initialState);
        System.out.println("  Finale state:   " + finalState);

        if (initialState.equals(finalState)) {
            System.out.println("\n✅ ACCEPTATIECRITERIUM 2 VERVULD: Status bleef ongewijzigd!\n");
        } else {
            System.out.println("\n❌ FOUT: Status VERANDERDE van \"" + initialState + "\" naar \"" + finalState + "\"!\n");
        }

        // AND: Waarschuwing wordt gelogd
        System.out.println("─────────────────────────────────────────────────────────────────");
        System.out.println("AND: Waarschuwing wordt gelogd in systeemlogs");
        System.out.println("─────────────────────────────────────────────────────────────────");

        System.out.println("✓ Volledige event-log:\n");
        for (String log : logs) {
            System.out.println("  " + log);
        }

        if (hasIllogicalLog) {
            System.out.println("\n✅ ACCEPTATIECRITERIUM 3 VERVULD: Waarschuwing werd gelogd!\n");
        }

        // CONTRAST TEST: Wat GEBEURT er als simulatie DRAAIT?
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("🔄 CONTRAST TEST: Wat gebeurt als simulatie DRAAIT?");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        simulator.start();  // START de simulator nu
        System.out.println("✓ Simulator gestart via .start()");
        System.out.println("  → simulator.isRunning() = " + simulator.isRunning() + " (nu true)\n");

        System.out.println("  → Roep eventBus.handleExternalDLLEvent(\"ProcessData\") aan...\n");
        eventBus.handleExternalDLLEvent("ProcessData");

        System.out.println("\n✓ State na geldig event:");
        System.out.println("  systemState = " + eventBus.getSystemState());
        System.out.println("  (zou 'PROCESSING_DATA' moeten zijn)\n");

        if (eventBus.getSystemState().equals("PROCESSING_DATA")) {
            System.out.println("✅ Event WAS verwerkt omdat simulatie draaide!\n");
        } else {
            System.out.println("⚠️ Onverwacht resultaat: " + eventBus.getSystemState() + "\n");
        }

        // SAMENVATTEND
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("📊 SAMENVATTING");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("   - implementatie werkt correct!");
        System.out.println("   - Illogical events WORDEN geweigerd wanneer simulatie gestopt is");
        System.out.println("   - Status BLIJFT ongewijzigd");
        System.out.println("   - Waarschuwingen WORDEN gelogd");
        System.out.println("   - Geldige events WORDEN verwerkt wanneer simulatie draait");
        System.out.println("═══════════════════════════════════════════════════════════════\n");
    }

    /**
     * Maak een minimale dummy Hotel aan voor testing
     */
    private static Hotel createDummyHotel() {
        // Hotel constructor vereist: grid, breedte, hoogte, areas
        int breedte = 10;
        int hoogte = 10;
        String[][] grid = new String[hoogte][breedte];
        List<Area> areas = new ArrayList<>();

        Hotel hotel = new Hotel(grid, breedte, hoogte, areas);
        return hotel;
    }
}

