package ui;

import model.*;
import javax.swing.*;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class main {

    private static HotelPanel hotelPanel;
    private static JComboBox<String> layoutDropdown;
    private static JLabel statusLabel;
    private static JLabel timestepLabel;
    private static JButton startPauseButton;
    private static Simulator simulator;
    private static Timer simulationTimer;
    private static GuestListWindow guestListWindow;

    // Venster-caches voor hergebruik per kamer of persoon
    private static final Map<Kamer, RoomDetailWindow> roomWindows = new HashMap<>();
    private static final Map<Persoon, PersonDetailWindow> personWindows = new HashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Hotel Simulator");
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

                // --- UI SETUP ---
                JPanel controlPanel = new JPanel();
                String[] layouts = getAvailableLayouts();
                layoutDropdown = new JComboBox<>(layouts);

                JButton loadButton = new JButton("Laden");
                loadButton.addActionListener(_ -> loadLayout());

                startPauseButton = new JButton("Start");
                startPauseButton.addActionListener(_ -> toggleSimulation());

                // --- FIRE ALARM BUTTONS ---
                JButton fireAlarmButton = new JButton("🔥 BRANDALARM");
                fireAlarmButton.setBackground(new Color(255, 50, 50));
                fireAlarmButton.setForeground(Color.WHITE);
                fireAlarmButton.addActionListener(_ -> triggerFireAlarm());

                JButton clearAlarmButton = new JButton("✓ All Clear");
                clearAlarmButton.setBackground(new Color(50, 200, 50));
                clearAlarmButton.setForeground(Color.WHITE);
                clearAlarmButton.addActionListener(_ -> clearFireAlarm());

                JLabel speedLabel = new JLabel("Tick Interval: 100ms");
                JSlider speedSlider = new JSlider(50, 1000, 100);
                speedSlider.addChangeListener(_ -> {
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
                controlPanel.add(fireAlarmButton);
                controlPanel.add(clearAlarmButton);
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

                setupHotelPanelCallbacks(hotel, hotelPanel);

                frame.add(controlPanel, "North");
                frame.add(hotelPanel, "Center");
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                // --- MAIN LOOP ---
                simulationTimer = new Timer(50, _ -> runTick());
                simulationTimer.start();

            } catch (Exception e) {
                System.err.println("Error initializing simulator: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static void runTick() {
        if (simulator == null || !simulator.isRunning()) return;

        simulator.tick();
        int timestep = simulator.getClock().getTimestep();
        timestepLabel.setText("Timestep: " + timestep);
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

            setupHotelPanelCallbacks(newHotel, hotelPanel);

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
        String[] namen = {"Alice", "Bob", "Charlie", "Diana", "Frank", "Grace"};
        for (String naam : namen) {
            Gast g = new Gast(naam, -1, 0);
            g.setHotel(hotel);
            g.setStartPositie(-1.0, 6.5);
            hotel.addPersoon(g);
        }
    }

    private static void addSchoonmakers(Hotel hotel) {
        Schoonmaker s1 = new Schoonmaker("Schoonmaker1", 8, 6);
        Schoonmaker s2 = new Schoonmaker("Schoonmaker2", 8, 6);

        s1.setHotel(hotel);
        s2.setHotel(hotel);

        hotel.addPersoon(s1);
        hotel.addPersoon(s2);
    }

    private static String[] getAvailableLayouts() {
        File dir = new File("layouts");
        String[] files = dir.list((_ , n) -> n.endsWith(".json"));
        if (files != null) Arrays.sort(files);
        return (files != null) ? files : new String[0];
    }

    // === FIRE ALARM METHODS ===

    private static void triggerFireAlarm() {
        if (simulator != null && simulator.isRunning()) {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("🚨 🚨 🚨  FIRE ALARM ACTIVATED - EVACUATIE BEGONNEN  🚨 🚨 🚨");
            System.out.println("=".repeat(70) + "\n");

            simulator.triggerFireAlarm();
            statusLabel.setText("🔥 FIRE ALARM ACTIVE - EVACUATIE IN PROGRESS");
        } else {
            statusLabel.setText("⚠️ Simulatie moet actief zijn voor brandalarm!");
        }
    }

    private static void clearFireAlarm() {
        if (simulator != null) {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("✓ ✓ ✓  FIRE ALARM CLEARED - EVACUATION COMPLETE  ✓ ✓ ✓");
            System.out.println("=".repeat(70) + "\n");

            simulator.clearFireAlarm();
            statusLabel.setText("✓ Brandalarm uitgeschakeld - Normaal operatie");
        }
    }

    // === GUEST LIST WINDOW METHODS ===

    private static void openGuestListWindow(Hotel hotel) {
        if (guestListWindow != null && guestListWindow.isVisible()) {
            guestListWindow.toFront();
            guestListWindow.requestFocus();
        } else {
            guestListWindow = new GuestListWindow(hotel);
            guestListWindow.setVisible(true);
        }
    }

    // === DETAIL WINDOW METHODS ===

    private static void openRoomDetailWindow(Kamer kamer) {
        if (roomWindows.containsKey(kamer) && roomWindows.get(kamer).isVisible()) {
            roomWindows.get(kamer).toFront();
            roomWindows.get(kamer).requestFocus();
        } else {
            RoomDetailWindow window = new RoomDetailWindow(kamer);
            roomWindows.put(kamer, window);
            window.setVisible(true);
        }
    }

    private static void openPersonDetailWindow(Persoon persoon) {
        if (personWindows.containsKey(persoon) && personWindows.get(persoon).isVisible()) {
            personWindows.get(persoon).toFront();
            personWindows.get(persoon).requestFocus();
        } else {
            PersonDetailWindow window = new PersonDetailWindow(persoon);
            personWindows.put(persoon, window);
            window.setVisible(true);
        }
    }

    private static void setupHotelPanelCallbacks(Hotel hotel, HotelPanel panel) {
        panel.setOnLobbyClick(() -> openGuestListWindow(hotel));
        panel.setOnRoomClick(main::openRoomDetailWindow);
        panel.setOnPersonClick(main::openPersonDetailWindow);
    }
}