package model.strategy;

import model.Persoon;
import model.Gast;
import model.Schoonmaker;

/**
 * Concrete strategy: noodgedrag bij brandalarm.
 * Personen negeren normale doelen en bewegen via de trap richting uitgang.
 *
 * Strategy Pattern rol: dit is een concrete strategy die runtime gekozen wordt.
 * Gast en Schoonmaker kunnen allebei tijdelijk deze strategy gebruiken.
 */
public class EvacuationMovement implements IMovementStrategy {
    private static final double SPEED = 0.5;
    private static final double STAIR_X = 8.5;
    private static final double LOBBY_X = 1.5;

    @Override
    public void beweeg(Persoon persoon) {
        // Deze strategy werkt voor meerdere soorten Personen tijdens evacuatie.
        double pY = persoon.getY();

        // Bepaal de hotelhoogte dynamisch op basis van het type persoon
        int maxY = 6;
        if (persoon instanceof Gast) maxY = ((Gast) persoon).getMaxY();
        if (persoon instanceof Schoonmaker) maxY = ((Schoonmaker) persoon).getMaxY();
        int lobbyY = maxY - 1;

        // Stap 1: Als we nog boven zitten, loop eerst horizontaal naar de trap
        if ((int)pY != lobbyY) {
            persoon.beweegNaar(STAIR_X, lobbyY + 0.5, SPEED);
        } else {
            // Stap 2: We zijn in de lobby, loop nu horizontaal naar de uitgang
            if (persoon.beweegNaar(LOBBY_X, pY, SPEED)) {
                persoon.setX(-1.5); // Loop de simulatie uit (buiten het scherm)

                // Update de specifieke status van de persoon
                if (persoon instanceof Schoonmaker) {
                    ((Schoonmaker) persoon).setState(Schoonmaker.State.BUITEN);
                } else if (persoon instanceof Gast) {
                    ((Gast) persoon).setGastState(Gast.State.VERLAAT_HOTEL);
                }
            }
        }
    }
}
