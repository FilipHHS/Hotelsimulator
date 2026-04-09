import model.Hotel;
import model.LayoutLoader;
import model.Gast;
import ui.HotelPanel;
import model.Simulator;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;

public class main {

    private static HotelPanel hotelPanel;
    private static JComboBox<String> layoutDropdown;
    private static JLabel statusLabel;
    private static Simulator simulator;
    // --- US2.3: Variabele voor de timer toevoegen ---
    private static Timer simulationTimer;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Hotel Simulator");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JPanel controlPanel = new JPanel();
                controlPanel.add(new JLabel("Selecteer layout: "));

                String[] layouts = getAvailableLayouts();
                layoutDropdown = new JComboBox<>(layouts);
                controlPanel.add(layoutDropdown);

                JButton loadButton = new JButton("Laden");
                loadButton.addActionListener(e -> loadLayout());
                controlPanel.add(loadButton);

                JButton pauseButton = new JButton("Pause");
                pauseButton.setEnabled(false);

                JButton startButton = new JButton("Start");
                startButton.addActionListener(e -> {
                    if (!simulator.isRunning()) {
                        simulator.start();
                        startButton.setEnabled(false);
                        pauseButton.setEnabled(true);
                        statusLabel.setText("Simulatie actief");
                    }
                });
                controlPanel.add(startButton);

                pauseButton.addActionListener(e -> {
                    if (simulator.isRunning()) {
                        simulator.pause();
                        pauseButton.setText("Resume");
                        startButton.setEnabled(true);
                        statusLabel.setText("Simulatie gepauzeerd");
                    } else {
                        simulator.start();
                        pauseButton.setText("Pause");
                        startButton.setEnabled(false);
                        statusLabel.setText("Simulatie actief");
                    }
                });
                controlPanel.add(pauseButton);

                // --- US2.3: Slider toevoegen aan de GUI ---
                // We kiezen een range van 50ms (supersnel) tot 1000ms (traag).
                // De startwaarde is 500ms.
                JLabel speedLabel = new JLabel("Delay: 500ms");
                JSlider speedSlider = new JSlider(50, 1000, 500);

                speedSlider.addChangeListener(e -> {
                    int delay = speedSlider.getValue();
                    // Pas de snelheid van de tikkende timer aan
                    if (simulationTimer != null) {
                        simulationTimer.setDelay(delay);
                    }
                    // Geef de waarde ook door aan de simulator (voor interne logica)
                    if (simulator != null) {
                        simulator.setSpeed(delay);
                    }
                    speedLabel.setText("Delay: " + delay + "ms");
                });

                controlPanel.add(speedLabel);
                controlPanel.add(speedSlider);

                statusLabel = new JLabel("Hotel laden...");
                controlPanel.add(statusLabel);

                Hotel hotel = LayoutLoader.laadLayout("layouts/" + layouts[0]);
                hotelPanel = new HotelPanel(hotel);
                addTestGuests(hotel);

                simulator = new Simulator(hotel, hotelPanel);
                simulator.pause();

                statusLabel.setText("Hotel geladen: " + layouts[0]);

                frame.add(controlPanel, "North");
                frame.add(hotelPanel, "Center");

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                // --- US2.3: De timer opslaan in de variabele ---
                // In plaats van 'new Timer().start()' slaan we hem nu op zodat 'setDelay' werkt
                simulationTimer = new Timer(500, e -> simulator.tick());
                simulationTimer.start();

            } catch (Exception e) {
                System.out.println("Fout: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static String[] getAvailableLayouts() {
        File layoutDir = new File("layouts");
        String[] layouts = layoutDir.list((dir, name) -> name.endsWith(".json"));
        if (layouts == null || layouts.length == 0) {
            throw new RuntimeException("Geen layout bestanden gevonden");
        }
        Arrays.sort(layouts);
        return layouts;
    }

    private static void loadLayout() {
        try {
            String selectedLayout = (String) layoutDropdown.getSelectedItem();
            Hotel newHotel = LayoutLoader.laadLayout("layouts/" + selectedLayout);
            hotelPanel.setHotel(newHotel);
            addTestGuests(newHotel);

            simulator = new Simulator(newHotel, hotelPanel);
            simulator.pause();

            statusLabel.setText("Hotel geladen: " + selectedLayout);
        } catch (Exception e) {
            statusLabel.setText("Fout: " + e.getMessage());
        }
    }

    private static void addTestGuests(Hotel hotel) {
        model.Gast gast1 = new model.Gast("Alice", 2, 2);
        gast1.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
        hotel.addPersoon(gast1);

        model.Gast gast2 = new model.Gast("Bob", 3, 3);
        gast2.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
        hotel.addPersoon(gast2);

        model.Gast gast3 = new model.Gast("Charlie", 1, 1);
        gast3.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
        hotel.addPersoon(gast3);
    }
}