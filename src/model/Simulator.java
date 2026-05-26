package model;

import ui.HotelPanel;
import hotelevents.HotelEventType;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
     * Simulator: beheert de simulatie-loop, klok, lift en personen.
     * Kort: initialiseert wereldobjecten, verwerkt ticks en regelt
     * automatische check-ins/outs en brandalarm-gedrag.
     */
    public class Simulator {
    // Control flags and timers
    private boolean running = false;               // Of de simulatie loopt
    private int lastGuestSpawnTime = 0;            // Teller voor automatisch spawnen van gasten
    private static final int CHECKOUT_NA_TICKS = 600;

    // Referenties naar model en UI
    private Hotel hotel;                           // Hotel-model met kamers, areas en personen
    private HotelPanel hotelPanel;                 // UI-paneel om te verversen
    private SimulationClock clock;                 // Regelt het aantal ticks (tempo)
    private HTEClock hteClock;                     // High-level tick event dispatcher
    private EventBusImpl eventBus;                  // Event system (HotelEventType)

    // Wereldobjecten en status
    private Lift lift;                             // Lift-object als aanwezig in layout
    private Area lobbyArea;                        // Referentie naar de Lobby area (startpositie gasten)
    // Houdt bij hoeveel ticks een gast al in z'n kamer zit (naam -> ticks)
    private Map<String, Integer> guestCheckInTime = new HashMap<>(); // Houdt bij hoe lang een gast er al is

    public Simulator(Hotel hotel, HotelPanel hotelPanel) {
        this.hotel = hotel;
        this.hotelPanel = hotelPanel;
        this.clock = new SimulationClock(100);
        this.hteClock = new HTEClock();
        this.eventBus = new EventBusImpl();  // Initialize event system
        this.eventBus.setSimulator(this);   // US4.2.b: Koppel simulator aan EventBus voor validatie

        initialiseerLift();
        initialiseerPersonen();
    }

    // --- INITIALISATIE METHODEN ---

    // Zoek in de layout naar een area die 'Lift' of 'Schacht' heet en maak een Lift-object
    private void initialiseerLift() {
        for (Area area : hotel.getAreas()) {
            String type = area.getAreaType();

            // Controleer of deze area de lift/schacht is
            if (type != null && (type.equalsIgnoreCase("Schacht") ||
                    type.equalsIgnoreCase("Lift") ||
                    type.equalsIgnoreCase("Elevator"))) {

                double liftX = (area.getX() - 1) + 0.5;
                double liftY = (area.getY() + area.getHoogte() - 2) + 0.5;
                int minY = area.getY() - 1;
                int maxY = area.getY() + area.getHoogte() - 2;

                this.lift = new Lift(liftX, liftY, minY, maxY);
                this.lift.setEventBus(eventBus);  // Set event bus
                hteClock.addListener(this.lift);

                System.out.println("[Simulator] Lift succesvol geïnitialiseerd.");
                return; // Lift is gevonden en gemaakt, stop de loop
            }
        }
    }

    // Zet startposities en koppelingen (lift, hotel, tick-listener) voor alle personen
    private void initialiseerPersonen() {
        Area opslagArea = null;

        // Zoek eerst handmatig de Lobby en de Opslag (simpele for-loop)
        for (Area area : hotel.getAreas()) {
            if (area.getAreaType() != null) {
                if (area.getAreaType().equalsIgnoreCase("Lobby")) {
                    this.lobbyArea = area;
                }
                if (area.getAreaType().equalsIgnoreCase("Storage") || area.getAreaType().equalsIgnoreCase("Opslag")) {
                    opslagArea = area;
                }
            }
        }

        // Koppel de personen aan de startlocaties en de lift
        for (Persoon persoon : hotel.getPersonen()) {
            if (persoon instanceof TickListener) {
                hteClock.addListener(persoon);
            }

            // GAST INITIALISATIE
            if (persoon instanceof Gast gast) {
                gast.setLift(lift);
                gast.setHotel(hotel);
                gast.setEventBus(eventBus);  // Set event bus
                gast.setGridBounds(hotel.getBreedte(), hotel.getHoogte());

                if (lobbyArea != null) {
                    double startX = -1.0; // Start net buiten het hotel
                    double startY = (lobbyArea.getY() - 1) + 0.5;
                    gast.setStartPositie(startX, startY);
                }
            }

            // SCHOONMAKER INITIALISATIE
            if (persoon instanceof Schoonmaker schoonmaker) {
                schoonmaker.setLift(lift);
                schoonmaker.setHotel(hotel);
                schoonmaker.setEventBus(eventBus);  // Set event bus
                schoonmaker.setGridBounds(hotel.getBreedte(), hotel.getHoogte());

                if (opslagArea != null) {
                    double startX = opslagArea.getX() - 0.5;
                    double startY = opslagArea.getY() - 0.5;
                    schoonmaker.setStartPositie(startX, startY);
                }
            }
        }
    }

    // --- SIMULATIE TICK (LOOP) ---

    // Hoofd loop: wordt iedere tick van de UI timer aangeroepen
    // Verwerkt klok, verwijdert out-of-bounds personen en regelt automatische checkin/checkout
    public void tick() {
        if (!running || !clock.tick()) {
            hotelPanel.repaint();
            return;
        }

        hteClock.tick();

        // Verwijder gasten/schoonmakers die ver buiten beeld zijn gelopen
        hotel.getPersonen().removeIf(p -> (p instanceof Gast || p instanceof Schoonmaker) && p.getX() < -2.0);

        autoCheckInGuests();
        autoCheckoutGuests();

        // Voeg periodiek een nieuwe gast toe, maar nooit tijdens brandalarm/evacuatie.
        if (!isEvacuatieActief()) {
            lastGuestSpawnTime++;
            if (lastGuestSpawnTime >= 100 && hasAvailableRoom()) {
                spawnNewGuest();
                lastGuestSpawnTime = 0;
            }
        } else {
            lastGuestSpawnTime = 0;
        }

        // Random GODZILLA event (per 0,1% per tick)
        if (Math.random() < 0.001 && eventBus != null) {
            java.util.List<Persoon> gasten = hotel.getPersonen().stream()
                .filter(p -> p instanceof Gast)
                .toList();
            if (!gasten.isEmpty()) {
                Persoon randomGast = gasten.get((int)(Math.random() * gasten.size()));
                if (randomGast instanceof Gast gast) {
                    gast.activeerGodzilla();
                }
                eventBus.triggerHotelEvent(HotelEventType.GODZILLA,
                    randomGast.getNaam().hashCode(), 0);
            }
        }

        hotelPanel.repaint();
    }

    // --- GASTEN LOGICA (CHECKIN / CHECKOUT) ---

    // Controleert gasten in de lobby en checkt automatisch in wanneer een vrije kamer gevonden is
    private void autoCheckInGuests() {
        // Geen check-ins tijdens een brandalarm!
        if (lift != null && lift.isFireAlarmActive()) {
            return;
        }

        try {
            // Kopie van lijst om errors tijdens het loopen te voorkomen
            List<Persoon> personenKopie = new ArrayList<>(hotel.getPersonen());

            for (Persoon persoon : personenKopie) {
                if (persoon instanceof Gast gast) {
                    // Als de gast in de lobby staat en nog geen kamer heeft
                    if (gast.getHuidigKamer() == null && isInArea(gast, lobbyArea) && gast.getX() > 1.0) {

                        // Zoek een kamer (volgorde van exclusief naar budget)
                        Kamer kamer = hotel.zoekVrijeKamer("PentHouse");
                        if (kamer == null) kamer = hotel.zoekVrijeKamer("Luxe");
                        if (kamer == null) kamer = hotel.zoekVrijeKamer("Standaard");
                        if (kamer == null) kamer = hotel.zoekVrijeKamer("Budget");

                        if (kamer != null) {
                            gast.checkinKamer(kamer);
                            guestCheckInTime.put(gast.getNaam(), 0); // Start de timer op 0
                            System.out.println("[AUTO-CHECKIN] " + gast.getNaam() + " ingecheckt in kamer " + kamer.getKamernummer());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[autoCheckInGuests] Fout: " + e.getMessage());
        }
    }

    private boolean isInArea(Persoon persoon, Area area) {
        if (area == null) return false;

        double minX = area.getX() - 1;
        double maxX = minX + area.getBreedte();
        double minY = area.getY() - 1;
        double maxY = minY + area.getHoogte();

        return persoon.getX() >= minX
                && persoon.getX() < maxX
                && persoon.getY() >= minY
                && persoon.getY() < maxY;
    }

    // Houdt bij hoe lang gasten in hun kamer zitten en checkt automatisch uit na een tijd
    private void autoCheckoutGuests() {
        // Geen checkouts tijdens evacuatie!
        if (lift != null && lift.isFireAlarmActive()) {
            return;
        }

        try {
            List<Persoon> personenKopie = new ArrayList<>(hotel.getPersonen());

            for (Persoon persoon : personenKopie) {
                if (persoon instanceof Gast gast) {
                    if (gast.getHuidigKamer() != null && guestCheckInTime.containsKey(gast.getNaam())) {

                        // Verhoog de tijd dat de gast in de kamer zit
                        int stayTime = guestCheckInTime.get(gast.getNaam()) + 1;
                        guestCheckInTime.put(gast.getNaam(), stayTime);

                        // Na een tijdje gaat de gast automatisch uitchecken
                        if (stayTime >= CHECKOUT_NA_TICKS) {
                            gast.checkoutKamer();
                            guestCheckInTime.remove(gast.getNaam());
                            System.out.println("[AUTO-CHECKOUT] " + gast.getNaam() + " heeft uitgecheckt.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[autoCheckoutGuests] Fout: " + e.getMessage());
        }
    }

    private boolean hasAvailableRoom() {
        for (Kamer kamer : hotel.getKamers()) {
            if (kamer.getStatus() == Kamer.KamerStatus.VRIJ) {
                return true;
            }
        }
        return false;
    }

    private boolean isEvacuatieActief() {
        if (lift != null && lift.isFireAlarmActive()) {
            return true;
        }

        for (Persoon persoon : hotel.getPersonen()) {
            if (persoon.isFireAlarmActive() || persoon.isEvacuatieBegonnen()) {
                return true;
            }
        }
        return false;
    }

    // Maakt periodiek een nieuwe gast met random naam en type en voegt die aan het hotel toe
    private void spawnNewGuest() {
        if (lobbyArea == null) return;

        String[] firstNames = {"Emma", "Liam", "Olivia", "Noah", "Ava", "Elijah", "Sophia", "Mason"};
        String[] types = {"Luxe", "Standaard", "Budget"};

        String randomName = firstNames[(int)(Math.random() * firstNames.length)];
        String randomType = types[(int)(Math.random() * types.length)];

        Gast newGuest = new Gast(randomName, -1, 0);
        newGuest.setLift(lift);
        newGuest.setHotel(hotel);
        newGuest.setEventBus(eventBus);
        newGuest.setGridBounds(hotel.getBreedte(), hotel.getHoogte());

        double startX = -1.0;
        double startY = (lobbyArea.getY() - 1) + 0.5;
        newGuest.setStartPositie(startX, startY);

        hteClock.addListener(newGuest);
        hotel.addPersoon(newGuest);

        System.out.println("[Simulator] Nieuwe gast '" + randomName + "' komt aan (kamertype: " + randomType + ")");
    }

    // --- MANUELE CHECKIN / CHECKOUTS ---

    public boolean gastCheckin(String naam, String type) {
        for (Persoon persoon : hotel.getPersonen()) {
            if (persoon instanceof Gast gast && persoon.getNaam().equals(naam)) {
                Kamer kamer = hotel.zoekVrijeKamer(type);
                if (kamer != null) {
                    return gast.checkinKamer(kamer);
                }
            }
        }
        return false;
    }

    public void gastCheckout(String naam) {
        for (Persoon persoon : hotel.getPersonen()) {
            if (persoon instanceof Gast gast && persoon.getNaam().equals(naam)) {
                gast.checkoutKamer();
            }
        }
    }

    // --- BRANDALARM (US4.3) ---

    public void triggerFireAlarm() {
        System.out.println("\n============================================================");
        System.out.println("🚨 🚨 🚨  BRANDALARM GEACTIVEERD - EVACUATIE BEGONNEN  🚨 🚨 🚨");
        System.out.println("============================================================\n");

        if (lift != null) {
            lift.activeerFireAlarm();
        }

        for (Persoon persoon : hotel.getPersonen()) {
            persoon.activeerFireAlarm();
        }
    }

    public void clearFireAlarm() {
        System.out.println("\n============================================================");
        System.out.println("✓ ✓ ✓  BRANDALARM REPROGMANSEERD - EVACUATIE AFGEROND  ✓ ✓ ✓");
        System.out.println("============================================================\n");

        if (lift != null) {
            lift.deactiveerFireAlarm();
        }

        for (Persoon persoon : hotel.getPersonen()) {
            persoon.deactiveerFireAlarm();
        }
    }

    // --- GETTERS & SETTERS (ALGEMENE BESTURING) ---

    public void start() { this.running = true; clock.start(); }
    public void pause() { this.running = false; clock.stop(); }
    public boolean isRunning() { return running; }
    public SimulationClock getClock() { return clock; }
    public Lift getLift() { return lift; }
    public EventBusImpl getEventBus() { return eventBus; }  // Get event bus for UI/debug
    public void resetClock() { this.clock.reset(); }
}
