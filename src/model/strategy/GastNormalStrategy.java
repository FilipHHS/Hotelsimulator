package model.strategy;

import model.personen.Gast;
import model.personen.Persoon;
import java.util.Random;

public class GastNormalStrategy implements IMovementStrategy {
    private static final Random RANDOM = new Random();
    private static final double LOBBY_X = 1.5;
    private static final double RAND_MARGE = 0.5;
    private static final double RECHTER_MARGE = 1.5;

    @Override
    public void beweeg(Persoon persoon) {
        if (!(persoon instanceof Gast)) return; // SS1.1: type-check — deze strategy werkt alleen op gasten
        Gast gast = (Gast) persoon;

        if (gast.isInLift()) { // SS1.2: in de lift? → liftlogica en klaar voor deze tick
            handleLiftLogic(gast);
            return;
        }

        switch (gast.getGastState()) { // SS1.3: state machine — kies het deelalgoritme op basis van de state
            case WANDELEN -> randomWalk(gast); // SS1.4: random links/rechts wandelen (40/40/20), 2% kans op verdiepingswissel
            case NAAR_LIFT_WACHTEN -> beweegNaarLiftTrap(gast); // SS1.5: loop naar TRAP_X of LIFT_WAIT_X → dan WACHTEN_OP_VERVOER
            case WACHTEN_OP_VERVOER -> wachtOpVervoer(gast); // SS1.6: trap → meteen verdieping wisselen; lift → instappen als idle
            case GAAT_NAAR_FACILITEIT -> beweegNaarFaciliteit(gast); // SS1.7: loop naar faciliteitX → IN_FACILITEIT + duur 20-50 ticks
            case IN_FACILITEIT -> zitInFaciliteit(gast); // SS1.8: duur aftellen → daarna naar kamer of WANDELEN
            case GAAT_NAAR_KAMER -> beweegNaarKamer(gast); // SS1.9: eerst juiste verdieping, dan naar destX → WANDELEN + roomStayTimer
            case GAAT_NAAR_LOBBY -> beweegNaarLobby(gast); // SS1.10: naar lobbyverdieping → naar LOBBY_X → VERLAAT_HOTEL
            case VERLAAT_HOTEL -> beweegNaarExit(gast); // SS1.11: elke tick x - speed → loopt links het scherm uit
            case EVACUATIE -> { /* SS1.12: leeg — wordt afgehandeld door EvacuationMovement */ }
            case BUITEN -> { /* SS1.13: leeg — veilig buiten, blijf staan */ }
            case IN_LIFT -> { /* SS1.14: leeg — al afgevangen bij SS1.2 */ }
        }
    }

    private void handleLiftLogic(Gast gast) {
        if (gast.getLift() != null) {
            gast.setX(gast.getLift().getX());
            gast.setY(gast.getLift().getY());

            if ((int)gast.getLift().getY() == gast.getDoelVerdieping() && gast.getLift().isIdle()) {
                gast.getLift().verwijderGast(gast);
                gast.setInLift(false);
                gast.setY(gast.getDoelVerdieping() + 0.5);
                gast.hervatStateNaVerdiepingWissel();
            }
        }
    }

    private void randomWalk(Gast gast) {
        if (gast.getRoomStayTimer() > 0) {
            gast.setRoomStayTimer(gast.getRoomStayTimer() - 1);
            return;
        }
        if (gast.getHuidigKamer() != null) {
            return;
        }

        double speed = gast.getActueleSnelheid();
        boolean doelBereikt = Math.abs(gast.getDestX() - gast.getX()) <= speed;

        if (doelBereikt || gast.getStapsInRichting() >= gast.getMaxStapsRichting()) {
            int keuze = RANDOM.nextInt(10);
            double nieuwDoel = gast.getX();
            if (keuze < 4) nieuwDoel = gast.getX() - 3;
            else if (keuze < 8) nieuwDoel = gast.getX() + 3;

            gast.setDestX(beperkBinnenHotel(nieuwDoel, gast));
            gast.setStapsInRichting(0);
            gast.setMaxStapsRichting(RANDOM.nextInt(10) + 8);
        }

        beweegNaarX(gast, gast.getDestX());
        gast.setStapsInRichting(gast.getStapsInRichting() + 1);

        if (RANDOM.nextDouble() < 0.02) gast.wiltVerdiepingWisselen();
    }

    private void beweegNaarLiftTrap(Gast gast) {
        double speed = gast.getActueleSnelheid();
        // Bepaal richting naar het lift/trap wachtpunt (TRAP_X of LIFT_WAIT_X)
        double doeltargetX = gast.isUsesTrap() ? Gast.TRAP_X : Gast.LIFT_WAIT_X;
        double dx = doeltargetX - gast.getX();

        if (Math.abs(dx) < speed) {
            gast.setX(doeltargetX);
            gast.setGastState(Gast.State.WACHTEN_OP_VERVOER);
        } else {
            gast.setX(gast.getX() + ((dx > 0) ? speed : -speed));
        }
    }

    private void wachtOpVervoer(Gast gast) {
        if (gast.isUsesTrap()) {
            int direction = bepaalTrapRichting(gast);
            gast.setY(((int)gast.getY() + direction) + 0.5);
            gast.hervatStateNaVerdiepingWissel();
        } else if (gast.getLift() != null && Math.abs(gast.getLift().getY() - gast.getY()) < 1.0 && gast.getLift().isIdle()) {
            int nieuwDoel;
            if (gast.getStateNaVerdiepingWissel() != Gast.State.WANDELEN) {
                nieuwDoel = gast.getDoelVerdieping();
            } else {
                nieuwDoel = RANDOM.nextInt(gast.getMaxY());
                if (nieuwDoel == (int)gast.getY()) {
                    nieuwDoel = (nieuwDoel + 1) % gast.getMaxY();
                }
            }
            gast.setDoelVerdieping(nieuwDoel);

            if (gast.getLift().voegGastToe(gast, gast.getDoelVerdieping())) {
                gast.setInLift(true);
                gast.setGastState(Gast.State.IN_LIFT);
            }
        }
    }

    private void beweegNaarFaciliteit(Gast gast) {
        double speed = gast.getActueleSnelheid();
        double dx = gast.getFaciliteitX() - gast.getX();

        if (Math.abs(dx) < speed) {
            gast.setX(gast.getFaciliteitX());
            gast.setGastState(Gast.State.IN_FACILITEIT);
            if (gast.getFaciliteitsBezoekDuur() <= 0) {
                gast.setFaciliteitsBezoekDuur(20 + RANDOM.nextInt(30));
            }
        } else {
            gast.setX(gast.getX() + ((dx > 0) ? speed : -speed));
        }
    }

    private void zitInFaciliteit(Gast gast) {
        gast.setFaciliteitsBezoekDuur(gast.getFaciliteitsBezoekDuur() - 1);
        if (gast.getFaciliteitsBezoekDuur() <= 0) {
            gast.setHuidigerFaciliteitType(null);
            if (gast.getHuidigKamer() != null) {
                gast.setDestX(getAreaCenterX(gast.getHuidigKamer().getArea()));
                gast.setDoelVerdieping(gast.getHuidigKamer().getArea().getY() - 1);
                gast.setGastState(Gast.State.GAAT_NAAR_KAMER);
            } else {
                gast.setGastState(Gast.State.WANDELEN);
            }
        }
    }

    private double getAreaCenterX(model.Area area) {
        return area.getX() - 1 + area.getBreedte() / 2.0;
    }

    private void beweegNaarKamer(Gast gast) {
        if ((int)gast.getY() != gast.getDoelVerdieping()) {
            gast.wiltVerdiepingWisselen();
        } else {
            double speed = gast.getActueleSnelheid();
            double kamerX = gast.getDestX(); // de bestemming die in checkinKamer is gezet
            double dx = kamerX - gast.getX(); // echte afstand tot de kamer

            if (Math.abs(dx) < speed) {
                gast.setX(kamerX); // snap naar de kamer
                gast.setGastState(Gast.State.WANDELEN);
                gast.setRoomStayTimer(300 + RANDOM.nextInt(300));
            } else {
                gast.setX(gast.getX() + (dx > 0 ? speed : -speed)); // beweeg richting kamer
            }
        }
    }

    private void beweegNaarLobby(Gast gast) {
        int lobbyY = gast.getMaxY() - 1;
        if ((int)gast.getY() != lobbyY) {
            gast.wiltVerdiepingWisselen();
        } else {
            double speed = gast.getActueleSnelheid();
            double dx = LOBBY_X - gast.getX();
            if (Math.abs(dx) < speed) {
                gast.setX(LOBBY_X);
                gast.setGastState(Gast.State.VERLAAT_HOTEL);
            } else {
                gast.setX(gast.getX() + ((dx > 0) ? speed : -speed));
            }
        }
    }

    private void beweegNaarExit(Gast gast) {
        double speed = gast.getActueleSnelheid();
        gast.setX(gast.getX() - speed);
    }

    private void beweegNaarX(Gast gast, double targetX) {
        double speed = gast.getActueleSnelheid();
        double dx = targetX - gast.getX();

        if (Math.abs(dx) <= speed) {
            gast.setX(targetX);
        } else {
            gast.setX(gast.getX() + Math.signum(dx) * speed);
        }
    }

    private double beperkBinnenHotel(double targetX, Gast gast) {
        return Math.max(RAND_MARGE, Math.min(gast.getMaxX() - RECHTER_MARGE, targetX));
    }

    private int bepaalTrapRichting(Gast gast) {
        if (gast.getStateNaVerdiepingWissel() != Gast.State.WANDELEN) {
            return gast.getDoelVerdieping() > (int) gast.getY() ? 1 : -1;
        }
        return (gast.getY() >= gast.getMaxY() - 1) ? -1 : (gast.getY() <= 1 ? 1 : (RANDOM.nextBoolean() ? 1 : -1));
    }
}
