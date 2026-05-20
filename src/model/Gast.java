package model;

import hotelevents.HotelEventType;
import java.awt.Color;
import java.util.Random;

public class Gast extends Persoon {
    private static final Random RANDOM = new Random();
    private static final double SPEED = 0.5;
    private static final double GODZILLA_SPEED_MULTIPLIER = 3.0;
    private static final int GODZILLA_DURATION_TICKS = 250;
    private static final double LIFT_WAIT_X = 1.5;
    private static final double TRAP_X = 8.5;
    private static final int LOUNGE_CHILL_TICKS = 20;

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
    private int loungeStayTicks = 0;
    private double vorigeX = Double.NaN;
    private double vorigeY = Double.NaN;
    private int godzillaTicksRemaining = 0;

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
        // 1. Brandalarm activeren
        if (fireAlarmActive && !evacuatieBegonnen) {
            startEvacuatie();
        }

        if (godzillaTicksRemaining > 0) {
            godzillaTicksRemaining--;
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

        updateLoungeStayTicks();
        updateActiviteitLabel();
    }

    private void updateActiviteitLabel() {
        if (inLift) {
            if (lift != null && lift.isIdle()) {
                setHuidigeActiviteit("⏳ Wachten in lift");
            } else {
                setHuidigeActiviteit("🛗 In lift");
            }
            return;
        }

        if (isGodzillaActive() && state != State.EVACUATIE && state != State.VERLAAT_HOTEL) {
            setHuidigeActiviteit("GODZILLA!");
            return;
        }

        switch (state) {
            case WANDELEN -> setHuidigeActiviteit(loungeStayTicks >= LOUNGE_CHILL_TICKS ? "🛋️ Chill" : "🚶 Wandel");
            case NAAR_LIFT_WACHTEN -> setHuidigeActiviteit("⏳ Wacht Lift");
            case WACHTEN_OP_VERVOER -> setHuidigeActiviteit("⏳ Wachten op lift");
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

    private void updateLoungeStayTicks() {
        boolean inLounge = isInAreaType("Lounge");
        boolean staatStil = !Double.isNaN(vorigeX)
                && Math.abs(x - vorigeX) < 0.01
                && Math.abs(y - vorigeY) < 0.01;

        if (state == State.WANDELEN && inLounge && staatStil) {
            loungeStayTicks++;
        } else if (!inLounge || !staatStil) {
            loungeStayTicks = 0;
        }

        vorigeX = x;
        vorigeY = y;
    }

    private boolean isInAreaType(String areaType) {
        if (hotel == null) return false;

        for (Area area : hotel.getAreas()) {
            if (!areaType.equals(area.getAreaType())) continue;

            double minX = area.getX() - 1;
            double maxX = minX + area.getBreedte();
            double minY = area.getY() - 1;
            double maxY = minY + area.getHoogte();

            if (x >= minX && x < maxX && y >= minY && y < maxY) {
                return true;
            }
        }
        return false;
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

    private double getAreaCenterX(Area area) {
        return area.getX() - 1 + area.getBreedte() / 2.0;
    }

    private double getAreaCenterY(Area area) {
        return area.getY() - 1 + area.getHoogte() / 2.0;
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

        double speed = getSpeed();
        double dx = destX - x;
        if (Math.abs(dx) < speed) x = destX;
        else x += (dx > 0 ? speed : -speed);

        stapsInRichting++;
        if (RANDOM.nextDouble() < 0.02) wiltVerdiepingWisselen();
    }

    private void beweegNaarLiftTrap() {
        double speed = getSpeed();
        double dx = destX - x;
        if (Math.abs(dx) < speed) {
            x = destX;
            this.state = State.WACHTEN_OP_VERVOER;
        } else {
            x += (dx > 0) ? speed : -speed;
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
        double speed = getSpeed();
        double dx = faciliteitX - x;
        if (Math.abs(dx) < speed) {
            x = faciliteitX;
            this.state = State.IN_FACILITEIT;
            this.faciliteitsBezoekDuur = 20 + RANDOM.nextInt(30);
        } else {
            x += (dx > 0) ? speed : -speed;
        }
    }

    private void beweegNaarKamer() {
        if ((int)y != doelVerdieping) {
            wiltVerdiepingWisselen();
        } else {
            double speed = getSpeed();
            double dx = destX - x;
            if (Math.abs(dx) < speed) {
                x = destX;
                this.y = doelVerdieping + 0.5;
                this.state = State.WANDELEN;
                this.roomStayTimer = 300 + RANDOM.nextInt(300); // Blijf 300-600 ticks in kamer
            } else {
                x += (dx > 0) ? speed : -speed;
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
            double speed = getSpeed();
            double lobbyX = 1.5;
            double dx = lobbyX - x;
            if (Math.abs(dx) < speed) {
                x = lobbyX;
                this.state = State.VERLAAT_HOTEL;
            } else {
                x += (dx > 0) ? speed : -speed;
            }
        }
    }

    private void beweegNaarExit() {
        double speed = getSpeed();
        double exitX = -1.5;
        double dx = exitX - x;
        if (Math.abs(dx) < speed) {
            x = exitX;
            setHuidigeActiviteit("👋 Buiten hotel");
        } else {
            x -= speed;
        }
    }

    public boolean checkinKamer(Kamer k) {
        this.huidigKamer = k;
        k.setStatus(Kamer.KamerStatus.BEZET);
        this.destX = getAreaCenterX(k.getArea());
        this.doelVerdieping = k.getArea().getY() - 1;
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
            double speed = getSpeed();
            if (Math.abs(x - stairX) > speed) {
                x += (x < stairX) ? speed : -speed;
            } else {
                // Beweeg verticaal via de trappen naar de lobby-verdieping
                y += (y < lobbyY) ? speed : -speed;
            }
        } else {
            // Stap 2: Eenmaal in de lobby, loop door naar de uitgang
            double speed = getSpeed();
            double dx = lobbyX - x;
            if (Math.abs(dx) < speed) {
                x = lobbyX;
                this.state = State.VERLAAT_HOTEL;
                setHuidigeActiviteit("👋 Verlaat Hotel");
            } else {
                x += (dx > 0) ? speed : -speed;
            }
        }
    }

    public void activeerGodzilla() {
        this.godzillaTicksRemaining = GODZILLA_DURATION_TICKS;
        this.roomStayTimer = 0;
        this.faciliteitsBezoekDuur = 0;
        if (state == State.IN_FACILITEIT) {
            state = State.WANDELEN;
            huidigerFaciliteitType = null;
        }
        setHuidigeActiviteit("GODZILLA!");
    }

    public boolean isGodzillaActive() {
        return godzillaTicksRemaining > 0;
    }

    private double getSpeed() {
        return isGodzillaActive() ? SPEED * GODZILLA_SPEED_MULTIPLIER : SPEED;
    }

    // --- SETTERS & GETTERS ---
    public void setGridBounds(int maxX, int maxY) { this.maxX = maxX; this.maxY = maxY; }
    public void setLift(Lift lift) { this.lift = lift; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }
    public void setEventBus(EventBusImpl eventBus) { this.eventBus = eventBus; }

    public Color getKleur() { return kleur; }
    public Kamer getHuidigKamer() { return huidigKamer; }
}
