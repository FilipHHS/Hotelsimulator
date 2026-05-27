package model.strategy;

import model.Gast;
import model.Persoon;
import java.util.Random;

/**
 * Concrete strategy: normaal gedrag van een Gast.
 * Deze klasse bevat het algoritme voor wandelen, lift/trap, kamers en faciliteiten.
 *
 * Strategy Pattern rol: dit is een concrete strategy.
 * Persoon roept alleen beweeg(...) aan; deze class bepaalt wat een Gast normaal doet.
 */
public class GastNormalStrategy implements IMovementStrategy {
    private static final Random RANDOM = new Random();

    @Override
    public void beweeg(Persoon persoon) {
        // Deze strategy is alleen bedoeld voor Gast-objecten.
        if (!(persoon instanceof Gast)) return;
        Gast gast = (Gast) persoon;

        if (gast.isInLift()) {
            volgLift(gast);
            return;
        }

        // De state van Gast bepaalt welk stuk normaal gastgedrag deze tick draait.
        switch (gast.getGastState()) {
            case WANDELEN -> wandelVrij(gast);
            case NAAR_LIFT_WACHTEN -> gaNaarVervoerPunt(gast);
            case WACHTEN_OP_VERVOER -> wachtOpVervoer(gast);
            case GAAT_NAAR_FACILITEIT -> gaNaarFaciliteit(gast);
            case IN_FACILITEIT -> zitInFaciliteit(gast);
            case GAAT_NAAR_KAMER -> gaNaarKamer(gast);
            case GAAT_NAAR_LOBBY -> gaNaarLobby(gast);
            case VERLAAT_HOTEL -> verlaatHotel(gast);
            case EVACUATIE -> { /* Wordt afgehandeld door EvacuationMovement */ }
            case IN_LIFT -> { /* Wordt bovenaan afgevangen */ }
        }
    }

    private void volgLift(Gast gast) {
        if (gast.getLift() != null) {
            gast.setX(gast.getLift().getX());
            gast.setY(gast.getLift().getY());

            if ((int)gast.getLift().getY() == gast.getDoelVerdieping() && gast.getLift().isIdle()) {
                gast.getLift().verwijderGast(gast);
                gast.setInLift(false);
                gast.setY(gast.getDoelVerdieping() + 0.5);
                gast.setGastState(gast.getStateNaVervoer());
                gast.resetStateNaVervoer();
            }
        }
    }

    private void wandelVrij(Gast gast) {
        if (gast.getRoomStayTimer() > 0) {
            gast.setRoomStayTimer(gast.getRoomStayTimer() - 1);
            return;
        }

        if (gast.getStapsInRichting() >= gast.getMaxStapsRichting()
                || Math.abs(gast.getDestX() - gast.getX()) < 0.1) {
            double nieuwDoelX = 0.5 + RANDOM.nextDouble() * Math.max(1.0, gast.getMaxX() - 2.0);
            gast.setDestX(nieuwDoelX);
            gast.setDestY(gast.getY());
            gast.setStapsInRichting(0);
            gast.setMaxStapsRichting(RANDOM.nextInt(3) + 2);
        }

        // De strategy kiest het doel; Persoon.beweegNaar(...) doet de echte x/y-verplaatsing.
        gast.beweegNaar(gast.getDestX(), gast.getDestY(), gast.getActueleSnelheid());
        gast.setStapsInRichting(gast.getStapsInRichting() + 1);

        if (RANDOM.nextDouble() < 0.02) gast.wiltVerdiepingWisselen();
    }

    private void gaNaarVervoerPunt(Gast gast) {
        double doelX = gast.isUsesTrap() ? Gast.TRAP_X : Gast.LIFT_WAIT_X;
        if (gast.beweegNaar(doelX, gast.getY(), gast.getActueleSnelheid())) {
            gast.setGastState(Gast.State.WACHTEN_OP_VERVOER);
        }
    }

    private void wachtOpVervoer(Gast gast) {
        if (gast.isUsesTrap()) {
            double doelY = gast.getDoelVerdieping() + 0.5;
            if (gast.beweegNaar(gast.getX(), doelY, gast.getActueleSnelheid())) {
                gast.setGastState(gast.getStateNaVervoer());
                gast.resetStateNaVervoer();
            }
        } else if (gast.getLift() != null && Math.abs(gast.getLift().getY() - gast.getY()) < 1.0 && gast.getLift().isIdle()) {
            if (gast.getLift().voegGastToe(gast, gast.getDoelVerdieping())) {
                gast.setInLift(true);
                gast.setGastState(Gast.State.IN_LIFT);
            }
        }
    }

    private void gaNaarFaciliteit(Gast gast) {
        int doelVerdieping = (int)gast.getFaciliteitY();
        if ((int)gast.getY() != doelVerdieping) {
            gast.gaNaarVerdieping(doelVerdieping, Gast.State.GAAT_NAAR_FACILITEIT);
            return;
        }

        if (gast.beweegNaar(gast.getFaciliteitX(), gast.getY(), gast.getActueleSnelheid())) {
            gast.setGastState(Gast.State.IN_FACILITEIT);
            gast.setFaciliteitsBezoekDuur(20 + RANDOM.nextInt(30));
        }
    }

    private void zitInFaciliteit(Gast gast) {
        gast.setFaciliteitsBezoekDuur(gast.getFaciliteitsBezoekDuur() - 1);
        if (gast.getFaciliteitsBezoekDuur() <= 0) {
            gast.setGastState(Gast.State.WANDELEN);
            gast.setHuidigerFaciliteitType(null);
        }
    }

    private void gaNaarKamer(Gast gast) {
        if ((int)gast.getY() != gast.getDoelVerdieping()) {
            gast.gaNaarVerdieping(gast.getDoelVerdieping(), Gast.State.GAAT_NAAR_KAMER);
            return;
        }

        if (gast.beweegNaar(gast.getDestX(), gast.getY(), gast.getActueleSnelheid())) {
            gast.setGastState(Gast.State.WANDELEN);
            gast.setRoomStayTimer(300 + RANDOM.nextInt(300));
        }
    }

    private void gaNaarLobby(Gast gast) {
        int lobbyY = gast.getMaxY() - 1;
        if ((int)gast.getY() != lobbyY) {
            gast.gaNaarVerdieping(lobbyY, Gast.State.GAAT_NAAR_LOBBY);
            return;
        }

        if (gast.beweegNaar(1.5, gast.getY(), gast.getActueleSnelheid())) {
            gast.setGastState(Gast.State.VERLAAT_HOTEL);
        }
    }

    private void verlaatHotel(Gast gast) {
        gast.beweegNaar(-1.5, gast.getY(), gast.getActueleSnelheid());
    }
}
