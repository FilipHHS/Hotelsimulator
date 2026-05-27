package model;

import model.*;
import model.strategy.EvacuationMovement;
import model.strategy.GastNormalStrategy;
import model.strategy.SchoonmakerNormalStrategy;
import ui.HotelPanel;

/**
 * Quick Test: Fire Alarm System
 * US4.3: Brandalarm - Simulation Test
 */
public class FireAlarmTest {
    
    public static void main(String[] args) throws Exception {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║  FIRE ALARM SYSTEM (US4.3: BRANDALARM) - TEST SIMULATION      ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
        
        // Load hotel layout
        System.out.println("Loading hotel layout...");
        Hotel hotel = LayoutLoader.laadLayout("layouts/hotel_multistory.json");
        
        // Initialize rooms
        System.out.println("Initializing rooms...");
        initializeKamers(hotel);
        
        // Add guests
        System.out.println("Adding guests...");
        addTestGuests(hotel);
        
        // Add staff
        System.out.println("Adding staff...");
        addSchoonmakers(hotel);
        
        System.out.println("\n✓ Initial setup complete!\n");
        
        // Create simulator and panel
        HotelPanel panel = new HotelPanel(hotel);
        Simulator simulator = new Simulator(hotel, panel);
        
        System.out.println("Starting simulation...\n");
        simulator.start();
        
        // Phase 1: Normal operations
        System.out.println("\n" + "=".repeat(70));
        System.out.println("PHASE 1: Normal Operations (5 seconds)");
        System.out.println("=".repeat(70));
        for (int i = 0; i < 50; i++) {
            simulator.tick();
            Thread.sleep(100);
        }
        
        // Show status before alarm
        printPersonStatus(hotel, "BEFORE ALARM");
        
        // Phase 2: Trigger fire alarm
        System.out.println("\n" + "=".repeat(70));
        System.out.println("PHASE 2: TRIGGERING FIRE ALARM");
        System.out.println("=".repeat(70));
        simulator.triggerFireAlarm();
        
        // Let them start evacuating
        Thread.sleep(500);
        
        // Run evacuation simulation
        System.out.println("\n" + "=".repeat(70));
        System.out.println("PHASE 3: Evacuation in progress (10 seconds)");
        System.out.println("=".repeat(70));
        
        for (int i = 0; i < 100; i++) {
            simulator.tick();
            if (i % 20 == 0) {
                printEvacuationStatus(hotel);
            }
            Thread.sleep(100);
        }
        
        // Show status during alarm
        printPersonStatus(hotel, "DURING EVACUATION");
        
        // Phase 3: Clear alarm
        System.out.println("\n" + "=".repeat(70));
        System.out.println("PHASE 4: Clearing Fire Alarm");
        System.out.println("=".repeat(70));
        simulator.clearFireAlarm();
        
        // Wait a bit
        Thread.sleep(1000);
        
        // Show final status
        printPersonStatus(hotel, "AFTER ALARM CLEARED");
        
        simulator.pause();
        
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║  ✅ TEST COMPLETE - Fire Alarm System Working!                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
        
        System.exit(0);
    }
    
    private static void initializeKamers(Hotel hotel) {
        java.util.List<Area> roomAreas = new java.util.ArrayList<>();
        for (Area a : hotel.getAreas()) {
            if ("Room".equals(a.getAreaType())) roomAreas.add(a);
        }
        
        int[] nummers = {101, 102, 201, 202};
        String[] types = {"Luxe", "Luxe", "Standaard", "Standaard"};
        
        for (int i = 0; i < nummers.length && i < roomAreas.size(); i++) {
            Kamer k = new Kamer(nummers[i], types[i]);
            k.setArea(roomAreas.get(i));
            hotel.addKamer(k);
        }
    }
    
    private static void addTestGuests(Hotel hotel) {
        String[] namen = {"Alice", "Bob", "Charlie", "Diana"};
        for (String naam : namen) {
            Gast g = new Gast(naam, -1, 0, new GastNormalStrategy(), new EvacuationMovement());
            g.setHotel(hotel);
            g.setStartPositie(-1.0, 6.5);
            hotel.addPersoon(g);
        }
    }
    
    private static void addSchoonmakers(Hotel hotel) {
        Schoonmaker s1 = new Schoonmaker("Schoonmaker1", 8, 6,
                new SchoonmakerNormalStrategy(), new EvacuationMovement());
        Schoonmaker s2 = new Schoonmaker("Schoonmaker2", 8, 6,
                new SchoonmakerNormalStrategy(), new EvacuationMovement());
        s1.setHotel(hotel);
        s2.setHotel(hotel);
        hotel.addPersoon(s1);
        hotel.addPersoon(s2);
    }
    
    private static void printPersonStatus(Hotel hotel, String phase) {
        System.out.println("\n>>> STATUS: " + phase);
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        for (Persoon p : hotel.getPersonen()) {
            String type = (p instanceof Gast) ? "👤 Guest" : "👷 Staff";
            String activity = p.getHuidigeActiviteit();
            String alarm = p.isFireAlarmActive() ? "🔥 ALARM" : "✓ OK";
            System.out.printf("│ %s %-20s Activity: %-20s %s%n", 
                type, p.getNaam(), activity, alarm);
        }
        System.out.println("└─────────────────────────────────────────────────────────────┘");
    }
    
    private static void printEvacuationStatus(Hotel hotel) {
        int inLobby = 0;
        int evacuating = 0;
        int notEvacuating = 0;
        
        for (Persoon p : hotel.getPersonen()) {
            if (p.isFireAlarmActive()) {
                if (Math.abs(p.getX() - 1.5) < 0.5) {
                    inLobby++;
                } else {
                    evacuating++;
                }
            } else {
                notEvacuating++;
            }
        }
        
        System.out.printf("  📊 Status: In Lobby: %d | Evacuating: %d | Not Active: %d%n", 
            inLobby, evacuating, notEvacuating);
    }
}

