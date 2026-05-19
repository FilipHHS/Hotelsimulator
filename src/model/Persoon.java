package model;

/**
 * Basisklasse voor alle personen (zoals Gasten en Schoonmakers) binnen de simulatie.
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

    public Persoon(String naam, String type) {
        this.id = volgendeId++;
        this.naam = naam;
        this.type = type;
    }

    /**
     * Zet de beginpositie en het initiële doel van de persoon (bijv. bij het spawnen).
     */
    public void setStartPositie(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.destX = startX;
        this.destY = startY;
        System.out.println("[Spawn] " + naam + " (" + type + ") geplaatst op (" + x + ", " + y + ")");
    }

    /**
     * Updates de positie van de persoon wanneer deze zich in de lift bevindt.
     * Zorgt ervoor dat het doel (destination) meebeweegt met de lift.
     */
    public void setLiftPosition(double liftX, double liftY) {
        this.x = liftX;
        this.y = liftY;
        this.destX = liftX;
        this.destY = liftY;
    }

    // --- BRANDALARM METHODEN ---

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
    public String getHuidigeActiviteit() { return huidigeActiviteit; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setHuidigeActiviteit(String activiteit) { this.huidigeActiviteit = activiteit; }

    public boolean isFireAlarmActive() { return fireAlarmActive; }
    public boolean isEvacuatieBegonnen() { return evacuatieBegonnen; }

    @Override
    public abstract void onTick();
}