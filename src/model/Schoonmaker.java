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

    private enum State { VRIJ, NAAR_DOEL, SCHOONMAKEN, IN_LIFT }
    private State state = State.VRIJ;

    public Schoonmaker(String naam, int startX, int startY) {
        super(naam, "Schoonmaker");

        // FORCEER STARTPOSITIE NAAR OPSLAG (Vakje 8, 6 uit je JSON)
        // We negeren startX en startY die vanuit de simulator komen.
        this.x = 8.5;
        this.y = 6.5;

        System.out.println("[Schoonmaker] " + naam + " is in de Opslag (8,6)");
    }

    @Override
    public void onTick() {
        if (inLift) {
            handleLiftMovement();
            return;
        }

        // 1. Zoek werk
        Kamer viezeKamer = zoekViezeKamer();

        // 2. Bepaal doelpositie (Kamer of Opslag 8.5, 6.5)
        double targetX = (viezeKamer != null) ? viezeKamer.getArea().getX() + 0.5 : 8.5;
        double targetY = (viezeKamer != null) ? viezeKamer.getArea().getY() + 0.5 : 6.5;

        // 3. Voer actie uit op basis van lokatie
        if (state == State.SCHOONMAKEN) {
            werkAanKamer();
        } else {
            beweeg(targetX, targetY, viezeKamer);
        }
    }

    private void beweeg(double tx, double ty, Kamer doelKamer) {
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
                if (doelKamer != null) {
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
                this.state = State.IN_LIFT;
                lift.roepNaar(doelVerdieping);
            }
        }
    }

    private void handleLiftMovement() {
        this.x = lift.getX();
        this.y = lift.getY();

        // Ben ik op de verdieping die ik nodig heb?
        Kamer vKamer = zoekViezeKamer();
        int ty = (vKamer != null) ? (int)vKamer.getArea().getY() : 6;

        if ((int)y == ty && lift.isIdle()) {
            lift.verwijderGast(this);
            this.inLift = false;
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