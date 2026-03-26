package model;

/**
 * De Area klasse representeert een specifieke ruimte in het hotel.
 * Deze klasse bevat de data die direct uit de layout-configuratie komt
 * en biedt handige methodes om deze data om te zetten naar bruikbare coördinaten.
 */
public class Area {
    // Ruwe datavelden (meestal ingevuld door een JSON-parser zoals Jackson of Gson)
    private String AreaType;       // Bijv: "Room", "Cinema", "Lobby"
    private String Position;       // Bijv: "1, 1" (X, Y)
    private String Dimension;      // Bijv: "2, 1" (Breedte, Hoogte)
    private String Classification; // Bijv: "3 Star" (alleen voor kamers)
    private int Capacity;          // Maximaal aantal personen

    // --- Standaard Getters ---
    public String getAreaType() { return AreaType; }
    public String getPosition() { return Position; }
    public String getDimension() { return Dimension; }
    public String getClassification() { return Classification; }
    public int getCapacity() { return Capacity; }

    /**
     * Haalt de X-coördinaat op uit de Position string.
     * split(",") splitst de tekst op de komma, trim() verwijdert spaties,
     * en Integer.parseInt maakt er een echt getal van.
     */
    public int getX() {
        return Integer.parseInt(Position.split(",")[0].trim());
    }

    /**
     * Haalt de Y-coördinaat op uit de Position string (het tweede getal).
     */
    public int getY() {
        return Integer.parseInt(Position.split(",")[1].trim());
    }

    /**
     * Haalt de breedte op uit de Dimension string (het eerste getal).
     */
    public int getBreedte() {
        return Integer.parseInt(Dimension.split(",")[0].trim());
    }

    /**
     * Haalt de hoogte op uit de Dimension string (het tweede getal).
     */
    public int getHoogte() {
        return Integer.parseInt(Dimension.split(",")[1].trim());
    }
}