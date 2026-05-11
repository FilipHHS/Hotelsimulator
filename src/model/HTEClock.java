package model;

import model.TickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// De centrale klok van de simulatie
public class HTEClock {

    // Gebruik CopyOnWriteArrayList om ConcurrentModificationException te voorkomen
    private final List<TickListener> listeners = new CopyOnWriteArrayList<>();

    // Houdt bij hoeveel ticks er zijn geweest
    private long tickCount = 0;  // Changed to long to prevent integer overflow

    // Voeg een entiteit toe aan de klok
    public void addListener(TickListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    // Verwijder een entiteit (optioneel)
    public void removeListener(TickListener listener) {
        listeners.remove(listener);
    }

    // Deze methode stelt één tijdstap (tick) voor
    public void tick() {
        tickCount++;

        // Debug output (handig tijdens testen) - less verbose
        if (tickCount % 100 == 0) {  // Only print every 100 ticks to reduce console spam
            System.out.println("====== HTEClock TICK #" + tickCount + " (Listeners: " + listeners.size() + ") ======");
        }

        // BELANGRIJK:
        // Alle entiteiten reageren hier tegelijk op dezelfde tick
        try {
            for (TickListener listener : listeners) {
                if (listener != null) {
                    listener.onTick();
                }
            }
        } catch (Exception e) {
            System.err.println("[HTEClock] Error during tick #" + tickCount + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        if (tickCount % 100 == 0) {
            System.out.println("====== Einde TICK #" + tickCount + " ======\n");
        }
    }
}