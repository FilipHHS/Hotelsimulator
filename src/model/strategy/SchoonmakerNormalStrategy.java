package model.strategy;

import hotelevents.HotelEventType;
import model.Kamer;
import model.personen.Persoon;
import model.personen.Schoonmaker;
import model.Area;

public class SchoonmakerNormalStrategy implements IMovementStrategy {

    @Override
    public void beweeg(Persoon persoon) {
        if (!(persoon instanceof Schoonmaker)) return; // SS2.1: type-check — alleen schoonmakers
        Schoonmaker sm = (Schoonmaker) persoon;

        if (sm.isInLift()) { // SS2.2: in de lift? → liftlogica en klaar voor deze tick
            handleLiftMovement(sm);
            return;
        }

        Kamer viezeKamer = sm.zoekViezeKamer(); // SS2.3: zoek een kamer met status SCHOONMAKEN

        if (viezeKamer == null) { // SS2.4: geen werk → reset state en loop terug naar de opslag (7.5, 5.5)
            if (sm.getState() == Schoonmaker.State.NAAR_DOEL) {
                sm.setState(Schoonmaker.State.VRIJ);
                sm.setHuidigKamer(null);
            }

            // Loop rustig terug naar de opslag op (7.5, 5.5)
            if (Math.abs(sm.getX() - 7.5) > 0.1) {
                sm.setX(sm.getX() + (sm.getX() < 7.5 ? Schoonmaker.SPEED : -Schoonmaker.SPEED));
            } else if (Math.abs(sm.getY() - 5.5) > 0.1) {
                sm.setY(sm.getY() + (sm.getY() < 5.5 ? Schoonmaker.SPEED : -Schoonmaker.SPEED));
            } else {
                sm.setState(Schoonmaker.State.VRIJ);
            }
            return;
        }

        double targetX = getAreaCenterX(viezeKamer.getArea()); // SS2.5: doel = midden van de kamer-Area
        double targetY = getAreaCenterY(viezeKamer.getArea());

        if (sm.getState() == Schoonmaker.State.SCHOONMAKEN) {
            werkAanKamer(sm); // SS2.6: timer aftellen; op 0 → kamer VRIJ + schoonmaker VRIJ
        } else {
            beweegNaarKamer(sm, targetX, targetY, viezeKamer); // SS2.7: naar de kamer lopen (zie a t/m d hieronder)
        }
    }

    private void beweegNaarKamer(Schoonmaker sm, double tx, double ty, Kamer doelKamer) {
        if (doelKamer.getStatus() != Kamer.KamerStatus.SCHOONMAKEN) { // SS2.7a: guard — collega was sneller? → VRIJ en stop
            sm.setState(Schoonmaker.State.VRIJ);
            sm.setHuidigKamer(null);
            return;
        }

        if ((int)sm.getY() != (int)ty) { // SS2.7b: verkeerde verdieping → naar LIFT_X lopen en lift instappen
            if (Math.abs(sm.getX() - Schoonmaker.LIFT_X) > 0.1) {
                sm.setX(sm.getX() + (sm.getX() < Schoonmaker.LIFT_X ? Schoonmaker.SPEED : -Schoonmaker.SPEED));
            } else {
                sm.stapInLift((int)ty);
            }
        } else {
            if (Math.abs(sm.getX() - tx) > 0.1) { // SS2.7c: goede verdieping → loop horizontaal naar het kamercentrum
                sm.setX(sm.getX() + (sm.getX() < tx ? Schoonmaker.SPEED : -Schoonmaker.SPEED));
            } else {
                if (doelKamer.getStatus() == Kamer.KamerStatus.SCHOONMAKEN) {
                    sm.setHuidigKamer(doelKamer);
                    sm.setState(Schoonmaker.State.SCHOONMAKEN); // SS2.7d: aangekomen → kamer claimen + timer + CLEANING_EMERGENCY-event
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

    private void handleLiftMovement(Schoonmaker sm) {
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