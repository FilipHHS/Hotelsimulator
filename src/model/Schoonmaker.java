package model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Schoonmaker extends Persoon {
    private static final double SPEED = 0.15; // Iets rustiger voor vloeiende animatie
    private static final double LIFT_X = 1.5;
    private static final int SCHOONMAAK_DUUR = 100;

    private Hotel hotel;
    private Lift lift;
    private Kamer huidigKamer;
    private int schoonmaakTimer = 0;
    private boolean inLift = false;
    private int doelVerdieping;

    private enum State { VRIJ, NAAR_DOEL, SCHOONMAKEN, IN_LIFT }
    private State state = State.VRIJ;

    public Schoonmaker(String naam, int startX, int startY) {
        super(naam, "Schoonmaker");

        // FORCEER STARTPOSITIE NAAR OPSLAG (1 grid linksboven van Position 8,6 = Position 7,5)
        // We negeren startX en startY die vanuit de simulator komen.
        this.x = 7.5;
        this.y = 5.5;

        System.out.println("[Schoonmaker] " + naam + " is in de Opslag (7,5)");
    }

    @Override
    public void onTick() {
        if (inLift) {
            handleLiftMovement();
            return;
        }

        // 1. Zoek werk
        Kamer viezeKamer = zoekViezeKamer();

        // 2. Als er geen vuile kamer is: BLIJF IN OPSLAG!
        if (viezeKamer == null) {
            // If we were going to a room, cancel and go back to storage
            if (state == State.NAAR_DOEL) {
                state = State.VRIJ;
                huidigKamer = null;
            }
            
            // Return to storage and stay there (7.5, 5.5)
            if (Math.abs(x - 7.5) > 0.1) {
                x += (x < 7.5) ? SPEED : -SPEED;
            } else if (Math.abs(y - 5.5) > 0.1) {
                y += (y < 5.5) ? SPEED : -SPEED;
            } else {
                state = State.VRIJ;
                // IMPORTANT: Don't do anything else, just stay here!
            }
            return;  // Exit early - don't process further
        }

        // 3. There's work to do - go clean
        double targetX = viezeKamer.getArea().getX() + 0.5;
        double targetY = viezeKamer.getArea().getY() + 0.5;

        if (state == State.SCHOONMAKEN) {
            werkAanKamer();
        } else {
            beweeg(targetX, targetY, viezeKamer);
        }
    }

    private void beweeg(double tx, double ty, Kamer doelKamer) {
        // Check: Is the target room still dirty? If not, abort and go back to storage
        if (doelKamer.getStatus() != Kamer.KamerStatus.SCHOONMAKEN) {
            state = State.VRIJ;
            huidigKamer = null;
            return;  // Abort, go back to storage in next tick
        }
        
        // Verdieping check
        if ((int)y != (int)ty) {
            // Ga naar lift
            if (Math.abs(x - LIFT_X) > 0.1) {
                x += (x < LIFT_X) ? SPEED : -SPEED;
            } else {
                stapInLift((int)ty);
            }
        } else {
            // Op de juiste verdieping, loop naar X
            if (Math.abs(x - tx) > 0.1) {
                x += (x < tx) ? SPEED : -SPEED;
            } else {
                // Gearriveerd
                if (doelKamer != null && doelKamer.getStatus() == Kamer.KamerStatus.SCHOONMAKEN) {
                    this.huidigKamer = doelKamer;
                    this.state = State.SCHOONMAKEN;
                    this.schoonmaakTimer = SCHOONMAAK_DUUR;
                } else {
                    this.state = State.VRIJ;
                }
            }
        }
    }

    private void werkAanKamer() {
        schoonmaakTimer--;
        if (schoonmaakTimer <= 0) {
            huidigKamer.setStatus(Kamer.KamerStatus.VRIJ);
            System.out.println("[Schoonmaker] Kamer " + huidigKamer.getKamernummer() + " is klaar.");
            huidigKamer = null;
            state = State.VRIJ; // Dit triggert de loop naar de opslag in de volgende tick
        }
    }

    private void stapInLift(int doelVerdieping) {
        if (lift != null && Math.abs(lift.getY() - y) < 0.2 && lift.isIdle()) {
            if (lift.voegGastToe(this)) {
                this.inLift = true;
                this.doelVerdieping = doelVerdieping;
                this.state = State.IN_LIFT;
                lift.roepNaar(doelVerdieping);
            }
        }
    }

    private void handleLiftMovement() {
        this.x = lift.getX();
        this.y = lift.getY();

        // Exit lift when at the correct floor and lift is idle
        if (Math.abs(lift.getY() - (doelVerdieping + 0.5)) < 0.2 && lift.isIdle()) {
            // Double-check: Is there still a dirty room to clean?
            Kamer viezeKamer = zoekViezeKamer();
            if (viezeKamer == null) {
                // No more dirty rooms, abort mission and go back to storage
                lift.verwijderGast(this);
                this.inLift = false;
                this.y = doelVerdieping + 0.5;
                this.state = State.VRIJ;
                return;
            }
            
            lift.verwijderGast(this);
            this.inLift = false;
            this.y = doelVerdieping + 0.5;
            this.state = State.NAAR_DOEL;
        }
    }

    private Kamer zoekViezeKamer() {
        if (hotel == null) return null;
        for (Kamer k : hotel.getKamers()) {
            if (k.getStatus() == Kamer.KamerStatus.SCHOONMAKEN) return k;
        }
        return null;
    }

    // Setters voor initialisatie
    public void setHotel(Hotel h) { this.hotel = h; }
    public void setLift(Lift l) { this.lift = l; }
    public void setGridBounds(int mx, int my) { } // Niet meer nodig met vaste opslag

    // Getters voor tekenen
    public double getX() { return x; }
    public double getY() { return y; }
    public Color getKleur() { return Color.GRAY; }
}