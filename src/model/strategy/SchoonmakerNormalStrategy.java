package model.strategy;

import hotelevents.HotelEventType;
import model.Kamer;
import model.Persoon;
import model.Schoonmaker;
import model.Area;

/**
 * Concrete strategy: normaal gedrag van een Schoonmaker.
 * De schoonmaker zoekt vieze kamers, reist erheen en maakt ze schoon.
 *
 * Strategy Pattern rol: dit is een concrete strategy voor Schoonmaker.
 * De schoonmaaklogica staat hier, niet in Persoon.
 */
public class SchoonmakerNormalStrategy implements IMovementStrategy {

    @Override
    public void beweeg(Persoon persoon) {
        // Deze strategy is alleen bedoeld voor Schoonmaker-objecten.
        if (!(persoon instanceof Schoonmaker)) return;
        Schoonmaker sm = (Schoonmaker) persoon;

        if (sm.isInLift()) {
            volgLift(sm);
            return;
        }

        Kamer viezeKamer = sm.zoekViezeKamer();

        if (viezeKamer == null) {
            if (sm.getState() == Schoonmaker.State.NAAR_DOEL) {
                sm.setState(Schoonmaker.State.VRIJ);
                sm.setHuidigKamer(null);
            }

            // Strategy kiest opslag als doel; Persoon.beweegNaar(...) verplaatst de schoonmaker.
            if (sm.beweegNaar(7.5, 5.5, Schoonmaker.SPEED)) {
                sm.setState(Schoonmaker.State.VRIJ);
            }
            return;
        }

        double targetX = getAreaCenterX(viezeKamer.getArea());
        double targetY = getAreaCenterY(viezeKamer.getArea());

        if (sm.getState() == Schoonmaker.State.SCHOONMAKEN) {
            werkAanKamer(sm);
        } else {
            gaNaarKamer(sm, targetX, targetY, viezeKamer);
        }
    }

    private void gaNaarKamer(Schoonmaker sm, double tx, double ty, Kamer doelKamer) {
        if (doelKamer.getStatus() != Kamer.KamerStatus.SCHOONMAKEN) {
            sm.setState(Schoonmaker.State.VRIJ);
            sm.setHuidigKamer(null);
            return;
        }

        if ((int)sm.getY() != (int)ty) {
            if (sm.beweegNaar(Schoonmaker.LIFT_X, sm.getY(), Schoonmaker.SPEED)) {
                sm.stapInLift((int)ty);
            }
        } else {
            if (sm.beweegNaar(tx, sm.getY(), Schoonmaker.SPEED)) {
                if (doelKamer.getStatus() == Kamer.KamerStatus.SCHOONMAKEN) {
                    sm.setHuidigKamer(doelKamer);
                    sm.setState(Schoonmaker.State.SCHOONMAKEN);
                    sm.setSchoonmaakTimer(Schoonmaker.SCHOONMAAK_DUUR);

                    if (sm.getEventBus() != null) {
                        sm.getEventBus().triggerHotelEvent(HotelEventType.CLEANING_EMERGENCY,
                                sm.getNaam().hashCode(), doelKamer.getKamernummer());
                    }
                } else {
                    sm.setState(Schoonmaker.State.VRIJ);
                }
            }
        }
    }

    private void werkAanKamer(Schoonmaker sm) {
        sm.setSchoonmaakTimer(sm.getSchoonmaakTimer() - 1);
        if (sm.getSchoonmaakTimer() <= 0) {
            sm.getHuidigKamer().setStatus(Kamer.KamerStatus.VRIJ);
            System.out.println("[Schoonmaker] Kamer " + sm.getHuidigKamer().getKamernummer() + " is klaar.");
            sm.setHuidigKamer(null);
            sm.setState(Schoonmaker.State.VRIJ);
        }
    }

    private void volgLift(Schoonmaker sm) {
        sm.setX(sm.getLift().getX());
        sm.setY(sm.getLift().getY());

        if ((int)sm.getLift().getY() == sm.getDoelVerdieping() && sm.getLift().isIdle()) {
            sm.getLift().verwijderGast(sm);
            sm.setInLift(false);
            sm.setY(sm.getDoelVerdieping() + 0.5);

            if (sm.zoekViezeKamer() == null) {
                sm.setState(Schoonmaker.State.VRIJ);
            } else {
                sm.setState(Schoonmaker.State.NAAR_DOEL);
            }
        }
    }

    private double getAreaCenterX(Area area) { return area.getX() - 1 + area.getBreedte() / 2.0; }
    private double getAreaCenterY(Area area) { return area.getY() - 1 + area.getHoogte() / 2.0; }
}
