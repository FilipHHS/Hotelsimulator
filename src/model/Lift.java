package model;

import java.util.ArrayList;
import java.util.List;

public class Lift implements TickListener {

    private double x, y;
    private double targetY;
    private int schachtMinY;
    private int schachtMaxY;

    private List<Persoon> passagiers;
    private static final int MAX_CAPACITY = 3;
    private static final double LIFT_SPEED = 0.5;

    private enum LiftState { IDLE, MOVING_UP, MOVING_DOWN }
    private LiftState state = LiftState.IDLE;

    private int nextAutoFloor = 1;
    private int waitTicks = 0; // 🔥 NIEUW: Zorgt dat de deuren even open blijven

    public Lift(double startX, double startY, int schachtMinY, int schachtMaxY) {
        this.x = startX;
        this.y = startY;
        this.targetY = startY;
        this.schachtMinY = schachtMinY;
        this.schachtMaxY = schachtMaxY;
        this.passagiers = new ArrayList<>();
    }

    public void roepNaar(int verdieping) {
        this.targetY = (verdieping - 1) + 0.5;
        // Als de lift al daar is, reset de wacht-timer
        if (Math.abs(targetY - y) < 0.1) {
            this.waitTicks = 3;
        }
    }

    public boolean voegGastToe(Persoon persoon) {
        // Iets ruimere marge (0.3) voor betrouwbaarheid
        double persoonY = (persoon instanceof Gast) ? ((Gast) persoon).getY() : ((Schoonmaker) persoon).getY();

        if (Math.abs(persoonY - this.y) > 0.3) return false;
        if (passagiers.size() >= MAX_CAPACITY) return false;

        passagiers.add(persoon);
        this.waitTicks = 2; // Houd deuren langer open als er iemand instapt
        System.out.println("[Lift] " + persoon.getNaam() + " ingestapt. Passagiers: " + passagiers.size());
        return true;
    }

    public void verwijderGast(Persoon persoon) {
        if (passagiers.remove(persoon)) {
            this.waitTicks = 2; // Houd deuren open voor uitstappen
            if (passagiers.isEmpty()) {
                this.state = LiftState.IDLE;
            }
        }
    }

    @Override
    public void onTick() {
        // === FASE 1: WACHTEN (Deuren open) ===
        if (waitTicks > 0) {
            waitTicks--;
            return;
        }

        // === FASE 2: DOEL BEREIKT? ===
        if (Math.abs(targetY - y) < 0.1) {
            y = targetY;
            if (state != LiftState.IDLE) {
                state = LiftState.IDLE;
                waitTicks = 4; // Blijf 4 ticks staan voor in/uitstappers
                System.out.println("[Lift] Gestopt op verdieping " + (int)y + ", wacht op passagiers...");
                return;
            }

            // Automatische ronde als er niemand is
            if (passagiers.isEmpty()) {
                this.targetY = (nextAutoFloor - 1) + 0.5;
                nextAutoFloor++;
                if (nextAutoFloor > schachtMaxY + 1) nextAutoFloor = 1;
            }
        }

        // === FASE 3: BEWEGING ===
        if (targetY > y + 0.05) {
            state = LiftState.MOVING_UP;
            y += LIFT_SPEED;
        } else if (targetY < y - 0.05) {
            state = LiftState.MOVING_DOWN;
            y -= LIFT_SPEED;
        }

        y = Math.max(schachtMinY + 0.5, Math.min(y, schachtMaxY + 0.5));

        // Passagiers meenemen
        for (Persoon p : passagiers) {
            if (p instanceof Gast) ((Gast) p).setLiftPosition(this.x, this.y);
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isIdle() { return state == LiftState.IDLE; }

}