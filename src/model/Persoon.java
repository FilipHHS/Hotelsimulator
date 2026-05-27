package model;

import model.strategy.IMovementStrategy;
import model.strategy.MovementContext;

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

    // Strategy Pattern rol: MovementContext is de context class.
    // Persoon gebruikt de context, maar beheert de concrete strategies niet zelf.
    private final MovementContext movementContext;

    public Persoon(String naam, String type, IMovementStrategy normalMovementStrategy,
                   IMovementStrategy evacuationMovementStrategy) {
        this.id = volgendeId++;
        this.naam = naam;
        this.type = type;
        this.movementContext = new MovementContext(normalMovementStrategy, evacuationMovementStrategy);
    }

    /**
     * Enige ingang voor beweging bij personen.
     * De actieve strategy bepaalt het doel, Persoon voert de beweging uit.
     * Hier wordt polymorfisme gebruikt: GastNormalStrategy, SchoonmakerNormalStrategy
     * of EvacuationMovement kan achter dezelfde interface zitten.
     */
    public void beweeg() {
        movementContext.beweeg(this);
    }

    /**
     * Enige basis-beweegmethode voor fysieke verplaatsing.
     * Strategies gebruiken deze helper zodat x/y-aanpassingen niet overal los staan.
     *
     * @return true als de persoon het doel heeft bereikt
     */
    public boolean beweegNaar(double doelX, double doelY, double snelheid) {
        this.destX = doelX;
        this.destY = doelY;

        if (Math.abs(doelX - x) > snelheid) {
            x += Math.signum(doelX - x) * snelheid;
            return false;
        }
        x = doelX;

        if (Math.abs(doelY - y) > snelheid) {
            y += Math.signum(doelY - y) * snelheid;
            return false;
        }
        y = doelY;

        return true;
    }

    /**
     * Strategy-injectie: de buitenwereld bepaalt welke algoritmes deze persoon gebruikt.
     * Simulator gebruikt dit om concrete strategies mee te geven.
     * Daardoor maakt Persoon zelf geen new GastNormalStrategy() of new EvacuationMovement().
     */
    public void setMovementStrategies(IMovementStrategy normalMovementStrategy,
                                      IMovementStrategy evacuationMovementStrategy) {
        movementContext.setStrategies(normalMovementStrategy, evacuationMovementStrategy);
    }

    // Wissel runtime tussen de geinjecteerde strategies.
    protected void useNormalMovementStrategy() {
        movementContext.useNormalStrategy();
    }

    protected void useEvacuationMovementStrategy() {
        movementContext.useEvacuationStrategy();
    }

    protected boolean isUsingEvacuationMovementStrategy() {
        return movementContext.isUsingEvacuationStrategy();
    }

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
