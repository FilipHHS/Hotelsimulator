package model;

import java.util.ArrayList;
import java.util.List;

/**
 * LIFT - Beweegt omhoog/omlaag in de schacht
 * Personen kunnen erin stappen en naar andere verdiepingen gaan.
 * De lift brengt passagiers naar hun doel-verdieping.
 */
public class Lift implements TickListener {

    // === POSITIE ===
    private double x, y;           // Huidige coördinaten (X blijft constant, Y verandert)
    private double targetY;        // Doelverdieping waar lift heen gaat
    private int schachtMinY;       // Onderste verdieping van schacht
    private int schachtMaxY;       // Bovenste verdieping van schacht
    
    // === PASSAGIERS ===
    private List<Persoon> passagiers;  // Personen in de lift (zowel Gast als Schoonmaker)
    private static final int MAX_CAPACITY = 3; // Max personen
    
    // === BEWEGING ===
    private static final double LIFT_SPEED = 0.5; // Hoe snel lift beweegt
    
    // === STAAT ===
    private enum LiftState {
        IDLE,         // Stil wachten
        MOVING_UP,    // Naar boven
        MOVING_DOWN   // Naar beneden
    }
    private LiftState state = LiftState.IDLE;
    
    // === AUTOMATISCHE RONDE ===
    private int nextAutoFloor = 1;  // Volgende verdieping die lift automatisch gaat bezoeken

    /**
     * Constructor - initialiseer lift
     * startX: X-positie in de schacht (meestal 0.5)
     * startY: Beginhoogte
     * schachtMinY/MaxY: Grenzen van schacht
     */
    public Lift(double startX, double startY, int schachtMinY, int schachtMaxY) {
        this.x = startX;
        this.y = startY;
        this.targetY = startY;
        this.schachtMinY = schachtMinY;
        this.schachtMaxY = schachtMaxY;
        this.passagiers = new ArrayList<>();
        System.out.println("[Lift] Geïnitialiseerd op positie (" + x + ", " + y + ")");
    }

    /**
     * Roep lift op naar een bepaalde verdieping
     */
    public void roepNaar(int verdieping) {
        // Converteer verdieping (1-7) naar Y-coördinaat
        this.targetY = (verdieping - 1) + 0.5;
        System.out.println("[Lift] Geroepen naar verdieping " + verdieping);
    }

    /**
     * Persoon stapt in de lift (alleen als lift op dezelfde verdieping is!)
     */
    public boolean voegGastToe(Persoon persoon) {
        // Check: zit persoon op dezelfde verdieping als lift?
        if (persoon instanceof Gast) {
            Gast gast = (Gast) persoon;
            if (Math.abs(gast.getY() - this.y) > 0.1) {
                return false; // Nee, persoon kan niet instappen
            }
        } else if (persoon instanceof Schoonmaker) {
            Schoonmaker schoonmaker = (Schoonmaker) persoon;
            if (Math.abs(schoonmaker.getY() - this.y) > 0.1) {
                return false; // Nee, persoon kan niet instappen
            }
        }
        
        // Check: is lift vol?
        if (passagiers.size() >= MAX_CAPACITY) {
            return false;
        }
        
        // Voeg persoon toe
        passagiers.add(persoon);
        System.out.println("[Lift] " + persoon.getNaam() + " stapt in op verdieping " + (int)y + " | Passagiers nu: " + passagiers.size());
        return true;
    }

    /**
     * Persoon stapt uit de lift
     */
    public void verwijderGast(Persoon persoon) {
        if (passagiers.remove(persoon)) {
            System.out.println("[Lift] " + persoon.getNaam() + " stapt uit op verdieping " + (int)y + " | Passagiers nu: " + passagiers.size());
        }
    }

    /**
     * Geef alle passagiers terug
     */
    public List<Persoon> getPassagiers() {
        return new ArrayList<>(passagiers);
    }

    /**
     * Check of lift vol is
     */
    public boolean isVol() {
        return passagiers.size() >= MAX_CAPACITY;
    }

    // === TICK LISTENER - Wordt elke HTE-tick aangeroepen ===
    @Override
    public void onTick() {
        // === FASE 1: AUTO-RONDE (lift maakt continu ronde) ===
        // BELANGRIJK: Lift gaat door, zelfs met passagiers!
        if (state == LiftState.IDLE) {
            // Lift staat stil -> ga volgende verdieping bezoeken
            this.targetY = (nextAutoFloor - 1) + 0.5;
            System.out.println("[Lift] Gaat naar verdieping " + nextAutoFloor + " (Y=" + String.format("%.1f", targetY) + ")");
            
            // Volgende keer volgende verdieping
            nextAutoFloor++;
            if (nextAutoFloor > schachtMaxY + 1) {
                nextAutoFloor = 1;  // Terug naar beneden
            }
        }
        
        // === FASE 2: BEWEGING NAAR TARGET ===
        if (Math.abs(targetY - y) < LIFT_SPEED) {
            // Aangekomen!
            y = targetY;
            state = LiftState.IDLE;
            System.out.println("[Lift] AANGEKOMEN op Y=" + String.format("%.1f", y) + " (Passagiers: " + passagiers.size() + ")");
        } else if (targetY > y) {
            // Omhoog
            state = LiftState.MOVING_UP;
            y += LIFT_SPEED;
        } else if (targetY < y) {
            // Omlaag
            state = LiftState.MOVING_DOWN;
            y -= LIFT_SPEED;
        }
        
        // Zorg dat lift binnen grenzen blijft
        y = Math.max(schachtMinY + 0.5, Math.min(y, schachtMaxY + 0.5));
        
        // === FASE 3: PASSAGIERS MEENEMEN ===
        for (Persoon persoon : passagiers) {
            if (persoon instanceof Gast) {
                ((Gast) persoon).setLiftPosition(this.x, this.y);
            }
        }
    }

    // === GETTERS ===
    public double getX() { return x; }
    public double getY() { return y; }
    public int getVerdieping() { return (int) y; }
    public boolean isIdle() { return state == LiftState.IDLE; }
    public int getPassagierCount() { return passagiers.size(); }
}






