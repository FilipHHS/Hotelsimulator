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
    private static GuestListWindow guestListWindow;  // US2.1: Gastenoverzicht

    // New: Detail windows (cache)
    private static java.util.HashMap<Object, JFrame> detailWindows = new java.util.HashMap<>();

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

                // --- FIRE ALARM BUTTON ---
                JButton fireAlarmButton = new JButton("🔥 BRANDALARM");
                fireAlarmButton.setBackground(new java.awt.Color(255, 50, 50));
                fireAlarmButton.setForeground(java.awt.Color.WHITE);
                fireAlarmButton.addActionListener(e -> triggerFireAlarm());

                JButton clearAlarmButton = new JButton("✓ All Clear");
                clearAlarmButton.setBackground(new java.awt.Color(50, 200, 50));
                clearAlarmButton.setForeground(java.awt.Color.WHITE);
                clearAlarmButton.addActionListener(e -> clearFireAlarm());

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

                // Setup all callbacks (lobby, room, person)
                setupHotelPanelCallbacks(hotel, hotelPanel);

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

        // Voer de simulator tick uit
        simulator.tick();

        // Update de UI labels
        int timestep = simulator.getClock().getTimestep();
        timestepLabel.setText("Timestep: " + timestep);

        // Optional: Manual test scenarios (most are now automatic)
        // Auto-checkin and auto-checkout are handled in Simulator now
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

            // Setup all callbacks (lobby, room, person)
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
        // Add more initial test guests - they will spawn at x = -1.0
        String[] namen = {"Alice", "Bob", "Charlie", "Diana", "Frank", "Grace"};
        for (int i = 0; i < namen.length; i++) {
            Gast g = new Gast(namen[i], -1, 0);
            g.setHotel(hotel);
            g.setStartPositie(-1.0, 6.5); // Spawn outside at lobby level
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

    // === FIRE ALARM METHODS (US4.3: Brandalarm) ===

    /**
     * Trigger fire alarm - evacuate all people to lobby
     */
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

    /**
     * Clear fire alarm - resume normal operations
     */
    private static void clearFireAlarm() {
        if (simulator != null) {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("✓ ✓ ✓  FIRE ALARM CLEARED - EVACUATION COMPLETE  ✓ ✓ ✓");
            System.out.println("=".repeat(70) + "\n");

            simulator.clearFireAlarm();
            statusLabel.setText("✓ Brandalarm uitgeschakeld - Normaal operatie");
        }
    }

    // === US2.1: GUEST LIST WINDOW METHODS ===

    /**
     * Opent het gastenoverzicht venster
     * Toont alle gasten met hun huidge status en informatie
     */
    private static void openGuestListWindow(Hotel hotel) {
        if (guestListWindow != null && guestListWindow.isVisible()) {
            // Venster is al open - zet het in focus en vernieuw
            guestListWindow.toFront();
            guestListWindow.requestFocus();
        } else {
            // Maak nieuw venster
            guestListWindow = new GuestListWindow(hotel);
            guestListWindow.setVisible(true);
        }
    }

    // === NEW: DETAIL WINDOW METHODS ===

    /**
     * Opent kamer-detail window (reuse als al open)
     */
    private static void openRoomDetailWindow(Kamer kamer) {
        Object key = kamer;  // Use Kamer object as key

        if (detailWindows.containsKey(key) && detailWindows.get(key).isVisible()) {
            // Window al open - focus
            detailWindows.get(key).toFront();
            detailWindows.get(key).requestFocus();
        } else {
            // Maak nieuw window
            RoomDetailWindow window = new RoomDetailWindow(kamer);
            detailWindows.put(key, window);
            window.setVisible(true);
        }
    }

    /**
     * Opent persoon-detail window (reuse als al open)
     */
    private static void openPersonDetailWindow(Persoon persoon) {
        Object key = persoon;  // Use Persoon object as key

        if (detailWindows.containsKey(key) && detailWindows.get(key).isVisible()) {
            // Window al open - focus
            detailWindows.get(key).toFront();
            detailWindows.get(key).requestFocus();
        } else {
            // Maak nieuw window
            PersonDetailWindow window = new PersonDetailWindow(persoon);
            detailWindows.put(key, window);
            window.setVisible(true);
        }
    }

    /**
     * Helpermethod: Stel alle callbacks in voor een hotel + panel combinatie
     */
    private static void setupHotelPanelCallbacks(Hotel hotel, HotelPanel panel) {
        // Lobby click
        panel.setOnLobbyClick(() -> openGuestListWindow(hotel));

        // Kamer click
        panel.setOnRoomClick(kamer -> openRoomDetailWindow(kamer));

        // Persoon click
        panel.setOnPersonClick(persoon -> openPersonDetailWindow(persoon));
    }
}
