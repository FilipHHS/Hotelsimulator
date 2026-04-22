package model;
import java.awt.Color;
import java.util.Random;

/**
 * GAST - Hotelgast die random wandelt (links-rechts) op dezelfde verdieping
 * Alleen via lift/trap kan gast naar andere verdiepingen
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
    private String huidigerFaciliteitType = null;  // Restaurant, Fitness, Lounge, etc.
    private double faciliteitX, faciliteitY;       // Doel-coördinaten van faciliteit
    private int faciliteitsBezoekDuur = 0;         // Hoe lang gast in faciliteit blijft
    
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

    private void randomWalk() {
        if (stapsInRichting >= maxStapsRichting) {
            int keuze = RANDOM.nextInt(10);
            if (keuze < 4) {
                destX = Math.max(2.0, x - 3);  // Minimum 2.0 (links van trap)
            } else if (keuze < 8) {
                destX = Math.min(maxX - 3.0, x + 3);  // Maximum maxX-3 (rechts van trap)
            } else {
                destX = x;
            }
            stapsInRichting = 0;
            maxStapsRichting = RANDOM.nextInt(3) + 2;
        }
        
        double dx = destX - x;
        
        if (Math.abs(dx) < SPEED) {
            x = destX;
            stapsInRichting = maxStapsRichting;
        } else if (dx > 0) {
            x += SPEED;
        } else {
            x -= SPEED;
        }
        
        x = Math.max(0.5, Math.min(x, maxX - 0.5));
        stapsInRichting++;
        
        if (RANDOM.nextDouble() < 0.03) {
            wiltVerdiepingWisselen();
        }
    }

    private void wiltVerdiepingWisselen() {
        this.usesTrap = RANDOM.nextBoolean();
        
        if (usesTrap) {
            this.destX = TRAP_X;
        } else {
            this.destX = LIFT_WAIT_X;
        }
        
        this.state = State.NAAR_LIFT_WACHTEN;
        System.out.println("[Gast] " + getNaam() + " loopt naar " + (usesTrap ? "TRAP" : "LIFT"));
    }

    private void beweegNaarLiftTrap() {
        double dx = destX - x;
        
        if (Math.abs(dx) < SPEED * 2) {  // Groter tolerance zone
            x = destX;
            this.state = State.WACHTEN_OP_VERVOER;
            System.out.println("[Gast] " + getNaam() + " wacht op " + (usesTrap ? "trap" : "lift") + " op X=" + String.format("%.1f", destX));
            return;
        }
        
        if (dx > 0) {
            x += SPEED;
        } else {
            x -= SPEED;
        }
        
        x = Math.max(0.5, Math.min(x, maxX - 0.5));
    }

    private void wachtOpVervoer() {
        if (usesTrap) {
            int currentFloor = (int) y;
            int newFloor = currentFloor + (RANDOM.nextBoolean() ? 1 : -1);
            newFloor = Math.max(0, Math.min(newFloor, maxY - 1));
            
            // Trap gebruiken - ga naar nieuw doel
            this.y = newFloor + 0.5;
            this.destX = x;  // Blijf op huidige X positie na trap
            this.state = State.WANDELEN;
            System.out.println("[Gast] " + getNaam() + " gaat trap naar verdieping " + newFloor);
        } else {
            if (lift != null && Math.abs(lift.getY() - this.y) < 0.2 && lift.isIdle()) {
                int currentFloor = (int) y;
                int newFloor = currentFloor + (RANDOM.nextBoolean() ? 1 : -1);
                newFloor = Math.max(0, Math.min(newFloor, maxY - 1));
                
                if (lift.voegGastToe(this)) {
                    this.inLift = true;
                    this.destX = newFloor + 0.5;  // Opslaan als intermediaire waarde
                    this.state = State.IN_LIFT;
                    lift.roepNaar(newFloor);
                    System.out.println("[Gast] " + getNaam() + " stapt in lift naar verdieping " + newFloor);
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
            System.out.println("[Gast] " + getNaam() + " kon niet inchecken!");
            return false;
        }

        this.huidigKamer = kamer;
        kamer.setStatus(Kamer.KamerStatus.BEZET);
        System.out.println("[Gast] " + getNaam() + " incheckt in kamer " + kamer.getKamernummer());
        return true;
    }

    public void checkoutKamer() {
        if (huidigKamer != null) {
            huidigKamer.setStatus(Kamer.KamerStatus.SCHOONMAKEN);
            System.out.println("[Gast] " + getNaam() + " checkt uit van kamer " + huidigKamer.getKamernummer());
            this.huidigKamer = null;
        }
    }

    public Kamer getHuidigKamer() {
        return huidigKamer;
    }

    // ===== FACILITEITEN SYSTEEM =====
    
    /**
     * Gast gaat naar een specifieke faciliteit.
     * Zoekt automatisch de faciliteit-coördinaten uit het hotel.
     * BELANGRIJK: Gast kan alleen naar faciliteiten op dezelfde verdieping (Y) gaan!
     */
    public void gaatNaarFaciliteit(String faciliteitsType) {
        if (hotel == null) {
            System.out.println("[Gast] " + getNaam() + " kan niet naar " + faciliteitsType + " - geen hotel!");
            return;
        }
        
        // Zoek de faciliteit in het hotel (Areas)
        Area faciliteitsArea = null;
        for (Area area : hotel.getAreas()) {
            if (faciliteitsType.equalsIgnoreCase(area.getAreaType())) {
                faciliteitsArea = area;
                break;
            }
        }
        
        if (faciliteitsArea == null) {
            System.out.println("[Gast] " + getNaam() + " kan faciliteit " + faciliteitsType + " niet vinden!");
            return;
        }
        
        // CHECK: Faciliteit moet op dezelfde verdieping zijn als gast!
        int faciliteitsY = faciliteitsArea.getY();
        int gastY = (int) this.y;
        
        if (faciliteitsY != gastY) {
            System.out.println("[Gast] " + getNaam() + " kan niet naar " + faciliteitsType 
                + " - anders verdieping! (Gast op Y=" + gastY + ", Faciliteit op Y=" + faciliteitsY + ")");
            return;
        }
        
        // Zet doel-coördinaten (het midden van de faciliteit)
        double newFaciliteitX = faciliteitsArea.getX() + faciliteitsArea.getBreedte() / 2.0;
        double newFaciliteitY = faciliteitsArea.getY() + 0.5;
        
        // VEILIGHEID: Zorg dat doel binnen grid grenzen blijft
        newFaciliteitX = Math.max(0.5, Math.min(newFaciliteitX, maxX - 0.5));
        newFaciliteitY = Math.max(0.5, Math.min(newFaciliteitY, maxY - 0.5));
        
        this.faciliteitX = newFaciliteitX;
        this.faciliteitY = newFaciliteitY;
        this.huidigerFaciliteitType = faciliteitsType;
        this.state = State.GAAT_NAAR_FACILITEIT;
        this.destX = this.faciliteitX;
        
        System.out.println("[Gast] " + getNaam() + " loopt naar " + faciliteitsType 
            + " op (" + String.format("%.1f", faciliteitX) + ", " + String.format("%.1f", faciliteitY) + ")");
    }
    
    /**
     * Beweeg naar de doel-coördinaten van de faciliteit (ZOWEL X als Y)
     */
    private void beweegNaarFaciliteit() {
        double dx = faciliteitX - x;
        double dy = faciliteitY - y;
        
        // Check if reached destination (beide X en Y)
        if (Math.abs(dx) < SPEED * 2 && Math.abs(dy) < SPEED * 2) {
            x = faciliteitX;
            y = faciliteitY;
            this.state = State.IN_FACILITEIT;
            this.faciliteitsBezoekDuur = 10 + RANDOM.nextInt(20);  // 10-30 ticks blijven
            System.out.println("[Gast] " + getNaam() + " is aangekomen bij " + huidigerFaciliteitType);
            return;
        }
        
        // Beweeg richting X
        if (dx > 0) {
            x += SPEED;
        } else if (dx < 0) {
            x -= SPEED;
        }
        
        // Beweeg richting Y
        if (dy > 0) {
            y += SPEED;
        } else if (dy < 0) {
            y -= SPEED;
        }
        
        // VEILIGHEID: Zorg dat gast binnen grenzen blijft
        x = Math.max(0.5, Math.min(x, maxX - 0.5));
        y = Math.max(0.5, Math.min(y, maxY - 0.5));
    }
    
    /**
     * Gast zit in faciliteit en geniet ervan
     */
    private void zitInFaciliteit() {
        faciliteitsBezoekDuur--;
        
        if (faciliteitsBezoekDuur <= 0) {
            System.out.println("[Gast] " + getNaam() + " verlaat " + huidigerFaciliteitType);
            this.huidigerFaciliteitType = null;
            this.state = State.WANDELEN;
            this.destX = x;  // Blijf op huidige plek
        }
    }
    
    public String getHuidigerFaciliteitType() {
        return huidigerFaciliteitType;
    }
    
    public boolean isInFaciliteit() {
        return state == State.IN_FACILITEIT;
    }
}

