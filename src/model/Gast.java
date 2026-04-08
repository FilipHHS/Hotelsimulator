package model;

import java.awt.Color;
import java.util.Random;

/**
 * Gast - subtype van Persoon
 * Heeft positie, bestemming en simpele AI movement
 */
public class Gast extends Persoon {

    private static final Random RANDOM = new Random();

    // HUIDIGE POSITIE
    private int x;
    private int y;

    // DOEL POSITIE
    private int destX;
    private int destY;

    // VISUELE KLEUR
    private Color kleur;

    // STATUS (bijv lobby, wandelen etc.)
    private String status;

    // GRID LIMIETEN
    private int maxX = 10;
    private int maxY = 10;

    public Gast(String naam, int startX, int startY) {
        super(naam, "Gast");

        this.x = startX;
        this.y = startY;

        this.kleur = generateRandomColor();
        this.status = "in_lobby";

        // INIT FASE - eerste bestemming kiezen
        chooseRandomDestination();
    }

    // RANDOM KLEUR GENEREREN
    private static Color generateRandomColor() {
        int r = 100 + RANDOM.nextInt(156);
        int g = 100 + RANDOM.nextInt(156);
        int b = 100 + RANDOM.nextInt(156);
        return new Color(r, g, b);
    }

    // NIEUWE BESTEMMING KIEZEN
    private void chooseRandomDestination() {
        this.destX = RANDOM.nextInt(this.maxX);
        this.destY = RANDOM.nextInt(this.maxY);
    }

    // GRID GRENZEN INSTELLEN
    public void setGridBounds(int maxX, int maxY) {
        this.maxX = maxX;
        this.maxY = maxY;
    }

    // GETTERS
    public int getX() { return x; }
    public int getY() { return y; }
    public Color getKleur() { return kleur; }

    // UPDATE FASE - beweging per tick
    public void update() {

        // ALS DOEL BEREIKT → NIEUW DOEL
        if (this.x == this.destX && this.y == this.destY) {
            chooseRandomDestination();
        }

        // BEWEGING RICHTING X
        if (this.x < this.destX) x++;
        else if (this.x > this.destX) x--;

        // BEWEGING RICHTING Y
        if (this.y < this.destY) y++;
        else if (this.y > this.destY) y--;
    }

    @Override
    public String toString() {
        return "Gast{id=" + getId() +
                ", naam='" + getNaam() +
                "', pos=(" + x + "," + y + ")" +
                ", dest=(" + destX + "," + destY + ")" +
                ", status='" + status + "'}";
    }
}