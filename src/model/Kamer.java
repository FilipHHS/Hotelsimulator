package model;

public class Kamer {

    public enum KamerStatus {
        VRIJ,
        BEZET,
        SCHOONMAKEN
    }
    private int kamernummer; // 101, 102
    private String type; // "Luxe", "Standaard"
    private KamerStatus status; // Vrij, Bezet, Schoonmaken
    private Area area; // Referentie naar de ruimte van deze kamer

    public Kamer(int kamernummer, String type) {
        this.kamernummer = kamernummer;
        this.type = type;
        this.status = KamerStatus.VRIJ;
        this.area = null;
    }

    public int getKamernummer() {
        return kamernummer;
    }

    public String getType() {
        return type;
    }

    public KamerStatus getStatus() {
        return status;
    }
    public void setStatus(KamerStatus status) {
        this.status = status;
        System.out.println("Kamer " + kamernummer + " is nu " + status);
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }
}
