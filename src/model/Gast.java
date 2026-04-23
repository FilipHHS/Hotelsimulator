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
    }
    private State state = State.WANDELEN;

    // === FACILITEITEN ===
    private String huidigerFaciliteitType = null;
    private double faciliteitX, faciliteitY;
    private int faciliteitsBezoekDuur = 0;

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
                    System.out.println("[Gast] " + getNaam() + " stapt uit lift op verdieping " + liftFloor);
                }
            }
        }
    }

    /**
     * US3.7: Random walk die controleert of het grid-vakje vrij is.
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

        // --- US3.7 COLLISION CHECK ---
        if (isVakjeBezet((int)volgendeX, (int)y)) {
            // Vakje is bezet, we stoppen en proberen volgende tick opnieuw (of andere richting)
            stapsInRichting = maxStapsRichting;
            return;
        }

        x = volgendeX;
        x = Math.max(0.5, Math.min(x, maxX - 0.5));
        stapsInRichting++;

        if (RANDOM.nextDouble() < 0.03) {
            wiltVerdiepingWisselen();
        }
    }

    /**
     * Hulp-methode voor US3.7 om te checken of iemand anders op een vakje staat.
     */
    private boolean isVakjeBezet(int gx, int gy) {
        if (hotel == null) return false;
        Persoon ander = hotel.getPersoonOp(gx, gy);
        // Bezet als er iemand staat die ik niet zelf ben
        return (ander != null && ander != this);
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

        // US3.7 Collision check ook hier
        if (!isVakjeBezet((int)volgendeX, (int)y)) {
            x = volgendeX;
        }

        x = Math.max(0.5, Math.min(x, maxX - 0.5));
    }

    private void wachtOpVervoer() {
        if (usesTrap) {
            int currentFloor = (int) y;
            int newFloor = currentFloor + (RANDOM.nextBoolean() ? 1 : -1);
            newFloor = Math.max(0, Math.min(newFloor, maxY - 1));

            // Check of landingsplek trap vrij is
            if (!isVakjeBezet((int)x, newFloor)) {
                this.y = newFloor + 0.5;
                this.destX = x;
                this.state = State.WANDELEN;
                System.out.println("[Gast] " + getNaam() + " gaat trap naar verdieping " + newFloor);
            }
        } else {
            if (lift != null && Math.abs(lift.getY() - this.y) < 0.2 && lift.isIdle()) {
                int currentFloor = (int) y;
                int newFloor = currentFloor + (RANDOM.nextBoolean() ? 1 : -1);
                newFloor = Math.max(0, Math.min(newFloor, maxY - 1));

                if (lift.voegGastToe(this)) {
                    this.inLift = true;
                    this.destX = newFloor + 0.5;
                    this.state = State.IN_LIFT;
                    lift.roepNaar(newFloor);
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

    public boolean checkinKamer(Kamer kamer) {
        if (kamer == null || kamer.getStatus() != Kamer.KamerStatus.VRIJ) {
            return false;
        }
        this.huidigKamer = kamer;
        kamer.setStatus(Kamer.KamerStatus.BEZET);
        return true;
    }

    public void checkoutKamer() {
        if (huidigKamer != null) {
            huidigKamer.setStatus(Kamer.KamerStatus.SCHOONMAKEN);
            this.huidigKamer = null;
        }
    }

    public Kamer getHuidigKamer() {
        return huidigKamer;
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

        // US3.7 Collision check
        if (!isVakjeBezet((int)volgendeX, (int)volgendeY)) {
            x = volgendeX;
            y = volgendeY;
        }

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