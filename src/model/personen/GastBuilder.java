package model.personen;

import model.*;

import java.util.Objects;

public class GastBuilder {
    private String naam;
    private Hotel hotel;
    private Lift lift;
    private IEventBus eventBus;
    private double startX;
    private double startY;
    private int maxX;
    private int maxY;

    public GastBuilder naam(String naam)            { this.naam = naam;     return this; }
    public GastBuilder hotel(Hotel hotel)           { this.hotel = hotel;   return this; }
    public GastBuilder lift(Lift lift)              { this.lift = lift;     return this; }
    public GastBuilder eventBus(IEventBus eventBus) { this.eventBus = eventBus; return this; }
    public GastBuilder startPos(double x, double y) { this.startX = x; this.startY = y; return this; }
    public GastBuilder gridBounds(int w, int h)     { this.maxX = w; this.maxY = h; return this; }

    public Gast build() {
        Objects.requireNonNull(naam,  "naam is required");
        Objects.requireNonNull(hotel, "hotel is required");
        Gast g = new Gast(naam, -1, 0);
        g.setHotel(hotel);
        g.setLift(lift);
        g.setEventBus(eventBus);
        g.setGridBounds(maxX, maxY);
        g.setStartPositie(startX, startY);
        return g;
    }
}