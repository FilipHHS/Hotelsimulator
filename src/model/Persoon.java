package model;

import model.strategy.IMovementStrategy;

/**
 * Basisklasse voor alle personen (zoals Gasten en Schoonmakers) binnen de simulatie.
 * Gebruikt het Strategy Pattern voor het afhandelen van loopgedrag.
 */
public abstract class Persoon implements TickListener {

    private static int volgendeId = 1;

    private final int id;
    private final String naam;
    private final String type;

    protected String huidigeActiviteit = "";
    protected double x;
    protected double y;
    protected double destX;
    protected double destY;

    protected boolean fireAlarmActive = false;
    protected boolean evacuatieBegonnen = false;

    // --- STRATEGY PATTERN COMPONENTEN ---

    // De huidige bewegingsstrategie van de persoon (bijv. Normal, Evacuation, Panic)
    protected IMovementStrategy movementStrategy;

    public Persoon(String naam, String type) {
        this.id = volgendeId++;
        this.naam = naam;
        this.type = type;
    }

    /**
     * STRATEGY PATTERN: Voert de beweging uit gebaseerd op de actieve strategie.
     * De Persoon weet zelf niet HOE hij loopt, dat bepaalt het strategie-object.
     */
    public void performMovement() {
        if (movementStrategy != null) {
            movementStrategy.move(this); // Geef 'zichzelf' mee aan de strategie
        }
    }

    /**
     * STRATEGY PATTERN: Verander het gedrag tijdens de simulatie (Runtime binding).
     * Handig voor wanneer het brandalarm afgaat of een gast van gedachten verandert.
     */
    public void setMovementStrategy(IMovementStrategy strategy) {
        this.movementStrategy = strategy;
    }

    // ------------------------------------

    public void setStartPositie(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.destX = startX;
        this.destY = startY;
        System.out.println("[Spawn] " + naam + " (" + type + ") geplaatst op (" + x + ", " + y + ")");
    }

    public void setLiftPosition(double liftX, double liftY) {
        this.x = liftX;
        this.y = liftY;
        this.destX = liftX;
        this.destY = liftY;
    }

    public void activeerFireAlarm() {
        this.fireAlarmActive = true;
        this.evacuatieBegonnen = false;
        System.out.println("[FireAlarm] 🔥 ALARM GEACTIVEERD voor " + naam);
    }

    public void deactiveerFireAlarm() {
        this.fireAlarmActive = false;
        System.out.println("[FireAlarm] ✓ ALARM GEDEACTIVEERD voor " + naam);
    }

    // --- GETTERS & SETTERS ---
    public int getId() { return id; }
    public String getNaam() { return naam; }
    public String getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getDestX() { return destX; }
    public double getDestY() { return destY; }
    public String getHuidigeActiviteit() { return huidigeActiviteit; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setDestX(double destX) { this.destX = destX; }
    public void setDestY(double destY) { this.destY = destY; }
    public void setHuidigeActiviteit(String activiteit) { this.huidigeActiviteit = activiteit; }

    public boolean isFireAlarmActive() { return fireAlarmActive; }
    public boolean isEvacuatieBegonnen() { return evacuatieBegonnen; }
    public void setEvacuatieBegonnen(boolean begonnen) { this.evacuatieBegonnen = begonnen; }

    @Override
    public abstract void onTick();
}