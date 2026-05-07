package model;

import java.awt.Color;
import java.util.Random;

public class Gast extends Persoon {
    private static final Random RANDOM = new Random();
    private static final double SPEED = 0.5;
    private static final double LIFT_WAIT_X = 1.5;
    private static final double TRAP_X = 8.5;

    private int maxX, maxY;
    private Hotel hotel;
    private Lift lift;
    private boolean inLift = false;
    private boolean usesTrap = false;
    private Kamer huidigKamer;
    private Color kleur;

    private int doelVerdieping;
    private int roomStayTimer = 0;  // Timer for how long guest stays in room

    private double faciliteitX, faciliteitY;
    private int faciliteitsBezoekDuur = 0; // Let op de 'l'
    private String huidigerFaciliteitType = null;

    private enum State {
        WANDELEN,
        NAAR_LIFT_WACHTEN,
        WACHTEN_OP_VERVOER,
        IN_LIFT,
        GAAT_NAAR_FACILITEIT,
        IN_FACILITEIT,
        GAAT_NAAR_LOBBY,
        GAAT_NAAR_KAMER,
        VERLAAT_HOTEL
    }

    private State state = State.WANDELEN;
    private int stapsInRichting = 0;
    private int maxStapsRichting = 5;

    public Gast(String naam, int ignoreX, int ignoreY) {
        super(naam, "Gast");
        this.kleur = new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
    }


    @Override
    public void onTick() {
        if (!inLift) {
            switch (state) {
                case WANDELEN: randomWalk(); break;
                case NAAR_LIFT_WACHTEN: beweegNaarLiftTrap(); break;
                case WACHTEN_OP_VERVOER: wachtOpVervoer(); break;
                case GAAT_NAAR_FACILITEIT: beweegNaarFaciliteit(); break;
                case IN_FACILITEIT: zitInFaciliteit(); break;
                case GAAT_NAAR_KAMER: beweegNaarKamer(); break;
                case GAAT_NAAR_LOBBY: beweegNaarLobby(); break;
                case VERLAAT_HOTEL: beweegNaarExit(); break;
            }
        } else {
            handleLiftLogic();
        }
    }

    private void handleLiftLogic() {
        if (lift != null) {
            this.x = lift.getX();
            this.y = lift.getY();

            if (Math.abs(lift.getY() - (doelVerdieping + 0.5)) < 0.2 && lift.isIdle()) {
                lift.verwijderGast(this);
                this.inLift = false;
                this.y = doelVerdieping + 0.5;
                this.destX = this.x;
                this.state = State.WANDELEN;
            }
        }
    }

    private void randomWalk() {
        // If guest just arrived in room, don't move - stay there
        if (roomStayTimer > 0) {
            roomStayTimer--;
            destX = x;  // Stay in place
            if (roomStayTimer == 0) {
                // Timer done - maybe visit a facility
                if (RANDOM.nextDouble() < 0.5) {
                    // 50% chance to visit restaurant or fitness
                    String facility = RANDOM.nextBoolean() ? "Restaurant" : "Fitness";
                    // For now, just reset and start wandering
                    // TODO: Add facility visit logic here
                }
            }
            return;
        }

        if (stapsInRichting >= maxStapsRichting) {
            int keuze = RANDOM.nextInt(10);
            // FIX: Guests should NOT wander out of bounds (x < 0). Only stay inside hotel.
            if (keuze < 4) destX = Math.max(0.5, x - 3);
            else if (keuze < 8) destX = Math.min(maxX - 1.5, x + 3);
            else destX = x;
            stapsInRichting = 0;
            maxStapsRichting = RANDOM.nextInt(3) + 2;
        }

        double dx = destX - x;
        if (Math.abs(dx) < SPEED) x = destX;
        else x += (dx > 0 ? SPEED : -SPEED);

        stapsInRichting++;
        if (RANDOM.nextDouble() < 0.02) wiltVerdiepingWisselen();
    }

    private void beweegNaarLiftTrap() {
        double dx = destX - x;
        if (Math.abs(dx) < SPEED) {
            x = destX;
            this.state = State.WACHTEN_OP_VERVOER;
        } else {
            x += (dx > 0) ? SPEED : -SPEED;
        }
    }

    private void wachtOpVervoer() {
        if (usesTrap) {
            int direction = (y >= maxY - 1) ? -1 : (y <= 1 ? 1 : (RANDOM.nextBoolean() ? 1 : -1));
            int newFloor = (int)y + direction;
            this.y = newFloor + 0.5;
            this.destX = this.x;
            this.state = State.WANDELEN;
        } else if (lift != null) {
            if (Math.abs(lift.getY() - this.y) < 0.5 && lift.isIdle()) {
                if (lift.voegGastToe(this)) {
                    this.inLift = true;
                    this.doelVerdieping = RANDOM.nextInt(maxY);
                    this.state = State.IN_LIFT;
                    lift.roepNaar(doelVerdieping + 1);
                }
            }
        }
    }

    private void wiltVerdiepingWisselen() {
        this.usesTrap = RANDOM.nextBoolean();
        this.destX = usesTrap ? TRAP_X : LIFT_WAIT_X;
        this.state = State.NAAR_LIFT_WACHTEN;
    }

    // === FACILITEIT LOGICA (Gebruikt nu de juiste variabelenaam) ===

    public void gaatNaarFaciliteit(String type, double fX, double fY) {
        this.huidigerFaciliteitType = type;
        this.faciliteitX = fX;
        this.faciliteitY = fY;
        if ((int)fY != (int)y) wiltVerdiepingWisselen();
        else this.state = State.GAAT_NAAR_FACILITEIT;
    }

    private void beweegNaarFaciliteit() {
        double dx = faciliteitX - x;
        if (Math.abs(dx) < SPEED) {
            x = faciliteitX;
            this.state = State.IN_FACILITEIT;
            this.faciliteitsBezoekDuur = 20 + RANDOM.nextInt(30); // Gefixt
        } else {
            x += (dx > 0) ? SPEED : -SPEED;
        }
    }

    private void beweegNaarKamer() {
        // Check if we need to change floors
        if ((int)y != doelVerdieping) {
            wiltVerdiepingWisselen();
        } else {
            // On the correct floor, move to room X
            double dx = destX - x;
            if (Math.abs(dx) < SPEED) {
                x = destX;
                this.y = doelVerdieping + 0.5;
                this.state = State.WANDELEN;
                // Guest arrived in room - set timer to stay there
                this.roomStayTimer = 100 + RANDOM.nextInt(200);  // Stay 100-300 ticks
            } else {
                x += (dx > 0) ? SPEED : -SPEED;
            }
        }
    }

    private void zitInFaciliteit() {
        faciliteitsBezoekDuur--; // Gefixt
        if (faciliteitsBezoekDuur <= 0) {
            this.state = State.WANDELEN;
            this.huidigerFaciliteitType = null;
        }
    }

    private void beweegNaarLobby() {
        double lobbyX = 1.5;
        int lobbyY = maxY - 1;
        if ((int)y != lobbyY) wiltVerdiepingWisselen();
        else {
            double dx = lobbyX - x;
            if (Math.abs(dx) < SPEED) {
                x = lobbyX;
                this.state = State.VERLAAT_HOTEL;
            } else {
                x += (dx > 0) ? SPEED : -SPEED;
            }
        }
    }

    private void beweegNaarExit() {
        x -= SPEED;
        if (x < -1) System.out.println("[Gast] " + getNaam() + " is weg.");
    }

    // === SETTERS & GETTERS ===
    public void setGridBounds(int maxX, int maxY) { this.maxX = maxX; this.maxY = maxY; }
    public void setLift(Lift lift) { this.lift = lift; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }
    public Color getKleur() { return kleur; }
    public Kamer getHuidigKamer() { return huidigKamer; }

    public boolean checkinKamer(Kamer k) {
        this.huidigKamer = k;
        k.setStatus(Kamer.KamerStatus.BEZET);
        // Don't teleport! Set destination and change state to walk there
        this.destX = k.getArea().getX() + 0.5;
        this.state = State.GAAT_NAAR_KAMER;
        this.doelVerdieping = (int)k.getArea().getY();
        return true;
    }

    public void checkoutKamer() {
        if (huidigKamer != null) {
            huidigKamer.setStatus(Kamer.KamerStatus.SCHOONMAKEN);
            huidigKamer = null;
            this.state = State.GAAT_NAAR_LOBBY;
        }
    }
    /**
     * Wordt aangeroepen door de lift om de positie van de gast te synchroniseren
     * terwijl deze in de lift staat.
     */


}