package model.strategy;

import model.Persoon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simpele JUnit tests voor de Strategy Pattern context.
 * We gebruiken fake strategies zodat we makkelijk kunnen zien welke strategy is aangeroepen.
 */
class MovementContextTest {

    @Test
    void contextGebruiktStandaardDeNormaleStrategy() {
        // Arrange: maak twee nep-strategies.
        TestStrategy normalStrategy = new TestStrategy();
        TestStrategy evacuationStrategy = new TestStrategy();
        MovementContext context = new MovementContext(normalStrategy, evacuationStrategy);

        // Act: laat de context bewegen.
        context.beweeg(null);

        // Assert: standaard moet alleen de normale strategy gebruikt zijn.
        assertTrue(normalStrategy.isAangeroepen());
        assertFalse(evacuationStrategy.isAangeroepen());
    }

    @Test
    void contextKanWisselenNaarEvacuatieStrategy() {
        // Arrange: maak een context met normale en evacuatie-strategy.
        TestStrategy normalStrategy = new TestStrategy();
        TestStrategy evacuationStrategy = new TestStrategy();
        MovementContext context = new MovementContext(normalStrategy, evacuationStrategy);

        // Act: wissel naar evacuatie en voer beweging uit.
        context.useEvacuationStrategy();
        context.beweeg(null);

        // Assert: nu moet de evacuatie-strategy gebruikt zijn.
        assertTrue(context.isUsingEvacuationStrategy());
        assertFalse(normalStrategy.isAangeroepen());
        assertTrue(evacuationStrategy.isAangeroepen());
    }

    /**
     * Kleine fake strategy speciaal voor deze test.
     * Hij beweegt niemand echt, maar onthoudt alleen of beweeg() is aangeroepen.
     */
    private static class TestStrategy implements IMovementStrategy {
        private boolean aangeroepen = false;

        @Override
        public void beweeg(Persoon persoon) {
            aangeroepen = true;
        }

        boolean isAangeroepen() {
            return aangeroepen;
        }
    }
}
