package model;

import model.personen.*;

import java.util.ArrayList;
import model.Kamer;
import java.util.List;
/**
 * Hotel - bevat grid + areas + personen
 * US3.7: Houdt nu ook de positie van personen bij op een apart grid.
 */
public class Hotel {

    private final String[][] grid;
    private final int breedte;
    private final int hoogte;
    private final List<Area> areas;

    // ALLE PERSONEN IN SIMULATIE
    private final List<Persoon> personen;

    private final List<Kamer> kamers; // Alle kamers in hotel

    // --- NIEUW: US3.7 Positie bijhouden ---
    private final Persoon[][] positieGrid;

    public Hotel(String[][] grid, int breedte, int hoogte, List<Area> areas) {
        this.grid = grid;
        this.breedte = breedte;
        this.hoogte = hoogte;
        this.areas = areas;
        this.kamers = new ArrayList<>();
        this.personen = new ArrayList<>();
        // Initialiseer het grid voor personen
        this.positieGrid = new Persoon[hoogte][breedte];
    }
    /**
     * Updates de positie van een persoon in het administratieve grid.
     * Wordt aangeroepen door de Simulator na een beweging.
     */
    public void updatePersoonPositie(Persoon p, int x, int y) {
        // Wis eerst de oude positie van deze specifieke persoon
        for (int i = 0; i < hoogte; i++) {
            for (int j = 0; j < breedte; j++) {
                if (positieGrid[i][j] == p) {
                    positieGrid[i][j] = null;
                }
            }
        }

        // Plaats de persoon op de nieuwe grid-positie (binnen de grenzen)
        if (x >= 0 && x < breedte && y >= 0 && y < hoogte) {
            positieGrid[y][x] = p;
        }
    }

    /**
     * Geeft terug welke persoon er op een specifieke grid-locatie staat.
     */
    public Persoon getPersoonOp(int x, int y) {
        if (x >= 0 && x < breedte && y >= 0 && y < hoogte) {
            return positieGrid[y][x];
        }
        return null;
    }

    public String[][] getGrid() { return grid; }
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