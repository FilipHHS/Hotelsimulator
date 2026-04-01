package model;

import java.awt.Color;
import java.util.Random;

/**
 * Gast - Een gast die verblijft in het hotel.
 * Gasten hebben een positie op het grid en bewegen door het hotel.
 */
public class Gast extends Persoon {
    
    private static final Random RANDOM = new Random();
    
    // Positie van de gast (x, y op het grid)
    private int x;
    private int y;
    
    // Destinatie waar de gast naar toe wil
    private int destX;
    private int destY;
    
    // Kleur van de gast
    private Color kleur;
    
    // Huidige status/activiteit van de gast
    private String status; // bijv. "in_kamer", "naar_restaurant", "in_lobby"
    
    // Grid boundaries voor random movement
    private int maxX = 10;
    private int maxY = 10;

    /**
     * Constructor voor een nieuwe gast.
     * @param naam De naam van de gast
     * @param startX De X-positie waar de gast start
     * @param startY De Y-positie waar de gast start
     */
    public Gast(String naam, int startX, int startY) {
        super(naam, "Gast");
        this.x = startX;
        this.y = startY;
        this.kleur = generateRandomColor();
        this.status = "in_lobby";
        // Eerste willekeurige destinatie
        chooseRandomDestination();
    }
    
    /**
     * Genereer een willekeurige, levendige kleur voor de gast.
     */
    private static Color generateRandomColor() {
        int r = 100 + RANDOM.nextInt(156); // 100-255
        int g = 100 + RANDOM.nextInt(156); // 100-255
        int b = 100 + RANDOM.nextInt(156); // 100-255
        return new Color(r, g, b);
    }
    
    /**
     * Kies een willekeurig doel op het grid.
     */
    private void chooseRandomDestination() {
        this.destX = RANDOM.nextInt(maxX);
        this.destY = RANDOM.nextInt(maxY);
    }
    
    /**
     * Stel het grid maximum in voor random movement.
     */
    public void setGridBounds(int maxX, int maxY) {
        this.maxX = maxX;
        this.maxY = maxY;
    }

    // Getters en Setters voor positie
    public int getX() { return x; }
    public int getY() { return y; }
    
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    
    // Getters en Setters voor destinatie
    public int getDestX() { return destX; }
    public int getDestY() { return destY; }
    
    public void setDestX(int destX) { this.destX = destX; }
    public void setDestY(int destY) { this.destY = destY; }
    
    // Getters voor kleur
    public Color getKleur() { return kleur; }
    
    // Getters en Setters voor status
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    /**
     * Beweeg de gast naar de destinatie (simpele AI).
     * Elke tick beweegt de gast een stap in de richting van de destinatie.
     * Wanneer het doel bereikt is, kiest de gast automatisch een nieuw willekeurig doel.
     */
    public void update() {
        // Controleer of we op de destinatie zijn
        if (x == destX && y == destY) {
            // Kies een nieuw doel en begin meteen naar toe te lopen
            chooseRandomDestination();
        }
        
        // Beweeg een stap dichter naar de destinatie
        if (x < destX) x++;
        else if (x > destX) x--;
        
        if (y < destY) y++;
        else if (y > destY) y--;
    }

    @Override
    public String toString() {
        return "Gast{" +
                "id=" + getId() +
                ", naam='" + getNaam() + '\'' +
                ", pos=(" + x + "," + y + ")" +
                ", dest=(" + destX + "," + destY + ")" +
                ", status='" + status + '\'' +
                '}';
    }
}


