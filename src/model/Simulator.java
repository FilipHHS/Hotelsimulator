package model;

import model.TickListener;
import ui.HotelPanel;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Simulator {
    private boolean running = false;
    private Hotel hotel;
    private HotelPanel hotelPanel;
    private SimulationClock clock;
    private HTEClock hteClock;
    private Lift lift;
    private Area lobbyArea;
    private int lastGuestSpawnTime = 0;
    private Map<String, Integer> guestCheckInTime = new HashMap<>();  // Track check-in times

    public Simulator(Hotel hotel, HotelPanel hotelPanel) {
        this.hotel = hotel;
        this.hotelPanel = hotelPanel;
        this.clock = new SimulationClock(100);
        this.hteClock = new HTEClock();

        initialiseerLift();
        initialiseerPersonen();
    }

    private void initialiseerLift() {
        for (Area a : hotel.getAreas()) {
            String type = a.getAreaType();
            if (type != null && (type.equalsIgnoreCase("Schacht") ||
                    type.equalsIgnoreCase("Lift") ||
                    type.equalsIgnoreCase("Elevator"))) {

                double liftX = (a.getX() - 1) + 0.5;
                double liftY = (a.getY() + a.getHoogte() - 2) + 0.5;
                int minY = a.getY() - 1;
                int maxY = a.getY() + a.getHoogte() - 2;

                this.lift = new Lift(liftX, liftY, minY, maxY);
                hteClock.addListener(this.lift);

                System.out.println("[Simulator] Lift succesvol geïnitialiseerd.");
                return;
            }
        }
    }

    private void initialiseerPersonen() {
        Area lobbyArea = hotel.getAreas().stream()
                .filter(a -> a.getAreaType() != null && a.getAreaType().equalsIgnoreCase("Lobby"))
                .findFirst().orElse(null);

        Area opslagArea = hotel.getAreas().stream()
                .filter(a -> a.getAreaType() != null && (a.getAreaType().equalsIgnoreCase("Storage") || a.getAreaType().equalsIgnoreCase("Opslag")))
                .findFirst().orElse(null);

        // Store lobby reference for dynamic spawning
        this.lobbyArea = lobbyArea;

        for (Persoon p : hotel.getPersonen()) {
            if (p instanceof TickListener) {
                hteClock.addListener((TickListener) p);
            }

            // --- GAST INITIALISATIE ---
            if (p instanceof Gast) {
                Gast g = (Gast) p;
                g.setLift(lift);
                g.setHotel(hotel);
                g.setGridBounds(hotel.getBreedte(), hotel.getHoogte());

                if (lobbyArea != null) {
                    // Spawn at x = -1.0 outside the hotel
                    double startX = -1.0;
                    double startY = (lobbyArea.getY() - 1) + 0.5;
                    g.setStartPositie(startX, startY);
                }
            }

            // --- SCHOONMAKER INITIALISATIE ---
            if (p instanceof Schoonmaker) {
                Schoonmaker s = (Schoonmaker) p;
                s.setLift(lift);
                s.setHotel(hotel);
                s.setGridBounds(hotel.getBreedte(), hotel.getHoogte());

                if (opslagArea != null) {
                    // Keep cleaners in storage (1 grid linksboven van opslag: Position 7, 5)
                    double startX = opslagArea.getX() - 0.5;
                    double startY = opslagArea.getY() - 0.5;
                    s.setStartPositie(startX, startY);
                }
            }
        }
    }

    public void tick() {
        if (running && clock.tick()) {
            hteClock.tick();
            
            // Remove guests that have left the hotel (x < -1)
            hotel.getPersonen().removeIf(p -> p instanceof Gast && p.getX() < -1.0);
            
            // Auto-check in waiting guests
            autoCheckInGuests();
            
            // Auto-checkout guests after 300 ticks in room
            autoCheckoutGuests();
            
            // Periodically spawn new guests (every 100 ticks instead of 200)
            lastGuestSpawnTime++;
            if (lastGuestSpawnTime >= 100 && hasAvailableRoom()) {
                spawnNewGuest();
                lastGuestSpawnTime = 0;
            }
        }
        hotelPanel.repaint();
    }
    
    private void autoCheckInGuests() {
        try {
            for (Persoon p : new ArrayList<>(hotel.getPersonen())) {
                if (p instanceof Gast) {
                    Gast gast = (Gast) p;
                    // If guest is in lobby and not checked in yet
                    if (gast.getHuidigKamer() == null && (int)gast.getY() == 6 && gast.getX() > 1.0) {
                        // Try to check in to available room
                        Kamer k = hotel.zoekVrijeKamer("Luxe");
                        if (k == null) k = hotel.zoekVrijeKamer("Standaard");
                        if (k == null) k = hotel.zoekVrijeKamer("Budget");
                        
                        if (k != null) {
                            gast.checkinKamer(k);
                            guestCheckInTime.put(gast.getNaam(), 0);  // Start timer
                            System.out.println("[AUTO-CHECKIN] " + gast.getNaam() + " checked in to room " + k.getKamernummer());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[autoCheckInGuests] Error: " + e.getMessage());
        }
    }
    
    private void autoCheckoutGuests() {
        try {
            for (Persoon p : new ArrayList<>(hotel.getPersonen())) {
                if (p instanceof Gast) {
                    Gast gast = (Gast) p;
                    if (gast.getHuidigKamer() != null && guestCheckInTime.containsKey(gast.getNaam())) {
                        // Increment stay time
                        int stayTime = guestCheckInTime.get(gast.getNaam());
                        stayTime++;
                        guestCheckInTime.put(gast.getNaam(), stayTime);
                        
                        // Auto-checkout after 300 ticks
                        if (stayTime >= 300) {
                            gast.checkoutKamer();
                            guestCheckInTime.remove(gast.getNaam());
                            System.out.println("[AUTO-CHECKOUT] " + gast.getNaam() + " checked out");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[autoCheckoutGuests] Error: " + e.getMessage());
        }
    }
    
    private boolean hasAvailableRoom() {
        for (Kamer k : hotel.getKamers()) {
            if (k.getStatus() == Kamer.KamerStatus.VRIJ) return true;
        }
        return false;
    }
    
    private void spawnNewGuest() {
        if (lobbyArea == null) return;
        
        String[] firstNames = {"Emma", "Liam", "Olivia", "Noah", "Ava", "Elijah", "Sophia", "Mason"};
        String[] types = {"Luxe", "Standaard", "Budget"};
        
        String name = firstNames[(int)(Math.random() * firstNames.length)];
        String type = types[(int)(Math.random() * types.length)];
        
        Gast newGuest = new Gast(name, -1, 0);
        newGuest.setLift(lift);
        newGuest.setHotel(hotel);
        newGuest.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
        newGuest.setStartPositie(-1.0, (lobbyArea.getY() - 1) + 0.5);
        
        hteClock.addListener(newGuest);
        hotel.addPersoon(newGuest);
        
        System.out.println("[Simulator] Nieuwe gast '" + name + "' komt aan (kamertype: " + type + ")");
    }

    public void start() { this.running = true; clock.start(); }
    public void pause() { this.running = false; clock.stop(); }
    public boolean isRunning() { return running; }
    public SimulationClock getClock() { return clock; }
    public Lift getLift() { return lift; }
    public void resetClock() { this.clock.reset(); }

    public boolean gastCheckin(String naam, String type) {
        for (Persoon p : hotel.getPersonen()) {
            if (p instanceof Gast && p.getNaam().equals(naam)) {
                Kamer k = hotel.zoekVrijeKamer(type);
                if (k != null) return ((Gast) p).checkinKamer(k);
            }
        }
        return false;
    }

    public void gastCheckout(String naam) {
        for (Persoon p : hotel.getPersonen()) {
            if (p instanceof Gast && p.getNaam().equals(naam)) {
                ((Gast) p).checkoutKamer();
            }
        }
    }
}