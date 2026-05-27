package model.strategy;

import model.Persoon;

/**
 * Strategy-interface voor wisselend bewegingsgedrag.
 * Elke concrete strategy bepaalt zelf hoe een Persoon beweegt tijdens een tick.
 *
 * Strategy Pattern rol: dit is de Strategy-interface.
 * Persoon gebruikt alleen deze interface en hoeft de concrete class niet te kennen.
 */
public interface IMovementStrategy {
    void beweeg(Persoon persoon);
}
