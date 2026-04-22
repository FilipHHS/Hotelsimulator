package model;

import model.SimulationClock;
import model.HTEClock;
import model.TickListener;
import model.Persoon;
import ui.HotelPanel;

/**
 * Simulator - Stuurt de simulatie aan via HTE-ticks
 * Beheert lift en alle gasten
 */
public class Simulator {

    private boolean running;
    private HotelPanel hotelPanel;
    private Hotel hotel;
    private SimulationClock clock;
    private HTEClock hteClock;
    private Lift lift;             // De lift object

    private static final long TICK_INTERVAL = 100; // 100 ms

    public Simulator(Hotel hotel, HotelPanel hotelPanel) {
        this.hotel = hotel;
        this.hotelPanel = hotelPanel;
        this.running = false;
        this.clock = new SimulationClock(TICK_INTERVAL);
        this.hteClock = new HTEClock();
        
        // === INITIALISEER LIFT ===
        // Zoek de Schacht area (waar lift beweegt)
        Area schachtArea = null;
        for (Area area : hotel.getAreas()) {
            if (area.getAreaType().equals("Schacht")) {
                schachtArea = area;
                break;
            }
        }
        
        if (schachtArea != null) {
            // Maak lift aan in schacht
            // X = schacht X-coördinaat + 0.5 (midden van vakje)
            // Y start op eerste verdieping
            double liftX = (schachtArea.getX() - 1) + 0.5;
            double liftY = (schachtArea.getY() - 1) + 0.5;
            int minY = schachtArea.getY() - 1;
            int maxY = schachtArea.getY() + schachtArea.getHoogte() - 2;
            
            this.lift = new Lift(liftX, liftY, minY, maxY);
            System.out.println("[Simulator] Lift geïnitialiseerd op positie (" + liftX + ", " + liftY + ")");
        } else {
            System.out.println("[Simulator] WAARSCHUWING: Geen Schacht gevonden!");
        }
        
        // Registreer lift als listener
        if (lift != null) {
            hteClock.addListener(lift);
        }
        
        // === REGISTREER ALLE GASTEN EN KOP LIFT AAN ===
        for (Persoon p : hotel.getPersonen()) {
            // Registreer als listener op HTEClock
            if (p instanceof TickListener) {
                hteClock.addListener((TickListener) p);
            }
            
            // Als het een Gast is: geef hem een referentie naar de lift
            if (p instanceof Gast && lift != null) {
                ((Gast) p).setLift(lift);
                System.out.println("[Simulator] Gast '" + p.getNaam() + "' heeft lift-referentie gekregen");
            }
            
            // Als het een Schoonmaker is: geef hem ook lift-referentie
            if (p instanceof Schoonmaker && lift != null) {
                ((Schoonmaker) p).setLift(lift);
                System.out.println("[Simulator] Schoonmaker '" + p.getNaam() + "' heeft lift-referentie gekregen");
            }
        }
    }

    public void start() {
        running = true;
        clock.start();
        hotelPanel.setRunning(true);
        System.out.println("[Simulator] Simulatie gestart");
    }

    public void pause() {
        running = false;
        clock.stop();
        hotelPanel.setRunning(false);
        System.out.println("[Simulator] Simulatie gepauzeerd");
    }

    public boolean isRunning() {
        return running;
    }

    public SimulationClock getClock() {
        return clock;
    }

    public HTEClock getHteClock() {
        return hteClock;
    }
    
    public Lift getLift() {
        return lift;
    }

    public void resetClock() {
        clock.reset();
        hteClock = new HTEClock();
        
        if (lift != null) {
            hteClock.addListener(lift);
        }
        
        for (Persoon p : hotel.getPersonen()) {
            if (p instanceof TickListener) {
                hteClock.addListener((TickListener) p);
            }
        }
    }

     /**
      * HOOFDMETHODE: Wordt aangeroepen door Timer in main
      * Dit triggert HTE-ticks als timing klopt
      */
     public void tick() {

         // Check of er een echte HTE-tick plaatsvindt
         if (running && clock.tick()) {
             System.out.println("[Simulator] HTE-tick #" + clock.getTimestep() + " | Lift Y: " + (lift != null ? String.format("%.1f", lift.getY()) : "N/A"));
             
             // Roep HTEClock.tick() aan (die roept alle listeners.onTick() aan)
             // Dit includes: Lift + Gasten
             hteClock.tick();
         }

         // UI altijd updaten (ook bij pauze)
         hotelPanel.repaint();
     }
     
     /**
      * Stuur een gast naar een faciliteit.
      * Dit kan aangeroepen worden vanuit events of tests.
      */
     public void gastNaarFaciliteit(String gastNaam, String faciliteitsType) {
         for (Persoon p : hotel.getPersonen()) {
             if (p instanceof Gast && p.getNaam().equals(gastNaam)) {
                 ((Gast) p).gaatNaarFaciliteit(faciliteitsType);
                 System.out.println("[Simulator] Gast '" + gastNaam + "' gestuurd naar " + faciliteitsType);
                 return;
             }
         }
         System.out.println("[Simulator] Gast '" + gastNaam + "' niet gevonden!");
     }
 }


