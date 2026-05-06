package ui;

import model.*;
import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class main {

    private static HotelPanel hotelPanel;
    private static JComboBox<String> layoutDropdown;
    private static JLabel statusLabel;
    private static JLabel timestepLabel;
    private static JButton startPauseButton;
    private static Simulator simulator;
    private static Timer simulationTimer;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Hotel Simulator");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // --- UI SETUP ---
                JPanel controlPanel = new JPanel();
                String[] layouts = getAvailableLayouts();
                layoutDropdown = new JComboBox<>(layouts);

                JButton loadButton = new JButton("Laden");
                loadButton.addActionListener(e -> loadLayout());

                startPauseButton = new JButton("Start");
                startPauseButton.addActionListener(e -> toggleSimulation());

                JLabel speedLabel = new JLabel("Tick Interval: 100ms");
                JSlider speedSlider = new JSlider(50, 1000, 100);
                speedSlider.addChangeListener(e -> {
                    int val = speedSlider.getValue();
                    if (simulator != null) simulator.getClock().setTickInterval(val);
                    speedLabel.setText("Tick Interval: " + val + "ms");
                });

                statusLabel = new JLabel("Hotel laden...");
                timestepLabel = new JLabel("Timestep: 0");

                controlPanel.add(new JLabel("Layout: "));
                controlPanel.add(layoutDropdown);
                controlPanel.add(loadButton);
                controlPanel.add(startPauseButton);
                controlPanel.add(speedLabel);
                controlPanel.add(speedSlider);
                controlPanel.add(statusLabel);
                controlPanel.add(timestepLabel);

                // --- INITIALISATIE DATA ---
                Hotel hotel = LayoutLoader.laadLayout("layouts/" + layouts[0]);
                initializeKamers(hotel);
                addTestGuests(hotel);
                addSchoonmakers(hotel);

                hotelPanel = new HotelPanel(hotel);
                simulator = new Simulator(hotel, hotelPanel);

                if (simulator.getLift() != null) {
                    hotelPanel.setLift(simulator.getLift());
                }

                frame.add(controlPanel, "North");
                frame.add(hotelPanel, "Center");
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                // --- MAIN LOOP ---
                simulationTimer = new Timer(50, e -> runTick());
                simulationTimer.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void runTick() {
        if (simulator == null || !simulator.isRunning()) return;

        // 1. Voer de tick uit in de simulator
        // Deze methode moet in Simulator.java de hteClock.tick() aanroepen
        simulator.tick();

        // 2. Update de UI labels
        int timestep = simulator.getClock().getTimestep();
        timestepLabel.setText("Timestep: " + timestep);

        // 3. Test scenario's uitvoeren op basis van HTE-tijd
        if (timestep == 50) simulator.gastCheckin("Alice", "Luxe");
        if (timestep == 100) simulator.gastCheckin("Bob", "Luxe");
        if (timestep == 150) simulator.gastCheckin("Charlie", "Standaard");
        if (timestep == 200) simulator.gastCheckout("Alice");
        if (timestep == 250) simulator.gastCheckout("Bob");
        if (timestep == 300) simulator.gastCheckout("Charlie");
    }

    private static void toggleSimulation() {
        if (!simulator.isRunning()) {
            simulator.start();
            startPauseButton.setText("Pause");
            statusLabel.setText("Simulatie actief");
        } else {
            simulator.pause();
            startPauseButton.setText("Resume");
            statusLabel.setText("Simulatie gepauzeerd");
        }
    }

    private static void loadLayout() {
        try {
            String selected = (String) layoutDropdown.getSelectedItem();
            Hotel newHotel = LayoutLoader.laadLayout("layouts/" + selected);

            initializeKamers(newHotel);
            addTestGuests(newHotel);
            addSchoonmakers(newHotel);

            hotelPanel.setHotel(newHotel);
            simulator = new Simulator(newHotel, hotelPanel);

            if (simulator.getLift() != null) hotelPanel.setLift(simulator.getLift());

            startPauseButton.setText("Start");
            statusLabel.setText("Geladen: " + selected);
        } catch (Exception e) {
            statusLabel.setText("Fout: " + e.getMessage());
        }
    }

    private static void initializeKamers(Hotel hotel) {
        List<Area> roomAreas = new ArrayList<>();
        for (Area a : hotel.getAreas()) {
            if ("Room".equals(a.getAreaType())) roomAreas.add(a);
        }

        int[] nummers = {101, 102, 201, 202};
        String[] types = {"Luxe", "Luxe", "Standaard", "Standaard"};

        for (int i = 0; i < nummers.length && i < roomAreas.size(); i++) {
            Kamer k = new Kamer(nummers[i], types[i]);
            k.setArea(roomAreas.get(i));
            hotel.addKamer(k);
        }
    }

    private static void addTestGuests(Hotel hotel) {
        String[] namen = {"Alice", "Bob", "Charlie"};
        double[][] pos = {{2, 6}, {3, 6}, {1, 7}};

        for (int i = 0; i < namen.length; i++) {
            Gast g = new Gast(namen[i], (int)pos[i][0], (int)pos[i][1]);
            g.setHotel(hotel);
            hotel.addPersoon(g);
        }
    }

    private static void addSchoonmakers(Hotel hotel) {
        // HIER GEFIXED: Ze spawnen nu direct in de Opslag (8, 6)
        Schoonmaker s1 = new Schoonmaker("Schoonmaker1", 8, 6);
        Schoonmaker s2 = new Schoonmaker("Schoonmaker2", 8, 6);

        s1.setHotel(hotel);
        s2.setHotel(hotel);

        hotel.addPersoon(s1);
        hotel.addPersoon(s2);
    }

    private static String[] getAvailableLayouts() {
        File dir = new File("layouts");
        String[] files = dir.list((d, name) -> name.endsWith(".json"));
        if (files != null) Arrays.sort(files);
        return (files != null) ? files : new String[0];
    }
}