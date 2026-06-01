package model;

public class FireAlarmService {

    private final Hotel hotel;
    private final Lift lift;

    public FireAlarmService(Hotel hotel, Lift lift) {
        this.hotel = hotel;
        this.lift = lift;
    }

    public void trigger() {
        System.out.println("\n============================================================");
        System.out.println("🚨 🚨 🚨  BRANDALARM GEACTIVEERD - EVACUATIE BEGONNEN  🚨 🚨 🚨");
        System.out.println("============================================================\n");

        if (lift != null) lift.activeerFireAlarm();
        for (Persoon persoon : hotel.getPersonen()) {
            persoon.activeerFireAlarm();
        }
    }

    public void clear() {
        System.out.println("\n============================================================");
        System.out.println("✓ ✓ ✓  BRANDALARM GERESET - EVACUATIE AFGEROND  ✓ ✓ ✓");
        System.out.println("============================================================\n");

        if (lift != null) lift.deactiveerFireAlarm(); // polymorfisme
        for (Persoon persoon : hotel.getPersonen()) {
            persoon.deactiveerFireAlarm();
        }
    }
}