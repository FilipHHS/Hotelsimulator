/**
 * Fire Alarm System Test Examples
 * US4.3: Brandalarm Implementation
 * 
 * This file shows practical examples of how to use the Fire Alarm system
 * in your Hotel Simulator.
 */

package model;

public class FireAlarmExample {
    
    /**
     * Example 1: Basic Fire Alarm Trigger
     * 
     * This demonstrates how to trigger a fire alarm and observe evacuation
     */
    public static void example1_BasicFireAlarm(Simulator simulator) {
        System.out.println("\n=== EXAMPLE 1: Basic Fire Alarm ===\n");
        
        // Start the simulation
        simulator.start();
        
        // Let simulation run for a bit
        try { Thread.sleep(5000); } catch (Exception e) {}
        
        // Trigger fire alarm
        System.out.println("\n>>> Triggering fire alarm...");
        simulator.triggerFireAlarm();
        
        // Observe evacuation for 10 seconds
        try { Thread.sleep(10000); } catch (Exception e) {}
        
        // Clear alarm
        System.out.println("\n>>> Clearing fire alarm...");
        simulator.clearFireAlarm();
        
        // Observe recovery for 5 seconds
        try { Thread.sleep(5000); } catch (Exception e) {}
        
        simulator.pause();
    }
    
    /**
     * Example 2: Fire Alarm with Multiple People
     * 
     * Shows fire alarm behavior with various guests in different activities
     */
    public static void example2_WithMultiplePeople(Simulator simulator) {
        System.out.println("\n=== EXAMPLE 2: Fire Alarm with Multiple People ===\n");
        
        simulator.start();
        
        // Let simulation run for a bit to populate with guests
        try { Thread.sleep(15000); } catch (Exception e) {}
        
        // Trigger fire alarm
        System.out.println("\n>>> Fire alarm triggered!");
        simulator.triggerFireAlarm();
        
        // All guests will immediately evacuate
        try { Thread.sleep(15000); } catch (Exception e) {}
        
        simulator.pause();
    }
    
    /**
     * Example 3: Observing Elevator Behavior During Fire Alarm
     * 
     * Demonstrates how the elevator is disabled and passengers ejected
     */
    public static void example3_ElevatorBehavior(Simulator simulator) {
        System.out.println("\n=== EXAMPLE 3: Elevator Behavior During Fire Alarm ===\n");
        
        simulator.start();
        
        // Let simulation run so people use elevators
        try { Thread.sleep(10000); } catch (Exception e) {}
        
        // Check if anyone is in the lift
        Lift lift = simulator.getLift();
        if (lift != null && lift.getPassagiers().size() > 0) {
            System.out.println(">>> " + lift.getPassagiers().size() + " people in lift");
            System.out.println(">>> Triggering fire alarm - they will be ejected!");
            
            simulator.triggerFireAlarm();
            
            // Immediately after alarm
            try { Thread.sleep(100); } catch (Exception e) {}
            
            System.out.println(">>> People in lift after alarm: " + lift.getPassagiers().size());
            // Should be 0 - all ejected!
        }
        
        try { Thread.sleep(10000); } catch (Exception e) {}
        
        simulator.pause();
    }
    
    /**
     * Example 4: Checking Evacuation Status
     * 
     * Shows how to verify that people are in evacuation state
     */
    public static void example4_EvacuationStatus(Simulator simulator, Hotel hotel) {
        System.out.println("\n=== EXAMPLE 4: Evacuation Status Monitoring ===\n");
        
        simulator.start();
        
        try { Thread.sleep(5000); } catch (Exception e) {}
        
        simulator.triggerFireAlarm();
        
        // Check evacuation status after alarm
        try { Thread.sleep(2000); } catch (Exception e) {}
        
        int evacuating = 0;
        int inLobby = 0;
        
        for (Persoon p : hotel.getPersonen()) {
            if (p.isFireAlarmActive()) {
                if (p.isEvacuatieBegonnen()) {
                    // Check if they're in lobby (X position near 1.5)
                    if (Math.abs(p.getX() - 1.5) < 0.5) {
                        inLobby++;
                    } else {
                        evacuating++;
                    }
                }
            }
        }
        
        System.out.println("\n>>> Evacuation Status:");
        System.out.println("    Currently evacuating: " + evacuating);
        System.out.println("    Reached lobby: " + inLobby);
        
        try { Thread.sleep(10000); } catch (Exception e) {}
        
        simulator.clearFireAlarm();
        simulator.pause();
    }
    
    /**
     * Example 5: Multiple Fire Alarms (Stress Test)
     * 
     * Tests system behavior with multiple alarm triggers and clears
     */
    public static void example5_MultipleAlarms(Simulator simulator) {
        System.out.println("\n=== EXAMPLE 5: Multiple Fire Alarms ===\n");
        
        simulator.start();
        
        for (int alarmCount = 1; alarmCount <= 3; alarmCount++) {
            try { Thread.sleep(5000); } catch (Exception e) {}
            
            System.out.println("\n>>> ALARM #" + alarmCount);
            simulator.triggerFireAlarm();
            
            try { Thread.sleep(8000); } catch (Exception e) {}
            
            System.out.println(">>> Clearing alarm #" + alarmCount);
            simulator.clearFireAlarm();
        }
        
        try { Thread.sleep(5000); } catch (Exception e) {}
        
        simulator.pause();
    }
    
    /**
     * Example 6: Integration Test - Full Simulation Cycle
     * 
     * Complete test including guest activity, then fire alarm, 
     * then recovery to normal operations
     */
    public static void example6_FullCycle(Simulator simulator) {
        System.out.println("\n=== EXAMPLE 6: Full Simulation Cycle ===\n");
        
        simulator.start();
        
        // Phase 1: Normal operations
        System.out.println("\n[Phase 1] Normal Operations (15 seconds)");
        try { Thread.sleep(15000); } catch (Exception e) {}
        
        // Phase 2: Fire Alarm
        System.out.println("\n[Phase 2] Emergency Evacuation (20 seconds)");
        simulator.triggerFireAlarm();
        try { Thread.sleep(20000); } catch (Exception e) {}
        
        // Phase 3: All Clear
        System.out.println("\n[Phase 3] All Clear - Resuming Normal Operations (10 seconds)");
        simulator.clearFireAlarm();
        try { Thread.sleep(10000); } catch (Exception e) {}
        
        simulator.pause();
        System.out.println("\n[COMPLETE] Test cycle finished\n");
    }
    
    /**
     * Verification Helper: Check if all people are in lobby after evacuation
     */
    public static boolean verifyAllInLobby(Hotel hotel) {
        for (Persoon p : hotel.getPersonen()) {
            if (Math.abs(p.getX() - 1.5) > 0.5) {
                System.out.println("NOT in lobby: " + p.getNaam() + " at X=" + p.getX());
                return false;
            }
        }
        System.out.println("✓ Verification: All people in lobby!");
        return true;
    }
    
    /**
     * Verification Helper: Check if lift is disabled
     */
    public static boolean verifyLiftDisabled(Lift lift) {
        if (lift.isFireAlarmActive()) {
            System.out.println("✓ Verification: Lift properly disabled!");
            return true;
        }
        System.out.println("✗ Verification: Lift should be disabled!");
        return false;
    }
    
    /**
     * Verification Helper: Check if all people have evacuation flag set
     */
    public static boolean verifyAllEvacuating(Hotel hotel) {
        for (Persoon p : hotel.getPersonen()) {
            if (!p.isFireAlarmActive()) {
                System.out.println("NOT evacuating: " + p.getNaam());
                return false;
            }
        }
        System.out.println("✓ Verification: All people have evacuation flag!");
        return true;
    }
}



