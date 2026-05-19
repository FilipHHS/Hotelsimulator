package model;

import hotelevents.HotelEventType;
import java.awt.Color;

public class Schoonmaker extends Persoon {
    private static final double SPEED = 0.15;
    private static final double LIFT_X = 1.5;
    private static final int SCHOONMAAK_DUUR = 100;

    private Hotel hotel;
    private Lift lift;
    private EventBusImpl eventBus;  // Event system
    private Kamer huidigKamer;
    private int schoonmaakTimer = 0;
    private boolean inLift = false;
    private int doelVerdieping;
    private int maxY = 6;

    private enum State { VRIJ, NAAR_DOEL, SCHOONMAKEN, IN_LIFT, EVACUATIE }
    private State state = State.VRIJ;

    public Schoonmaker(String naam, int startX, int startY) {
        super(naam, "Schoonmaker");

        // Forceer startpositie naar de opslag
        this.x = 7.5;
        this.y = 5.5;
        this.destX = 7.5;
        this.destY = 5.5;

        System.out.println("[Schoonmaker] " + naam + " is in de Opslag (7,5)");
    }

    @Override
    public void onTick() {
        updateActiviteitLabel();

        // 1. Brandalarm activeren
        if (fireAlarmActive && !evacuatieBegonnen) {
            startEvacuatie();
        }

        // 2. Brandalarm is weer voorbij: ga terug naar de opslag
        if (!fireAlarmActive && evacuatieBegonnen) {
            if (state == State.EVACUATIE) {
                evacuatieBegonnen = false;
                state = State.VRIJ;
                setHuidigeActiviteit("⏳ Idle");
                x = 7.5;
                y = 5.5;
            }
        }

        // 3. Liftbeweging afhandelen
        if (inLift) {
            handleLiftMovement();
            return;
        }

        // 4. Evacuatiebeweging afhandelen
        if (state == State.EVACUATIE) {
            handleEvacuatie();
            return;
        }

        // 5. Normaal gedrag: Zoek naar werk (vieze kamers)
        Kamer viezeKamer = zoekViezeKamer();

        // Geen vieze kamers? Loop rustig terug naar de opslag
        if (viezeKamer == null) {
            if (state == State.NAAR_DOEL) {
                state = State.VRIJ;
                huidigKamer = null;
            }

            if (Math.abs(x - 7.5) > 0.1) {
                x += (x < 7.5) ? SPEED : -SPEED;
            } else if (Math.abs(y - 5.5) > 0.1) {
                y += (y < 5.5) ? SPEED : -SPEED;
            } else {
                state = State.VRIJ;
            }
            return;
        }

        // Wel een vieze kamer gevonden! Bepaal het doel
        double targetX = viezeKamer.getArea().getX() + 0.5;
        double targetY = viezeKamer.getArea().getY() + 0.5;

        if (state == State.SCHOONMAKEN) {
            werkAanKamer();
        } else {
            beweeg(targetX, targetY, viezeKamer);
        }
    }

    private void updateActiviteitLabel() {
        if (inLift) {
            setHuidigeActiviteit("🛗 In Lift");
            return;
        }

        switch (state) {
            case VRIJ -> setHuidigeActiviteit("⏳ Idle");
            case NAAR_DOEL -> setHuidigeActiviteit("🚶 Naar Kamer");
            case SCHOONMAKEN -> setHuidigeActiviteit("🧹 Schoonmaken");
            case IN_LIFT -> setHuidigeActiviteit("🛗 In Lift");
            case EVACUATIE -> setHuidigeActiviteit("🔥 EVACUATIE!");
            default -> setHuidigeActiviteit("");
        }
    }

    private void beweeg(double tx, double ty, Kamer doelKamer) {
        // Controleer of de kamer in de tussentijd niet al door iemand anders is opgepakt
        if (doelKamer.getStatus() != Kamer.KamerStatus.SCHOONMAKEN) {
            state = State.VRIJ;
            huidigKamer = null;
            return;
        }

        // Verdieping check: Moeten we een lift pakken?
        if ((int)y != (int)ty) {
            if (Math.abs(x - LIFT_X) > 0.1) {
                x += (x < LIFT_X) ? SPEED : -SPEED; // Loop naar de liftschacht
            } else {
                stapInLift((int)ty);
            }
        } else {
            // Op de juiste verdieping: Loop horizontaal naar de kamer
            if (Math.abs(x - tx) > 0.1) {
                x += (x < tx) ? SPEED : -SPEED;
            } else {
                // Gearriveerd bij de kamer! Start met schoonmaken
                if (doelKamer.getStatus() == Kamer.KamerStatus.SCHOONMAKEN) {
                    this.huidigKamer = doelKamer;
                    this.state = State.SCHOONMAKEN;
                    this.schoonmaakTimer = SCHOONMAAK_DUUR;

                    // Trigger CLEANING_EMERGENCY event
                    if (eventBus != null) {
                        eventBus.triggerHotelEvent(HotelEventType.CLEANING_EMERGENCY,
                            getNaam().hashCode(), doelKamer.getKamernummer());
                    }
                } else {
                    this.state = State.VRIJ;
                }
            }
        }
    }

    private void werkAanKamer() {
        schoonmaakTimer--;
        if (schoonmaakTimer <= 0) {
            huidigKamer.setStatus(Kamer.KamerStatus.VRIJ);
            System.out.println("[Schoonmaker] Kamer " + huidigKamer.getKamernummer() + " is klaar.");
            huidigKamer = null;
            state = State.VRIJ;
        }
    }

    private void stapInLift(int verdieping) {
        if (lift != null && Math.abs(lift.getY() - y) < 0.3 && lift.isIdle()) {
            if (lift.voegGastToe(this, verdieping)) {
                this.inLift = true;
                this.doelVerdieping = verdieping;
                this.state = State.IN_LIFT;
            }
        }
    }

    private void handleLiftMovement() {
        this.x = lift.getX();
        this.y = lift.getY();

        // Stap uit als de lift stilstaat op de juiste verdieping
        if ((int)lift.getY() == doelVerdieping && lift.isIdle()) {
            lift.verwijderGast(this);
            this.inLift = false;
            this.y = doelVerdieping + 0.5;

            // Check of de kamer nog steeds vies is, anders terug naar opslag
            if (zoekViezeKamer() == null) {
                this.state = State.VRIJ;
            } else {
                this.state = State.NAAR_DOEL;
            }
        }
    }

    private Kamer zoekViezeKamer() {
        if (hotel == null) return null;
        for (Kamer k : hotel.getKamers()) {
            if (k.getStatus() == Kamer.KamerStatus.SCHOONMAKEN) {
                return k;
            }
        }
        return null;
    }

    private void startEvacuatie() {
        evacuatieBegonnen = true;

        // Verlaat de lift direct bij brandalarm
        if (inLift && lift != null) {
            lift.verwijderGast(this);
            inLift = false;
        }

        huidigKamer = null;
        state = State.EVACUATIE;
        System.out.println("[FireAlarm] 🚨 " + getNaam() + " begint evacuatie naar lobby!");
    }

    private void handleEvacuatie() {
        double lobbyX = 1.5;
        int lobbyY = maxY - 1;

        // Als we nog niet op de lobby-verdieping zijn, loop naar de trappen (x = 8.5)
        if ((int)y != lobbyY) {
            double stairX = 8.5;
            if (Math.abs(x - stairX) > SPEED) {
                x += (x < stairX) ? SPEED : -SPEED;
            } else {
                // Neem de trap omhoog of omlaag richting de lobby
                y += (y < lobbyY) ? SPEED : -SPEED;
            }
        } else {
            // Op de lobby-verdieping: Loop naar de uitgang (lobbyX)
            double dx = lobbyX - x;
            if (Math.abs(dx) < SPEED) {
                x = -1.5; // Loop naar buiten het gebouw
                state = State.VRIJ;
                setHuidigeActiviteit("👋 Verlaten gebouw");
            } else {
                x += (dx > 0) ? SPEED : -SPEED;
            }
        }
    }

    // --- SETTERS EN INITIALISATIE ---
    public void setHotel(Hotel h) { this.hotel = h; }
    public void setLift(Lift l) { this.lift = l; }
    public void setEventBus(EventBusImpl eventBus) { this.eventBus = eventBus; }
    public void setGridBounds(int mx, int my) { this.maxY = my; }

    public Color getKleur() { return Color.GRAY; }
}