package model;

/**
 * Basisklasse voor alle personen
 */
public abstract class Persoon implements TickListener {

    private static int volgendeId = 1;

    private int id;
    private String naam;
    private String type;
    protected String huidigeActiviteit = ""; // Wat is de persoon aan het doen?

    // 'protected' zodat subclasses (Gast, Schoonmaker) ze kunnen gebruiken
    protected double x;
    protected double y;
    protected double destX;
    protected double destY; // destY toegevoegd voor volledige bewegingsvrijheid
    
    // FireAlarm evacuation flag
    protected boolean fireAlarmActive = false;
    protected boolean evacuatieBegonnen = false;

    public Persoon(String naam, String type) {
        this.id = volgendeId++;
        this.naam = naam;
        this.type = type;
    }

    /**
     * Zet de positie van de persoon (bijv. bij spawn)
     */
    public void setStartPositie(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.destX = startX;
        this.destY = startY;
        System.out.println("[Spawn] " + getNaam() + " (" + type + ") geplaatst op (" + x + ", " + y + ")");
    }

    /**
     * DIT IS DE FIX VOOR DE LIFT:
     * Omdat deze methode nu in Persoon staat, kan de Lift-klasse
     * p.setLiftPosition(...) aanroepen zonder foutmeldingen!
     */
    public void setLiftPosition(double lx, double ly) {
        this.x = lx;
        this.y = ly;
        // Zorg dat het doel van de persoon meebeweegt zodat ze niet
        // uit de lift proberen te rennen terwijl deze beweegt.
        this.destX = lx;
        this.destY = ly;
    }

    // Standaard Getters
    public int getId() { return id; }
    public String getNaam() { return naam; }
    public String getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public String getHuidigeActiviteit() { return huidigeActiviteit; }

    // Standaard Setters (handig voor algemeen gebruik)
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setHuidigeActiviteit(String activiteit) { this.huidigeActiviteit = activiteit; }
    
    // Fire Alarm methods
    public void activeerFireAlarm() {
        this.fireAlarmActive = true;
        this.evacuatieBegonnen = false;
        System.out.println("[FireAlarm] 🔥 ALARM GEACTIVEERD voor " + naam);
    }
    
    public void deactiveerFireAlarm() {
        this.fireAlarmActive = false;
        System.out.println("[FireAlarm] ✓ ALARM GEDEACTIVEERD voor " + naam);
    }
    
    public boolean isFireAlarmActive() { return fireAlarmActive; }
    public boolean isEvacuatieBegonnen() { return evacuatieBegonnen; }

    @Override
    public abstract void onTick();
}