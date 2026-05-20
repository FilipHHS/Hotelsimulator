package model.strategy;

import model.Persoon;

/**
 * De blauwdruk voor alle loop- en navigatiestrategieën.
 */
public interface IMovementStrategy {
    void move(Persoon persoon);
}