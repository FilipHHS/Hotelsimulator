package model;

public class Hotel {
    private String[][] grid;
    private int breedte;
    private int hoogte;

    public Hotel(String[][] grid, int breedte, int hoogte) {
        this.grid = grid;
        this.breedte = breedte;
        this.hoogte = hoogte;
    }

    public String[][] getGrid() { return grid; }
    public int getBreedte() { return breedte; }
    public int getHoogte() { return hoogte; }
}