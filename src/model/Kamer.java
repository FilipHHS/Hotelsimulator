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

    public Kamer(int kamernummer, String type) {
        this.kamernummer = kamernummer;
        this.type = type;
        this.status = KamerStatus.VRIJ;
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

}
