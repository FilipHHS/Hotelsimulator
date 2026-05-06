package model;

import model.TickListener;
import ui.HotelPanel;
import java.util.List;

public class Simulator {
    private boolean running = false;
    private Hotel hotel;
    private HotelPanel hotelPanel;
    private SimulationClock clock;
    private HTEClock hteClock;
    private Lift lift;

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
                    // CORRECTIE: lobbyArea.getY() - 1 zorgt dat ze één vakje hoger spawnen
                    double startX = lobbyArea.getX() + 0.5;
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
                    // Als de schoonmakers ook te laag staan, doe hier ook - 1
                    double startX = opslagArea.getX() + 0.5;
                    double startY = (opslagArea.getY() - 1) + 0.5;
                    s.setStartPositie(startX, startY);
                }
            }
        }
    }

    public void tick() {
        if (running && clock.tick()) {
            hteClock.tick();
        }
        hotelPanel.repaint();
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