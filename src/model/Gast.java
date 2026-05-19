package model;

import hotelevents.HotelEventType;
import java.awt.Color;
import java.util.Random;

public class Gast extends Persoon {
    private static final Random RANDOM = new Random();
    private static final double SPEED = 0.5;
    private static final double LIFT_WAIT_X = 1.5;
    private static final double TRAP_X = 8.5;

    private int maxX, maxY;
    private Hotel hotel;
    private Lift lift;
    private EventBusImpl eventBus;  // Event system
    private boolean inLift = false;
    private boolean usesTrap = false;
    private Kamer huidigKamer;
    private final Color kleur;

    private int doelVerdieping;
    private int roomStayTimer = 0;  // Tijd die de gast in de kamer doorbrengt
    private double faciliteitX, faciliteitY;
    private int faciliteitsBezoekDuur = 0;
    private String huidigerFaciliteitType = null;

    private enum State {
        WANDELEN, NAAR_LIFT_WACHTEN, WACHTEN_OP_VERVOER, IN_LIFT,
        GAAT_NAAR_FACILITEIT, IN_FACILITEIT, GAAT_NAAR_LOBBY,
        GAAT_NAAR_KAMER, VERLAAT_HOTEL, EVACUATIE
    }

    private State state = State.WANDELEN;
    private int stapsInRichting = 0;
    private int maxStapsRichting = 5;

    public Gast(String naam, int ignoreX, int ignoreY) {
        super(naam, "Gast");
        this.kleur = new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
    }

    @Override
    public void onTick() {
        updateActiviteitLabel();

        // 1. Brandalarm activeren
        if (fireAlarmActive && !evacuatieBegonnen) {
            startEvacuatie();
        }

        // 2. Brandalarm voorbij: reset naar de lobby en ga weer normaal wandelen
        if (!fireAlarmActive && evacuatieBegonnen) {
            if (state == State.EVACUATIE || state == State.VERLAAT_HOTEL) {
                evacuatieBegonnen = false;
                state = State.WANDELEN;
                setHuidigeActiviteit("🚶 Wandel");
                huidigKamer = null;
                x = 1.5;
                y = maxY - 1;
                destX = x;
            }
        }

        // 3. Bewegingslogica op basis van de huidige status
        if (inLift) {
            handleLiftLogic();
        } else {
            switch (state) {
                case WANDELEN -> randomWalk();
                case NAAR_LIFT_WACHTEN -> beweegNaarLiftTrap();
                case WACHTEN_OP_VERVOER -> wachtOpVervoer();
                case GAAT_NAAR_FACILITEIT -> beweegNaarFaciliteit();
                case IN_FACILITEIT -> zitInFaciliteit();
                case GAAT_NAAR_KAMER -> beweegNaarKamer();
                case GAAT_NAAR_LOBBY -> beweegNaarLobby();
                case VERLAAT_HOTEL -> beweegNaarExit();
                case EVACUATIE -> beweegNaarLobbyDirect();
            }
        }
    }

    private void updateActiviteitLabel() {
        if (inLift) {
            setHuidigeActiviteit("🛗 In Lift");
            return;
        }

        switch (state) {
            case WANDELEN -> setHuidigeActiviteit(huidigKamer != null ? "🛏️ Chill" : "🚶 Wandel");
            case NAAR_LIFT_WACHTEN -> setHuidigeActiviteit("⏳ Wacht Lift");
            case WACHTEN_OP_VERVOER -> setHuidigeActiviteit("⏳ Vervoer");
            case GAAT_NAAR_FACILITEIT -> setHuidigeActiviteit("🚶 > Faciliteit");
            case GAAT_NAAR_KAMER -> setHuidigeActiviteit("✓ Check-in");
            case GAAT_NAAR_LOBBY -> setHuidigeActiviteit("✗ Check-out");
            case VERLAAT_HOTEL -> setHuidigeActiviteit("👋 Vertrekt");
            case EVACUATIE -> setHuidigeActiviteit("🔥 EVACUATIE!");
            case IN_FACILITEIT -> {
                if (huidigerFaciliteitType == null) {
                    setHuidigeActiviteit("");
                } else if ("Restaurant".equals(huidigerFaciliteitType)) {
                    setHuidigeActiviteit("🍽️ Eet");
                } else if ("Fitness".equals(huidigerFaciliteitType)) {
                    setHuidigeActiviteit("💪 Sport");
                } else {
                    setHuidigeActiviteit("📍 " + huidigerFaciliteitType);
                }
            }
        }
    }

    private void handleLiftLogic() {
        if (lift != null) {
            this.x = lift.getX();
            this.y = lift.getY();

            // Stap uit de lift als we op de juiste verdieping zijn en de lift stilstaat
            if ((int)lift.getY() == doelVerdieping && lift.isIdle()) {
                lift.verwijderGast(this);
                this.inLift = false;
                this.y = doelVerdieping + 0.5;
                this.destX = this.x;
                this.state = State.WANDELEN;
            }
        }
    }

    private void randomWalk() {
        // Als de gast net in de kamer is, blijft hij daar voorlopig chillen
        if (roomStayTimer > 0) {
            roomStayTimer--;
            destX = x;
            return;
        }

        // Bepaal na een paar stappen een nieuwe willekeurige richting (binnen hotelgrenzen)
        if (stapsInRichting >= maxStapsRichting) {
            int keuze = RANDOM.nextInt(10);
            if (keuze < 4) destX = Math.max(0.5, x - 3);
            else if (keuze < 8) destX = Math.min(maxX - 1.5, x + 3);
            else destX = x;

            stapsInRichting = 0;
            maxStapsRichting = RANDOM.nextInt(3) + 2;
        }

        double dx = destX - x;
        if (Math.abs(dx) < SPEED) x = destX;
        else x += (dx > 0 ? SPEED : -SPEED);

        stapsInRichting++;
        if (RANDOM.nextDouble() < 0.02) wiltVerdiepingWisselen();
    }

    private void beweegNaarLiftTrap() {
        double dx = destX - x;
        if (Math.abs(dx) < SPEED) {
            x = destX;
            this.state = State.WACHTEN_OP_VERVOER;
        } else {
            x += (dx > 0) ? SPEED : -SPEED;
        }
    }

    private void wachtOpVervoer() {
        if (usesTrap) {
            // Neem direct de trap naar een willekeurige aangrenzende verdieping
            int direction = (y >= maxY - 1) ? -1 : (y <= 1 ? 1 : (RANDOM.nextBoolean() ? 1 : -1));
            this.y = ((int)y + direction) + 0.5;
            this.destX = this.x;
            this.state = State.WANDELEN;
        } else if (lift != null && Math.abs(lift.getY() - this.y) < 1.0 && lift.isIdle()) {
            // Kies een willekeurige andere verdieping dan waar we nu zijn
            this.doelVerdieping = RANDOM.nextInt(maxY);
            if (this.doelVerdieping == (int)this.y) {
                this.doelVerdieping = (this.doelVerdieping + 1) % maxY;
            }

            if (lift.voegGastToe(this, doelVerdieping)) {
                this.inLift = true;
                this.state = State.IN_LIFT;
            }
        }
    }

    private void wiltVerdiepingWisselen() {
        this.usesTrap = RANDOM.nextBoolean();
        this.destX = usesTrap ? TRAP_X : LIFT_WAIT_X;
        this.state = State.NAAR_LIFT_WACHTEN;
    }

    public void gaatNaarFaciliteit(String type, double fX, double fY) {
        this.huidigerFaciliteitType = type;
        this.faciliteitX = fX;
        this.faciliteitY = fY;

        // Trigger appropriate event based on facility type
        if (eventBus != null) {
            int guestId = getNaam().hashCode();
            switch (type.toLowerCase()) {
                case "restaurant":
                case "eten":
                case "food":
                    eventBus.triggerHotelEvent(HotelEventType.NEED_FOOD, guestId, 0);
                    break;
                case "cinema":
                case "film":
                    eventBus.triggerHotelEvent(HotelEventType.GOTO_CINEMA, guestId, 0);
                    break;
                case "fitness":
                case "gym":
                    eventBus.triggerHotelEvent(HotelEventType.GOTO_FITNESS, guestId, 0);
                    break;
            }
        }

        if ((int)fY != (int)y) wiltVerdiepingWisselen();
        else this.state = State.GAAT_NAAR_FACILITEIT;
    }

    private void beweegNaarFaciliteit() {
        double dx = faciliteitX - x;
        if (Math.abs(dx) < SPEED) {
            x = faciliteitX;
            this.state = State.IN_FACILITEIT;
            this.faciliteitsBezoekDuur = 20 + RANDOM.nextInt(30);
        } else {
            x += (dx > 0) ? SPEED : -SPEED;
        }
    }

    private void beweegNaarKamer() {
        if ((int)y != doelVerdieping) {
            wiltVerdiepingWisselen();
        } else {
            double dx = destX - x;
            if (Math.abs(dx) < SPEED) {
                x = destX;
                this.y = doelVerdieping + 0.5;
                this.state = State.WANDELEN;
                this.roomStayTimer = 100 + RANDOM.nextInt(200); // Blijf 100-300 ticks in kamer
            } else {
                x += (dx > 0) ? SPEED : -SPEED;
            }
        }
    }

    private void zitInFaciliteit() {
        faciliteitsBezoekDuur--;
        if (faciliteitsBezoekDuur <= 0) {
            this.state = State.WANDELEN;
            this.huidigerFaciliteitType = null;
        }
    }

    private void beweegNaarLobby() {
        int lobbyY = maxY - 1;
        if ((int)y != lobbyY) {
            wiltVerdiepingWisselen();
        } else {
            double lobbyX = 1.5;
            double dx = lobbyX - x;
            if (Math.abs(dx) < SPEED) {
                x = lobbyX;
                this.state = State.VERLAAT_HOTEL;
            } else {
                x += (dx > 0) ? SPEED : -SPEED;
            }
        }
    }

    private void beweegNaarExit() {
        double exitX = -1.5;
        double dx = exitX - x;
        if (Math.abs(dx) < SPEED) {
            x = exitX;
            setHuidigeActiviteit("👋 Buiten hotel");
        } else {
            x -= SPEED;
        }
    }

    public boolean checkinKamer(Kamer k) {
        this.huidigKamer = k;
        k.setStatus(Kamer.KamerStatus.BEZET);
        this.destX = k.getArea().getX() + 0.5;
        this.doelVerdieping = k.getArea().getY();
        this.state = State.GAAT_NAAR_KAMER;

        // Trigger CHECK_IN event
        if (eventBus != null) {
            int guestId = getNaam().hashCode();
            eventBus.triggerHotelEvent(HotelEventType.CHECK_IN, guestId, k.getKamernummer());
        }

        return true;
    }

    public void checkoutKamer() {
        if (huidigKamer != null) {
            // Trigger CHECK_OUT event
            if (eventBus != null) {
                int guestId = getNaam().hashCode();
                eventBus.triggerHotelEvent(HotelEventType.CHECK_OUT, guestId, huidigKamer.getKamernummer());
            }

            huidigKamer.setStatus(Kamer.KamerStatus.SCHOONMAKEN);
            huidigKamer = null;
            this.state = State.GAAT_NAAR_LOBBY;
        }
    }

    private void startEvacuatie() {
        evacuatieBegonnen = true;

        // Verlaat de lift direct bij brandalarm
        if (inLift && lift != null) {
            lift.verwijderGast(this);
            inLift = false;
            this.x = lift.getX();
            this.y = (int)lift.getY() + 0.5;
        }

        huidigKamer = null;
        huidigerFaciliteitType = null;
        faciliteitsBezoekDuur = 0;
        roomStayTimer = 0;

        this.state = State.EVACUATIE;
        this.usesTrap = true; // Bij brand ALTIJD de trap nemen
        System.out.println("[FireAlarm] 🚨 " + getNaam() + " begint evacuatie naar lobby!");
    }

    private void beweegNaarLobbyDirect() {
        double lobbyX = 1.5;
        double stairX = 8.5;
        int lobbyY = maxY - 1;

        // Stap 1: Loop naar de trap toe als we nog op een verdieping zitten
        if ((int)y != lobbyY) {
            if (Math.abs(x - stairX) > SPEED) {
                x += (x < stairX) ? SPEED : -SPEED;
            } else {
                // Beweeg verticaal via de trappen naar de lobby-verdieping
                y += (y < lobbyY) ? SPEED : -SPEED;
            }
        } else {
            // Stap 2: Eenmaal in de lobby, loop door naar de uitgang
            double dx = lobbyX - x;
            if (Math.abs(dx) < SPEED) {
                x = lobbyX;
                this.state = State.VERLAAT_HOTEL;
                setHuidigeActiviteit("👋 Verlaat Hotel");
            } else {
                x += (dx > 0) ? SPEED : -SPEED;
            }
        }
    }

    // --- SETTERS & GETTERS ---
    public void setGridBounds(int maxX, int maxY) { this.maxX = maxX; this.maxY = maxY; }
    public void setLift(Lift lift) { this.lift = lift; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }
    public void setEventBus(EventBusImpl eventBus) { this.eventBus = eventBus; }

    public Color getKleur() { return kleur; }
    public Kamer getHuidigKamer() { return huidigKamer; }
}