package model;

/**
 * Basisklasse voor alle personen
 */
public abstract class Persoon implements TickListener {

    private static int volgendeId = 1;

    private int id;
    private String naam;
    private String type;

    // === VOEG DEZE VARIABELEN TOE ===
    // We maken ze 'protected' zodat Gast en Schoonmaker er direct bij kunnen
    protected double x;
    protected double y;
    protected double destX;

    public Persoon(String naam, String type) {
        this.id = volgendeId++;
        this.naam = naam;
        this.type = type;
    }

    // Deze methode werkt nu voor iedereen!
    public void setStartPositie(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.destX = startX;
        System.out.println("[Spawn] " + getNaam() + " (" + type + ") geplaatst op (" + x + ", " + y + ")");
    }

    public int getId() { return id; }
    public String getNaam() { return naam; }
    public String getType() { return type; }

    // Nodig voor de subclasses om hun positie terug te geven aan de simulator
    public double getX() { return x; }
    public double getY() { return y; }

    @Override
    public abstract void onTick();
}