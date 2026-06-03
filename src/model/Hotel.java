package model;

import model.personen.*;

import java.util.ArrayList;
import model.Kamer;
import java.util.List;
/**
 * Hotel - bevat grid + areas + personen
 */
public class Hotel {

    private final String[][] grid;
    private final int breedte;
    private final int hoogte;
    private final List<Area> areas;

    // ALLE PERSONEN IN SIMULATIE
    private final List<Persoon> personen;

    private final List<Kamer> kamers; // Alle kamers in hotel

    public Hotel(String[][] grid, int breedte, int hoogte, List<Area> areas) {
        this.grid = grid;
        this.breedte = breedte;
        this.hoogte = hoogte;
        this.areas = areas;
        this.kamers = new ArrayList<>();
        this.personen = new ArrayList<>();
    }

    public int getBreedte() { return breedte; }
    public int getHoogte() { return hoogte; }
    public List<Area> getAreas() { return areas; }

    public List<Persoon> getPersonen() {
        return personen;
    }
    public void addPersoon(Persoon p) {
        personen.add(p);
    }

    public List<Kamer> getKamers() {
        return kamers;
    }

    public void addKamer(Kamer k) {
        kamers.add(k);
    }
    public Kamer zoekVrijeKamer(String type) {
        for (Kamer k : kamers) {
            if (k.getType().equals(type) && k.getStatus() == Kamer.KamerStatus.VRIJ) {
                return k; // Gevonden
            }
        }
        return null; // Geen vrije kamer van dit type gevonden
    }
}
