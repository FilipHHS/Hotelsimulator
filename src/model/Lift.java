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
    private int waitTicks = 0;
    private int maxWaitTicks = 0; // Begint op 0

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
        if (Math.abs(targetY - y) < 0.1) {
            this.waitTicks = 1;
        }
    }

    public boolean voegGastToe(Persoon persoon) {
        double persoonY = (persoon instanceof Gast) ? ((Gast) persoon).getY() : ((Schoonmaker) persoon).getY();

        if (Math.abs(persoonY - this.y) > 0.3) return false;
        if (passagiers.size() >= MAX_CAPACITY) return false;

        passagiers.add(persoon);
        this.waitTicks = 2;
        System.out.println("[Lift] " + persoon.getNaam() + " ingestapt.");
        return true;
    }

    public void verwijderGast(Persoon persoon) {
        if (passagiers.remove(persoon)) {
            this.waitTicks = 2;
            System.out.println("[Lift] " + persoon.getNaam() + " uitgestapt.");
            if (passagiers.isEmpty()) {
                this.state = LiftState.IDLE;
            }
        }
    }

    @Override
    public void onTick() {
        // 1. Harde timer voor IDLE state
        if (state == LiftState.IDLE) {
            maxWaitTicks++;
        } else {
            maxWaitTicks = 0;
        }

        // 2. Wachten met open deuren
        if (waitTicks > 0) {
            waitTicks--;
            return;
        }

        // 3. Doel bereikt check
        double distance = Math.abs(targetY - y);
        if (distance < 0.1) {
            y = targetY;

            if (state != LiftState.IDLE) {
                state = LiftState.IDLE;
                waitTicks = 10; // Geeft mensen tijd om in/uit te stappen
                return;
            }

            // 4. Automatisch verder gaan als we te lang wachten of leeg zijn
            if (maxWaitTicks > 30 || passagiers.isEmpty()) {
                this.targetY = (nextAutoFloor - 1) + 0.5;
                nextAutoFloor++;
                if (nextAutoFloor > (schachtMaxY + 1)) nextAutoFloor = 1;

                state = (targetY > y) ? LiftState.MOVING_UP : LiftState.MOVING_DOWN;
                maxWaitTicks = 0;
            }
            return;
        }

        // 5. Beweging
        if (targetY > y + 0.05) {
            state = LiftState.MOVING_UP;
            y += LIFT_SPEED;
        } else if (targetY < y - 0.05) {
            state = LiftState.MOVING_DOWN;
            y -= LIFT_SPEED;
        }

        y = Math.max(schachtMinY + 0.5, Math.min(y, schachtMaxY + 0.5));

        // 6. Passagiers bijwerken (DIT MOET BINNEN ONTICK)
        for (Persoon p : passagiers) {
            if (p instanceof Gast) {
                ((Gast) p).setLiftPosition(this.x, this.y);
            } else if (p instanceof Schoonmaker) {
                ((Schoonmaker) p).setLiftPosition(this.x, this.y);
            }
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isIdle() { return state == LiftState.IDLE; }
}