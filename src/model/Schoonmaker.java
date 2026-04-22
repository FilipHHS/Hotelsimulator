package model;

import java.awt.Color;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

/**
 * SCHOONMAKER - Beweegt random zoals gast, maar gaat naar vieze kamers
 * Dezelfde movement regels als Gast
 */
public class Schoonmaker extends Persoon {
    private static final Random RANDOM = new Random();
    private static final double SPEED = 0.5;
    
    // === POSITIE ===
    private double x, y;
    private double destX;
    private int maxX, maxY;
    
    // === HOTEL & KAMERS ===
    private Hotel hotel;
    private Kamer huidigKamer;
    private int schoonmaakTijd;
    private static final int SCHOONMAAK_DUUR = 50;
    private static int schoonmaakCounter = 0;  // Gedeelde counter voor alle schoonmakers
    private Lift lift;  // Voor lift-vervoer naar andere verdiepingen
    private static final double LIFT_WAIT_X = 1.5;
    private static final double TRAP_X = 8.5;
    private boolean inLift = false;
    private Kamer laatsteGewerktKamer = null;  // Voor load-balancing
    private int schoonmakerID;  // Unieke ID voor elke schoonmaker
    
    // === VISUAAL ===
    private Color kleur;
    
    // === TOESTANDEN ===
    private enum State {
        WANDELEN,           // Random rondlopen
        NAAR_KAMER,         // Naar vieze kamer toe
        SCHOONMAKEN,        // Aan het schoonmaken
        NAAR_LIFT_WACHTEN,  // Loopt naar lift/trap
        IN_LIFT,            // In lift
    }
    private State state = State.WANDELEN;
    
    // === RANDOM WALK (zelfde als Gast) ===
    private int stapsInRichting = 0;
    private int maxStapsRichting = 5;

    public Schoonmaker(String naam, int startX, int startY) {
        super(naam, "Schoonmaker");
        this.x = startX + 0.5;
        this.y = startY + 0.5;
        this.destX = x;
        this.schoonmaakTijd = 0;
        this.kleur = new Color(128, 128, 128);  // Grijs
        this.schoonmakerID = schoonmaakCounter++;  // Krijg unieke ID
        System.out.println("[Schoonmaker] " + naam + " krijgt ID: " + schoonmakerID);
    }

    @Override
    public void onTick() {
        // Als in lift: update positie
        if (inLift) {
            if (lift != null) {
                this.x = lift.getX();
                this.y = lift.getY();
                
                // Kamer op andere verdieping?
                Kamer viezeKamer = zoekEerstViezeKamer();
                if (viezeKamer != null) {
                    int liftFloor = (int) lift.getY();
                    int kamerFloor = (int) viezeKamer.getArea().getY();
                    
                    if (liftFloor == kamerFloor && lift.isIdle()) {
                        lift.verwijderGast(this);
                        this.inLift = false;
                        this.state = State.WANDELEN;
                        System.out.println("[Schoonmaker] " + getNaam() + " stapt uit lift op verdieping " + liftFloor);
                    }
                }
            }
            return;
        }
        
        // Check eerst of er vieze kamers zijn
        Kamer viezeKamer = zoekEerstViezeKamer();
        
        if (viezeKamer != null && state == State.WANDELEN) {
            // Vieze kamer gevonden - check if op andere verdieping
            Area kamerArea = viezeKamer.getArea();
            if (kamerArea != null) {
                int currentFloor = (int) y;
                int kamerFloor = (int) kamerArea.getY();
                
                if (currentFloor != kamerFloor) {
                    // Ga naar lift
                    this.huidigKamer = viezeKamer;
                    this.destX = LIFT_WAIT_X;
                    this.state = State.NAAR_LIFT_WACHTEN;
                    System.out.println("[Schoonmaker] " + getNaam() + " gaat naar lift voor kamer op verdieping " + kamerFloor);
                } else {
                    // Zelfde verdieping - ga direct
                    this.huidigKamer = viezeKamer;
                    this.destX = kamerArea.getX() + kamerArea.getBreedte() / 2.0 - 0.5;
                    this.state = State.NAAR_KAMER;
                    System.out.println("[Schoonmaker] " + getNaam() + " gaat naar vieze kamer " + huidigKamer.getKamernummer());
                }
            }
        }
        
        // Voer huidige state actie uit
        switch (state) {
            case WANDELEN:
                randomWalk();
                break;
            case NAAR_KAMER:
                beweegNaarKamer();
                break;
            case SCHOONMAKEN:
                maakSchoon();
                break;
            case NAAR_LIFT_WACHTEN:
                beweegNaarLift();
                break;
            case IN_LIFT:
                // Niets - positie wordt hierboven al gestuurd
                break;
        }
    }

    /**
     * Random walk - exact dezelfde als Gast met veilige grenzen
     */
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
    }

    /**
     * Beweeg naar de vieze kamer
     */
    private void beweegNaarKamer() {
        if (huidigKamer == null || huidigKamer.getArea() == null) {
            this.state = State.WANDELEN;
            return;
        }
        
        Area kamerArea = huidigKamer.getArea();
        double dx = kamerArea.getX() + kamerArea.getBreedte() / 2.0 - 0.5 - x;
        double dy = kamerArea.getY() + kamerArea.getHoogte() / 2.0 - 0.5 - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Aangekomen bij kamer?
        if (distance < SPEED) {
            this.state = State.SCHOONMAKEN;
            this.schoonmaakTijd = SCHOONMAAK_DUUR;
            System.out.println("[Schoonmaker] " + getNaam() + " begint met schoonmaken van kamer " + huidigKamer.getKamernummer());
            return;
        }

        // Zet stap in richting van doel
        double dirX = dx / distance;
        double dirY = dy / distance;
        x += dirX * SPEED;
        y += dirY * SPEED;

        // Zorg dat schoonmaker binnen hotel blijft
        x = Math.max(0.5, Math.min(x, maxX - 0.5));
        y = Math.max(0.5, Math.min(y, maxY - 0.5));
    }

    /**
     * Maak de huidige kamer schoon
     */
    private void maakSchoon() {
        if (huidigKamer == null) {
            this.state = State.WANDELEN;
            return;
        }

        schoonmaakTijd--;

        if (schoonmaakTijd <= 0) {
            huidigKamer.setStatus(Kamer.KamerStatus.VRIJ);
            System.out.println("[Schoonmaker] " + getNaam() + " heeft kamer " + huidigKamer.getKamernummer() + " schoongemaakt!");
            this.laatsteGewerktKamer = huidigKamer;  // Onthoud welke kamer we net deden
            this.huidigKamer = null;
            this.state = State.WANDELEN;
        }
    }

    /**
     * Zoek vieze kamer - elke schoonmaker neemt er een ander op basis van ID
     */
    private Kamer zoekEerstViezeKamer() {
        if (hotel == null) return null;
        
        // Zoek ALLE vieze kamers
        List<Kamer> viezeKamers = new ArrayList<>();
        for (Kamer kamer : hotel.getKamers()) {
            if (kamer.getStatus() == Kamer.KamerStatus.SCHOONMAKEN && 
                !kamer.equals(huidigKamer)) {  // Skip de kamer die ik nu doe
                viezeKamers.add(kamer);
            }
        }
        
        if (viezeKamers.isEmpty()) {
            return null;
        }
        
        // LOAD BALANCING: Schoonmaker ID bepaalt welke kamer
        // ID 0 → kamer index 0
        // ID 1 → kamer index 1
        // ID 2 → kamer index 2
        // etc., wrap around met modulo
        int kamerIndex = schoonmakerID % viezeKamers.size();
        return viezeKamers.get(kamerIndex);
    }

    /**
     * Beweeg naar lift
     */
    private void beweegNaarLift() {
        double dx = LIFT_WAIT_X - x;
        
        if (Math.abs(dx) < SPEED * 2) {
            x = LIFT_WAIT_X;
            
            // Probeer in lift stappen
            if (lift != null && Math.abs(lift.getY() - this.y) < 0.2 && lift.isIdle()) {
                Kamer targetKamer = huidigKamer;
                if (targetKamer != null && targetKamer.getArea() != null) {
                    int targetFloor = (int) targetKamer.getArea().getY();
                    
                    if (lift.voegGastToe(this)) {
                        this.inLift = true;
                        this.destX = targetFloor + 0.5;
                        this.state = State.IN_LIFT;
                        lift.roepNaar(targetFloor);
                        System.out.println("[Schoonmaker] " + getNaam() + " stapt in lift naar verdieping " + targetFloor);
                    }
                }
            }
            return;
        }
        
        if (dx > 0) {
            x += SPEED;
        } else {
            x -= SPEED;
        }
        
        x = Math.max(0.5, Math.min(x, maxX - 0.5));
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
}


















