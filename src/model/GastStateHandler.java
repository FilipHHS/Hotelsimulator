package model.state;

import model.Gast;

public interface GastStateHandler {
    default void onEnter(Gast gast) {}
    void onTick(Gast gast);
    default void onExit(Gast gast) {}
}