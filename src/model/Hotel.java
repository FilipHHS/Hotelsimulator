package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Hotel - bevat grid + areas + personen
 */
public class Hotel {

    private String[][] grid;
    private int breedte;
    private int hoogte;
    private List<Area> areas;

    // ALLE PERSONEN IN SIMULATIE
    private List<Persoon> personen;

    public Hotel(String[][] grid, int breedte, int hoogte, List<Area> areas) {
        this.grid = grid;
        this.breedte = breedte;
        this.hoogte = hoogte;
        this.areas = areas;

        // INIT FASE - lege lijst
        this.personen = new ArrayList<>();
    }

    public String[][] getGrid() { return grid; }
    public int getBreedte() { return breedte; }
    public int getHoogte() { return hoogte; }
    public List<Area> getAreas() { return areas; }

    // ALLE PERSONEN OPHALEN
    public List<Persoon> getPersonen() {
        return personen;
    }

    // PERSOON TOEVOEGEN
    public void addPersoon(Persoon p) {
        personen.add(p);
    }
}