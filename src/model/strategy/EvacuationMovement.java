package model.strategy;

import model.personen.Persoon;


/**
 * Strategie voor wanneer het brandalarm afgaat.
 * Personen negeren de lift en rennen via de trap (x=8.5) naar buiten.
 */
public class EvacuationMovement implements IMovementStrategy {
    private static final double SPEED = 0.5;
    private static final double STAIR_X = 8.5;
    private static final double LOBBY_X = 1.5;

    @Override
    public void beweeg(Persoon persoon) {
        double pX = persoon.getX(); // SS3.1: lees huidige positie
        double pY = persoon.getY();

        // Bepaal de hotelhoogte dynamisch op basis van het type persoon
        int maxY = persoon.getMaxY();        int lobbyY = maxY - 1; // SS3.2: lobbyverdieping dynamisch bepalen (werkt voor elke hotelhoogte)

        // Stap 1: Als we nog boven zitten, loop eerst horizontaal naar de trap
        if ((int)pY != lobbyY) { // SS3.3: nog NIET op de lobbyverdieping?
            if (Math.abs(pX - STAIR_X) > SPEED) {
                persoon.setX(pX + (pX < STAIR_X ? SPEED : -SPEED)); // SS3.4: loop horizontaal naar de trap (x=8.5) — lift wordt genegeerd!
            } else {
                // Eenmaal bij de trap: zak verticaal naar beneden richting lobby
                persoon.setY(pY + (pY < lobbyY ? SPEED : -SPEED)); // SS3.5: bij de trap → zak verticaal richting lobby
            }
        } else {
            // Stap 2: We zijn in de lobby, loop nu horizontaal naar de uitgang
            double dx = LOBBY_X - pX; // SS3.6 + SS3.7: op de lobbyverdieping → richting de uitgang (x=1.5)
            if (Math.abs(dx) < SPEED) {
                persoon.setX(-1.5); // SS3.8: bijna bij de uitgang → loop het scherm uit

                // Update de specifieke status van de persoon
                persoon.setStateToLeft(); // SS3.9: abstracte methode — Gast/Schoonmaker zet elk hun EIGEN state op BUITEN
            } else {
                persoon.setX(pX + (dx > 0 ? SPEED : -SPEED));
            }
        }
    }
}