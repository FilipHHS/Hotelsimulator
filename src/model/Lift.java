package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lift implements TickListener {

    private double x, y;
    private int schachtMinY;
    private int schachtMaxY;

    private List<Persoon> passagiers;
    private Map<Persoon, Integer> passengerDestinations;
    
    private static final int MAX_CAPACITY = 3;
    private static final double LIFT_SPEED = 0.3;
    private static final int STATION_WAIT_TICKS = 15;
    
    private boolean fireAlarmActive = false;

    private enum LiftState { MOVING_UP, MOVING_DOWN, AT_STATION }
    private LiftState state = LiftState.MOVING_UP;

    private int currentFloor;
    private boolean movingUp = true;
    private int stationWaitCounter = 0;

    public Lift(double startX, double startY, int schachtMinY, int schachtMaxY) {
        this.x = startX;
        this.y = startY;
        this.schachtMinY = schachtMinY;
        this.schachtMaxY = schachtMaxY;
        this.passagiers = new ArrayList<>();
        this.passengerDestinations = new HashMap<>();
        this.currentFloor = (int)(startY - 0.5);
        this.movingUp = (startY < (schachtMinY + schachtMaxY) / 2.0 + 0.5);
    }

    public void roepNaar(int verdieping) {
        // Legacy method
    }

    public boolean voegGastToe(Persoon persoon, int doelVerdieping) {
        // Reject during fire alarm
        if (fireAlarmActive) {
            return false;
        }
        
        int persoonFloor = (persoon instanceof Gast) 
            ? (int)((Gast) persoon).getY() 
            : (int)((Schoonmaker) persoon).getY();
        
        if (Math.abs(persoonFloor - currentFloor) > 0 || state != LiftState.AT_STATION) {
            return false;
        }
        
        if (passagiers.size() >= MAX_CAPACITY) {
            return false;
        }

        passagiers.add(persoon);
        passengerDestinations.put(persoon, doelVerdieping);
        System.out.println("[Lift] " + persoon.getNaam() + " ingestapt op verdieping " + currentFloor);
        return true;
    }

    public void verwijderGast(Persoon persoon) {
        if (passagiers.remove(persoon)) {
            passengerDestinations.remove(persoon);
            System.out.println("[Lift] " + persoon.getNaam() + " uitgestapt op verdieping " + currentFloor);
        }
    }

    @Override
    public void onTick() {
        syncPassagierPosities();

        if (stationWaitCounter > 0) {
            state = LiftState.AT_STATION;
            stationWaitCounter--;
            
            if (stationWaitCounter == 0) {
                determineNextFloor();
            }
            return;
        }

        double targetY = currentFloor + 0.5;
        double distance = Math.abs(targetY - y);

        if (distance < 0.15) {
            y = targetY;
            state = LiftState.AT_STATION;
            stationWaitCounter = STATION_WAIT_TICKS;
            return;
        }

        if (targetY > y) {
            state = LiftState.MOVING_UP;
            y = Math.min(y + LIFT_SPEED, targetY);
        } else {
            state = LiftState.MOVING_DOWN;
            y = Math.max(y - LIFT_SPEED, targetY);
        }
    }

    private void determineNextFloor() {
        if (movingUp) {
            currentFloor++;
            if (currentFloor > schachtMaxY) {
                currentFloor = schachtMaxY;
                movingUp = false;
            }
        } else {
            currentFloor--;
            if (currentFloor < schachtMinY) {
                currentFloor = schachtMinY;
                movingUp = true;
            }
        }
    }

    private void syncPassagierPosities() {
        for (Persoon p : passagiers) {
            p.setLiftPosition(this.x, this.y);
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getCurrentFloor() { return currentFloor; }
    public boolean isIdle() { return state == LiftState.AT_STATION; }
    public List<Persoon> getPassagiers() { return new ArrayList<>(passagiers); }
    public int getDestination(Persoon p) { return passengerDestinations.getOrDefault(p, -1); }
    
    // Fire alarm methods
    public void activeerFireAlarm() {
        this.fireAlarmActive = true;
        System.out.println("[Lift] 🔥 FIRE ALARM: Lift disabled, all passengers being evacuated!");
        
        // Immediately eject all passengers to nearest floor
        for (Persoon p : new ArrayList<>(passagiers)) {
            verwijderGast(p);
        }
    }
    
    public void deactiveerFireAlarm() {
        this.fireAlarmActive = false;
        System.out.println("[Lift] ✓ Fire alarm cleared, lift returning to normal operation");
    }
    
    public boolean isFireAlarmActive() { return fireAlarmActive; }
}