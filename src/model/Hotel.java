package model;

import java.util.List;

/**
 * Stelt een hotel voor met een 2D grid en een lijst van ruimtes (areas).
 * Wordt aangemaakt door de LayoutLoader op basis van een JSON bestand.
 */
public class Hotel {

    // Het 2D grid met ruimtetypes als strings (bijv. "L", "F", "T")
    private String[][] grid;

    // Breedte en hoogte van het grid in vakjes
    private int breedte;
    private int hoogte;

    // Lijst van alle ruimtes (kamers, faciliteiten, lobby etc.)
    private List<Area> areas;

    /**
     * Maakt een nieuw Hotel aan met een grid en lijst van areas.
     */
    public Hotel(String[][] grid, int breedte, int hoogte, List<Area> areas) {
        this.grid = grid;
        this.breedte = breedte;
        this.hoogte = hoogte;
        this.areas = areas;
    }

    // Geeft het 2D grid terug
    public String[][] getGrid() { return grid; }

    // Geeft de breedte van het grid terug
    public int getBreedte() { return breedte; }

    // Geeft de hoogte van het grid terug
    public int getHoogte() { return hoogte; }

    // Geeft de lijst van alle areas terug
    public List<Area> getAreas() { return areas; }
}