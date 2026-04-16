package model;
import java.awt.Color;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

/**
 * GAST - Hotelgast die rond loopt, in/uit lift stapt en verdiepingen verandert
 */
public class Gast extends Persoon {
    private static final Random RANDOM = new Random();
    private static final double SPEED = 0.5;
    private static final double LIFT_WAIT_X = 1.5;  // Buiten schacht: wachten op lift
    private static final double TRAP_X = 8.5;       // Trap X-positie (kolom 9, dus 8.5)
    
    // === POSITIE ===
    private double x, y;           // Huidige positie
    private double destX, destY;   // Intermediate doel (bijv. naar lift toe)
    private double finalDestX, finalDestY; // Echte doel (kamer/restaurant)
    private int maxX, maxY;
    
    // === HOTEL & LIFT ===
    private Hotel hotel;
    private Lift lift;             // Referentie naar de lift
    private boolean inLift = false; // Zit gast in lift?
    private boolean usesTrap = false; // Gebruikt deze gast de trap?
    
    // === VISUAAL ===
    private Color kleur;
    
    // === TOESTANDEN ===
    private enum State {
        WANDELEN,           // Loopt normaal rond
        NAAR_LIFT_WACHTEN,  // Loopt naar lift wachtplek (X=1.5, zelfde Y)
        WACHTEN_OP_LIFT,    // Staat stil en wacht tot lift aankomt
        IN_LIFT,            // Zit in lift
        NAAR_DOEL           // Na lift: loopt naar eindbestemming
    }
    private State state = State.WANDELEN;

    public Gast(String naam, int startX, int startY) {
        super(naam, "Gast");
        this.x = startX + 0.5;
        this.y = startY + 0.5;
        this.destX = x;
        this.destY = y;
        this.finalDestX = x;
        this.finalDestY = y;
        this.kleur = new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
    }

    @Override
    public void onTick() {
        // === NORMALE BEWEGING (niet in lift) ===
        if (!inLift) {
            beweegStap();
        } else {
            // === IN LIFT: VOLG LIFT-POSITIE ===
            if (lift != null) {
                this.x = lift.getX();
                this.y = lift.getY();
                
                // === CHECK OF GAST UIT MOET STAPPEN ===
                int liftFloor = (int) lift.getY();
                int destFloor = (int) finalDestY;
                
                // Lift op doel en stilstaand?
                if (liftFloor == destFloor && lift.isIdle()) {
                    // Stap uit!
                    lift.verwijderGast(this);
                    this.inLift = false;
                    this.state = State.NAAR_DOEL;
                    this.destX = finalDestX;
                    this.destY = finalDestY;
                    System.out.println("[Gast] " + getNaam() + " stapt uit lift op verdieping " + (int)y);
                }
            }
        }
    }

    /**
     * Normale beweging als gast niet in lift zit
     */
    private void beweegStap() {
        double dx = destX - x;
        double dy = destY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Aangekomen op doel?
        if (distance < SPEED) {
            handleStateTransition();
            return;
        }
        
        // Zet stap in richting van doel
        double dirX = dx / distance;
        double dirY = dy / distance;
        x += dirX * SPEED;
        y += dirY * SPEED;
        
        // Zorg dat gast binnen hotel blijft
        x = Math.max(0.5, Math.min(x, maxX - 0.5));
        y = Math.max(0.5, Math.min(y, maxY - 0.5));
    }

    /**
     * Verwerk toestandsovergangen
     */
    private void handleStateTransition() {
        switch (state) {
            case WANDELEN:
                // === FASE 1: KIES NIEUW DOEL ===
                kiesNieuwebestemming();
                int currentFloor = (int) y;
                int newFloor = (int) finalDestY;
                
                if (currentFloor != newFloor) {
                    // === ANDER VERDIEPING: KIES LIFT OF TRAP ===
                    // 50% kans trap, 50% kans lift
                    this.usesTrap = RANDOM.nextBoolean();
                    
                    if (usesTrap) {
                        // TRAP GEBRUIKEN
                        this.destX = TRAP_X;
                        this.destY = y;  // Zelfde verdieping, maar naar trap toe
                        this.state = State.NAAR_LIFT_WACHTEN;
                        System.out.println("[Gast] " + getNaam() + " loopt naar TRAP (verdieping " + currentFloor + " → " + newFloor + ")");
                    } else {
                        // LIFT GEBRUIKEN
                        this.destX = LIFT_WAIT_X;
                        this.destY = y;  // Zelfde verdieping, maar naar lift toe
                        this.state = State.NAAR_LIFT_WACHTEN;
                        System.out.println("[Gast] " + getNaam() + " loopt naar LIFT (verdieping " + currentFloor + " → " + newFloor + ")");
                    }
                } else {
                    // ZELFDE VERDIEPING: direct naar doel
                    this.destX = finalDestX;
                    this.destY = finalDestY;
                    this.state = State.NAAR_DOEL;
                }
                break;
                
            case NAAR_LIFT_WACHTEN:
                // === FASE 2: GAST LOOPT NAAR LIFT/TRAP WACHTPLEK ===
                double targetX = usesTrap ? TRAP_X : LIFT_WAIT_X;
                
                if (Math.abs(x - targetX) < 0.2) {
                    this.x = targetX;  // Lock positie
                    this.state = State.WACHTEN_OP_LIFT;
                    System.out.println("[Gast] " + getNaam() + " wacht op " + (usesTrap ? "TRAP" : "LIFT") + " op verdieping " + (int)y);
                }
                break;
                
            case WACHTEN_OP_LIFT:
                // === FASE 3: WACHTEN OP VERVOER ===
                if (usesTrap) {
                    // === TRAP: GAST GAAT ZELF OMHOOG/OMLAAG ===
                    // Trap mag direct gebruikt worden
                    this.destX = finalDestX;
                    this.destY = finalDestY;
                    this.state = State.NAAR_DOEL;
                    System.out.println("[Gast] " + getNaam() + " stapt op TRAP en gaat naar verdieping " + (int)finalDestY);
                } else {
                    // === LIFT: WACHT TOT LIFT AANKOMT ===
                    if (lift != null && Math.abs(lift.getY() - this.y) < 0.2 && lift.isIdle()) {
                        // LIFT IS HIER! Stap in
                        if (lift.voegGastToe(this)) {
                            this.inLift = true;
                            this.state = State.IN_LIFT;
                            // Roep lift op naar doel
                            lift.roepNaar((int) finalDestY);
                            System.out.println("[Gast] " + getNaam() + " stapt in LIFT op verdieping " + (int)y);
                        }
                    }
                }
                break;
                
            case IN_LIFT:
                // === FASE 4: IN LIFT - EXIT WORDT AFGEHANDELD IN onTick() ===
                // Niets nodig, gast volgt lift en stapt uit in onTick()
                break;
                
            case NAAR_DOEL:
                // === FASE 5: LOOPT NAAR EINDBESTEMMING ===
                // Check: zijn we aangekomen?
                double dx = finalDestX - x;
                double dy = finalDestY - y;
                double distance = Math.sqrt(dx * dx + dy * dy);
                
                if (distance < 0.5) {
                    // AANGEKOMEN! Terug naar WANDELEN
                    this.state = State.WANDELEN;
                    System.out.println("[Gast] " + getNaam() + " is aangekomen op bestemming " + (int)x + "," + (int)y);
                }
                break;
        }
    }

    /**
     * Kies willekeurig nieuw doel
     */
    private void kiesNieuwebestemming() {
        if (hotel == null) {
            this.finalDestX = RANDOM.nextInt(Math.max(1, maxX - 1)) + 0.5;
            this.finalDestY = RANDOM.nextInt(Math.max(1, maxY - 1)) + 0.5;
            return;
        }
        
        List<Area> areas = hotel.getAreas();
        if (areas.isEmpty()) return;
        
        // Filter: niet naar lift/trap/schacht
        List<Area> validAreas = new ArrayList<>();
        for (Area area : areas) {
            String type = area.getAreaType();
            if (!type.equals("Lift") && !type.equals("Staircase") && !type.equals("Schacht")) {
                validAreas.add(area);
            }
        }
        
        if (validAreas.isEmpty()) return;
        
        // Kies willekeurig
        Area target = validAreas.get(RANDOM.nextInt(validAreas.size()));
        
        int randomX = (target.getX() - 1) + RANDOM.nextInt(Math.max(1, target.getBreedte()));
        int randomY = (target.getY() - 1) + RANDOM.nextInt(Math.max(1, target.getHoogte()));
        
        this.finalDestX = randomX + 0.5;
        this.finalDestY = randomY + 0.5;
    }

    /**
     * BELANGRIJK: Lift zet gast positie wanneer in lift
     */
    public void setLiftPosition(double liftX, double liftY) {
        if (inLift) {
            this.x = liftX;
            this.y = liftY;
        }
    }

    // === SETTERS ===
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

    // === GETTERS ===
    public double getX() { return x; }
    public double getY() { return y; }
    public Color getKleur() { return kleur; }
    public boolean isInLift() { return inLift; }
}










