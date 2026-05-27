package model.strategy;

import model.Persoon;

/**
 * Context class van het Strategy Pattern.
 * Deze class beheert welke bewegingsstrategie actief is
 * en voert die strategie uit.
 */
public class MovementContext {

    // de huidige strategy
    private IMovementStrategy currentStrategy;

    // normale bewegingsstrategie
    private IMovementStrategy normalStrategy;

    // De evacuatie-strategy (bijvoorbeeld bij brandalarm)
    private IMovementStrategy evacuationStrategy;

    /**
     * Constructor.
     * Hier worden de strategies van buitenaf meegegeven.
     */
    public MovementContext(IMovementStrategy normalStrategy,
                           IMovementStrategy evacuationStrategy) {

        // Sla beide strategies op in de context
        setStrategies(normalStrategy, evacuationStrategy);

        // Start standaard met de normale strategy
        useNormalStrategy();
    }

    /**
     * Injecteer of vervang de strategies.
     */
    public void setStrategies(IMovementStrategy normalStrategy,
                              IMovementStrategy evacuationStrategy) {

        // Bewaar de normale strategy
        this.normalStrategy = normalStrategy;

        // Bewaar de evacuatie-strategy
        this.evacuationStrategy = evacuationStrategy;

        // Zet de huidige strategy standaard op normaal gedrag
        this.currentStrategy = normalStrategy;
    }

    /**
     * Voer beweging uit via de actieve strategy.
     */
    public void beweeg(Persoon persoon) {

        // Controleer of er een strategy actief is
        if (currentStrategy != null) {

            // Polymorfisme:
            // Java roept automatisch de juiste beweeg()-methode aan
            currentStrategy.beweeg(persoon);
        }
    }

    /**
     * Wissel terug naar normaal gedrag.
     */
    public void useNormalStrategy() {

        // Zet de actieve strategy op normaal gedrag
        this.currentStrategy = normalStrategy;
    }

    /**
     * Wissel naar evacuatiegedrag.
     */
    public void useEvacuationStrategy() {

        // Zet de actieve strategy op evacuatiegedrag
        this.currentStrategy = evacuationStrategy;
    }

    /**
     * Controleer of evacuatiegedrag actief is.
     */
    public boolean isUsingEvacuationStrategy() {

        // Geeft true terug als currentStrategy gelijk is aan evacuationStrategy
        return currentStrategy == evacuationStrategy;
    }
}