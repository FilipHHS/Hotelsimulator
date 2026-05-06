package model;
import java.awt.Color;
import java.util.Random;

/**
 * GAST - Hotelgast die random wandelt (links-rechts) op dezelfde verdieping
 * US3.7: Houdt nu rekening met het positiegrid van het hotel om botsingen te voorkomen.
 */
public class Gast extends Persoon {
    private static final Random RANDOM = new Random();
    private static final double SPEED = 0.5;
    private static final double LIFT_WAIT_X = 1.5;
    private static final double TRAP_X = 8.5;

    // === POSITIE ===
    private double x, y;
    private double destX;
    private int maxX, maxY;

    // === HOTEL & LIFT ===
    private Hotel hotel;
    private Lift lift;
    private boolean inLift = false;
    private boolean usesTrap = false;
    private Kamer huidigKamer;

    // === VISUAAL ===
    private Color kleur;

    // === TOESTANDEN ===
    private enum State {
        WANDELEN,
        NAAR_LIFT_WACHTEN,
        WACHTEN_OP_VERVOER,
        IN_LIFT,
        GAAT_NAAR_FACILITEIT,
        IN_FACILITEIT,
        NAAR_LOBBY,         // Loopt naar lobby voor checkout
        IN_LOBBY,            // Is in lobby, checkt uit
        NAAR_UITGANG,        // Loopt naar uitgang
        VERTREKT             // Verlaat hotel
    }
    private State state = State.WANDELEN;

    // === FACILITEITEN ===
    private String huidigerFaciliteitType = null;
    private double faciliteitX, faciliteitY;
    private int faciliteitsBezoekDuur = 0;
    
    // === CHECKOUT SYSTEEM ===
    private static final double LOBBY_X = 7.5;      // Lobby X-positie
    private static final double LOBBY_Y = 1.5;      // Lobby Y-positie (begane grond)
    private static final double UITGANG_X = 10.0;   // Uitgang (buiten kaart)
    private static final double UITGANG_Y = 1.5;

    // === RANDOM WALK ===
    private int stapsInRichting = 0;
    private int maxStapsRichting = 5;

    public Gast(String naam, int startX, int startY) {
        super(naam, "Gast");
        this.x = startX + 0.5;
        this.y = startY + 0.5;
        this.destX = x;
        this.kleur = new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
    }

    @Override
    public void onTick() {
        if (!inLift) {
            switch (state) {
                case WANDELEN:
                    randomWalk();
                    break;
                case NAAR_LIFT_WACHTEN:
                    beweegNaarLiftTrap();
                    break;
                case WACHTEN_OP_VERVOER:
                    wachtOpVervoer();
                    break;
                case GAAT_NAAR_FACILITEIT:
                    beweegNaarFaciliteit();
                    break;
                case IN_FACILITEIT:
                    zitInFaciliteit();
                    break;
                // NIEUWE STATES - niet hoeven ingevoerd voor nu, gasten blijven in WANDELEN
            }
        } else {
            if (lift != null) {
                this.x = lift.getX();
                this.y = lift.getY();

                int liftFloor = (int) lift.getY();
                int targetFloor = (int) destX;

                if (liftFloor == targetFloor && lift.isIdle()) {
                    lift.verwijderGast(this);
                    this.inLift = false;
                    this.state = State.WANDELEN;
                }
            }
        }
    }

    /**
     * US3.7: Random walk - geen collision detection meer (entiteiten via elkaar)
     */
    private void randomWalk() {
        if (stapsInRichting >= maxStapsRichting) {
            int keuze = RANDOM.nextInt(10);
            if (keuze < 4) {
                destX = Math.max(2.0, x - 3);
            } else if (keuze < 8) {
                destX = Math.min(maxX - 3.0, x + 3);
            } else {
                destX = x;
            }
            stapsInRichting = 0;
            maxStapsRichting = RANDOM.nextInt(3) + 2;
        }

        double dx = destX - x;
        double volgendeX = x;

        if (Math.abs(dx) < SPEED) {
            volgendeX = destX;
        } else if (dx > 0) {
            volgendeX = x + SPEED;
        } else {
            volgendeX = x - SPEED;
        }

        // === BOUNDARY CHECK EERST (VOORKOMEN OUT OF BOUNDS) ===
        if (volgendeX < 1.0 || volgendeX > maxX - 1.0) {
            stapsInRichting = maxStapsRichting;  // Forceer nieuwe richting
            return;
        }

        // COLLISION DETECTION VERWIJDERD - Entiteiten kunnen door elkaar heen gaan

        x = volgendeX;
        stapsInRichting++;

        if (RANDOM.nextDouble() < 0.03) {
            wiltVerdiepingWisselen();
        }
    }

    private void wiltVerdiepingWisselen() {
        this.usesTrap = RANDOM.nextBoolean();
        this.destX = usesTrap ? TRAP_X : LIFT_WAIT_X;
        this.state = State.NAAR_LIFT_WACHTEN;
    }

    private void beweegNaarLiftTrap() {
        double dx = destX - x;
        double volgendeX = x;

        if (Math.abs(dx) < SPEED * 2) {
            x = destX;
            this.state = State.WACHTEN_OP_VERVOER;
            return;
        }

        volgendeX = (dx > 0) ? x + SPEED : x - SPEED;

        // COLLISION DETECTION VERWIJDERD - Entiteiten kunnen door elkaar heen
        x = volgendeX;

        x = Math.max(0.5, Math.min(x, maxX - 0.5));
    }

    private void wachtOpVervoer() {
        if (usesTrap) {
            int currentFloor = (int) y;
            int newFloor = currentFloor + (RANDOM.nextBoolean() ? 1 : -1);
            newFloor = Math.max(0, Math.min(newFloor, maxY - 1));

            // COLLISION DETECTION VERWIJDERD - Gasten kunnen via trap gaan
            this.y = newFloor + 0.5;
            this.destX = x;
            this.state = State.WANDELEN;
            System.out.println("[Gast] " + getNaam() + " gaat trap naar verdieping " + newFloor);
        } else {
            // === LIFT LOGIC ===
            if (lift != null) {
                System.out.printf("  [LIFT DEBUG] %s: LiftY=%.1f, GastY=%.1f, Diff=%.1f, LiftIdle=%s%n", 
                    getNaam(), lift.getY(), this.y, Math.abs(lift.getY() - this.y), lift.isIdle());
                
                if (Math.abs(lift.getY() - this.y) < 0.2 && lift.isIdle()) {
                    int currentFloor = (int) y;
                    int newFloor = currentFloor + (RANDOM.nextBoolean() ? 1 : -1);
                    newFloor = Math.max(0, Math.min(newFloor, maxY - 1));

                    System.out.println("  [LIFT DEBUG] " + getNaam() + " probeert in te stappen naar verdieping " + newFloor);
                    
                    if (lift.voegGastToe(this)) {
                        this.inLift = true;
                        this.destX = newFloor + 0.5;
                        this.state = State.IN_LIFT;
                        lift.roepNaar(newFloor);
                        System.out.println("  ✅ [LIFT] " + getNaam() + " IN LIFT!");
                    } else {
                        System.out.println("  ❌ [LIFT] " + getNaam() + " kon niet instappen!");
                    }
                } else {
                    System.out.printf("  [LIFT] %s wacht: Positie %s, Lift %s%n",
                        getNaam(),
                        (Math.abs(lift.getY() - this.y) < 0.2) ? "OK" : "MISMATCH",
                        lift.isIdle() ? "IDLE" : "MOVING");
                }
            }
        }
    }

    public void setGridBounds(int maxX, int maxY) {
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public void setLift(Lift lift) {
        this.lift = lift;
    }

    public void setLiftPosition(double liftX, double liftY) {
        if (inLift) {
            this.x = liftX;
            this.y = liftY;
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public Color getKleur() { return kleur; }
    public boolean isInLift() { return inLift; }

    /**
     * US3.1 Scenario 1: Succesvol inchecken van een gast bij aankomst
     * - Gast wordt gekoppeld aan een vrije kamer van het gevraagde type
     * - Kamerstatus verandert naar "BEZET"
     * - Gast verplaatst zich naar de kamer (visualisatie)
     */
    public boolean checkinKamer(Kamer kamer) {
        // Controleer of kamer beschikbaar is
        if (kamer == null || kamer.getStatus() != Kamer.KamerStatus.VRIJ) {
            System.out.println("❌ [US3.1] " + getNaam() + " kon niet inchecken - kamer niet vrij!");
            return false;
        }
        
        // Koppel gast aan kamer
        this.huidigKamer = kamer;
        kamer.setStatus(Kamer.KamerStatus.BEZET);
        
        // Verplaats gast naar kamer positie (visualisatie)
        if (kamer.getArea() != null) {
            this.x = kamer.getArea().getX() + kamer.getArea().getBreedte() / 2.0;
            this.y = kamer.getArea().getY() + 0.5;
        }
        
        System.out.println("✅ [US3.1 Scenario 1] " + getNaam() + " is ingecheckt in kamer " + kamer.getKamernummer());
        return true;
    }

    /**
     * US3.1 Scenario 2: Gast verlaat het hotel en laat de kamer vies achter
     * - Status van kamer verandert naar "SCHOONMAKEN" (vies)
     * - Gast wordt verwijderd uit de simulatie (via Simulator.gastCheckout())
     */
    public void checkoutKamer() {
        if (huidigKamer != null) {
            // Markeer kamer als vies
            huidigKamer.setStatus(Kamer.KamerStatus.SCHOONMAKEN);
            System.out.println("[US3.1 Scenario 2] " + getNaam() + " heeft kamer " + 
                              huidigKamer.getKamernummer() + " verlaten. Kamer is nu VIES.");
            this.huidigKamer = null;
        } else {
            System.out.println("[US3.1] " + getNaam() + " probeerde uit te checken maar zat niet in een kamer.");
        }
    }

    /**
     * US3.1 Helper: Geef terug in welke kamer de gast verblijft
     */
    public Kamer getHuidigKamer() {
        return huidigKamer;
    }

    /**
     * US3.1 Helper: Check of gast is ingecheckt
     */
    public boolean isIngecheckt() {
        return huidigKamer != null;
    }

    public void gaatNaarFaciliteit(String faciliteitsType) {
        if (hotel == null) return;

        Area faciliteitsArea = null;
        for (Area area : hotel.getAreas()) {
            if (faciliteitsType.equalsIgnoreCase(area.getAreaType())) {
                faciliteitsArea = area;
                break;
            }
        }

        if (faciliteitsArea == null) return;

        int faciliteitsY = faciliteitsArea.getY();
        int gastY = (int) this.y;

        if (faciliteitsY != gastY) return;

        this.faciliteitX = faciliteitsArea.getX() + faciliteitsArea.getBreedte() / 2.0;
        this.faciliteitY = faciliteitsArea.getY() + 0.5;
        this.huidigerFaciliteitType = faciliteitsType;
        this.state = State.GAAT_NAAR_FACILITEIT;
        this.destX = this.faciliteitX;
    }

    private void beweegNaarFaciliteit() {
        double dx = faciliteitX - x;
        double dy = faciliteitY - y;

        if (Math.abs(dx) < SPEED * 2 && Math.abs(dy) < SPEED * 2) {
            x = faciliteitX;
            y = faciliteitY;
            this.state = State.IN_FACILITEIT;
            this.faciliteitsBezoekDuur = 10 + RANDOM.nextInt(20);
            return;
        }

        double volgendeX = x;
        double volgendeY = y;

        if (dx > 0) volgendeX += SPEED;
        else if (dx < 0) volgendeX -= SPEED;

        if (dy > 0) volgendeY += SPEED;
        else if (dy < 0) volgendeY -= SPEED;

        // COLLISION DETECTION VERWIJDERD - Entiteiten kunnen door elkaar heen
        x = volgendeX;
        y = volgendeY;

        x = Math.max(0.5, Math.min(x, maxX - 0.5));
        y = Math.max(0.5, Math.min(y, maxY - 0.5));
    }

    private void zitInFaciliteit() {
        faciliteitsBezoekDuur--;
        if (faciliteitsBezoekDuur <= 0) {
            this.huidigerFaciliteitType = null;
            this.state = State.WANDELEN;
            this.destX = x;
        }
    }

    public String getHuidigerFaciliteitType() {
        return huidigerFaciliteitType;
    }

    public boolean isInFaciliteit() {
        return state == State.IN_FACILITEIT;
    }
}
