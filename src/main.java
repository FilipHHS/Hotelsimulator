import model.Hotel;
import model.LayoutLoader;
import ui.HotelPanel;
import model.Simulator;
import model.Gast;
import model.Kamer;
import model.SimulationClock;
import model.HTEClock;
import model.Schoonmaker;
import model.Area;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;

/**
 * De MainUI klasse is het startpunt van de applicatie.
 * Het regelt de GUI (Swing), de interactie met de gebruiker en de aansturing van de simulator.
 */
public class main {

    // --- VARIABELEN (State) ---
    private static HotelPanel hotelPanel;      // Het canvas waarop het hotel getekend wordt
    private static JComboBox<String> layoutDropdown; // Keuzemenu voor de JSON-bestanden
    private static JLabel statusLabel;         // Tekstbalk onderin voor statusberichten
    private static JLabel timestepLabel;       // Toont de huidige tijdstap (HTE)
    private static JButton startPauseButton;   // De knop die wisselt tussen Start en Pause
    private static Simulator simulator;        // De 'hersenen' van de simulatie (logica)

    /** * US2.3: De Timer is de 'hartslag' van de UI.
     * We maken er een variabele van zodat de slider de hartslag kan versnellen of vertragen.
     */
    private static Timer simulationTimer;

    public static void main(String[] args) {
        // SwingUtilities zorgt ervoor dat de UI veilig op de 'Event Dispatch Thread' draait.
        // Dit voorkomt dat je scherm vastloopt (freezes).
        SwingUtilities.invokeLater(() -> {
            try {
                // --- FRAME INITIALISATIE ---
                JFrame frame = new JFrame("Hotel Simulator");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Stopt programma bij afsluiten

                // ControlPanel is de grijze balk bovenin met alle knoppen.
                JPanel controlPanel = new JPanel();
                controlPanel.add(new JLabel("Selecteer layout: "));

                // Haal de lijst met .json bestanden op uit de map 'layouts'.
                String[] layouts = getAvailableLayouts();
                layoutDropdown = new JComboBox<>(layouts);
                controlPanel.add(layoutDropdown);

                // --- KNOPPEN LOGICA ---

                // Laden-knop: roept de methode aan om een nieuw hotel in te laden.
                JButton loadButton = new JButton("Laden");
                loadButton.addActionListener(e -> loadLayout());
                controlPanel.add(loadButton);

                // Start/Pause-knop:
                startPauseButton = new JButton("Start");
                startPauseButton.addActionListener(e -> {
                    // Check of de simulator op dit moment draait
                    if (!simulator.isRunning()) {
                        simulator.start(); // Logische start
                        startPauseButton.setText("Pause"); // Verander tekst op knop
                        statusLabel.setText("Simulatie actief");
                    } else {
                        simulator.pause(); // Logische pauze
                        startPauseButton.setText("Resume"); // Verander tekst op knop
                        statusLabel.setText("Simulatie gepauzeerd");
                    }
                });
                controlPanel.add(startPauseButton);

                // --- US2.3: DE SNELHEID SLIDER ---
                // We maken een label om de huidige snelheid in tekst te tonen (ms).
                JLabel speedLabel = new JLabel("Tick Interval: 100ms");

                /**
                 * JSlider(min, max, startwaarde).
                 * We gebruiken 50ms als snelste (meer ticks per seconde).
                 * We gebruiken 1000ms als langzaamste (1 tick per sec).
                 */
                JSlider speedSlider = new JSlider(50, 1000, 100);

                // ChangeListener: wordt uitgevoerd zodra je het schuifje beweegt.
                speedSlider.addChangeListener(e -> {
                    int newInterval = speedSlider.getValue(); // Haal de nieuwe waarde uit de slider

                    if (simulator != null) {
                        // Pas de tick-interval aan in de SimulationClock
                        simulator.getClock().setTickInterval(newInterval);
                    }
                    // Werk de tekst op het scherm bij.
                    speedLabel.setText("Tick Interval: " + newInterval + "ms");
                });

                controlPanel.add(speedLabel);
                controlPanel.add(speedSlider);

                // --- LABELS VOOR INFORMATIE ---
                statusLabel = new JLabel("Hotel laden...");
                controlPanel.add(statusLabel);

                timestepLabel = new JLabel("Timestep: 0");
                controlPanel.add(timestepLabel);

                // --- EERSTE SETUP ---
                // We laden direct het eerste hotel uit de lijst zodat het scherm niet leeg is.
                Hotel hotel = LayoutLoader.laadLayout("layouts/" + layouts[0]);

                // === KAMERS AANMAKEN ===
                initializeKamers(hotel);

                hotelPanel = new HotelPanel(hotel); // Maak het teken-veld aan
                addTestGuests(hotel); // Zet Alice, Bob en Charlie in het hotel
                addSchoonmakers(hotel); // Voeg schoonmakers toe

                // Maak de simulator-engine aan.
                simulator = new Simulator(hotel, hotelPanel);
                simulator.pause(); // De simulatie staat stil tot de gebruiker op Start klikt.

                // === LINK LIFT AAN PANEL VOOR VISUALISATIE ===
                if (simulator.getLift() != null) {
                    hotelPanel.setLift(simulator.getLift());
                }

                // Voeg de onderdelen toe aan het hoofdvenster.
                frame.add(controlPanel, "North"); // Knoppen boven
                frame.add(hotelPanel, "Center"); // Hotel in het midden
                frame.pack(); // Maak venster precies groot genoeg
                frame.setLocationRelativeTo(null); // Centreer venster op scherm
                frame.setVisible(true); // Toon het venster

                /**
                 * DE KLOK (Timer) - HTE TICK ARCHITECTUUR MET US3.1 CHECK-IN/CHECK-OUT TESTS:
                 * - Timer 'vuurt' elke 50ms (UI refresh rate)
                 * - Simulator bepaalt via SimulationClock wanneer een HTE-tick plaatsvindt
                 * - NIEUW: US3.1 test Scenario 1 (CHECK-IN) en Scenario 2 (CHECK-OUT)
                 */
                simulationTimer = new Timer(50, e -> {
                    // Roep simulator.tick() aan - dit bepaalt intern of een HTE-tick nodig is
                    simulator.tick();

                    // Update UI met huidige timestep (alleen weergeven)
                    timestepLabel.setText("Timestep: " + simulator.getClock().getTimestep());

                    // === US3.1 CHECK-IN/CHECK-OUT TESTS ===
                    int timestep = simulator.getClock().getTimestep();

                    // US3.1 SCENARIO 1: CHECK-IN - Alice incheckt in Luxe kamer (na 2.5 sec = 50 ticks)
                    if (timestep == 50) {
                        System.out.println("\n" + "=".repeat(80));
                        System.out.println("[US3.1 SCENARIO 1: SUCCESVOL INCHECKEN - Alice");
                        System.out.println("Given: Alice arriveert met verzoek voor Luxe kamer");
                        System.out.println("When: simulator.gastCheckin('Alice', 'Luxe') wordt aangeroepen");
                        System.out.println("=".repeat(80));
                        boolean succes1 = simulator.gastCheckin("Alice", "Luxe");
                        if (succes1) {
                            System.out.println("PASS: Alice is ingecheckt in Luxe kamer - status BEZET");
                        }
                    }

                    // US3.1 SCENARIO 1: CHECK-IN - Bob incheckt in Luxe kamer (na 5 sec = 100 ticks)
                    if (timestep == 100) {
                        System.out.println("\n" + "=".repeat(80));
                        System.out.println("[US3.1 SCENARIO 1: SUCCESVOL INCHECKEN - Bob");
                        System.out.println("Given: Bob arriveert met verzoek voor Luxe kamer");
                        System.out.println("When: simulator.gastCheckin('Bob', 'Luxe') wordt aangeroepen");
                        System.out.println("=".repeat(80));
                        boolean succes2 = simulator.gastCheckin("Bob", "Luxe");
                        if (succes2) {
                            System.out.println("PASS: Bob is ingecheckt in Luxe kamer - status BEZET");
                        }
                    }

                    // US3.1 SCENARIO 1: CHECK-IN - Charlie incheckt in Standaard kamer (na 7.5 sec = 150 ticks)
                    if (timestep == 150) {
                        System.out.println("\n" + "=".repeat(80));
                        System.out.println("[US3.1 SCENARIO 1: SUCCESVOL INCHECKEN - Charlie");
                        System.out.println("Given: Charlie arriveert met verzoek voor Standaard kamer");
                        System.out.println("When: simulator.gastCheckin('Charlie', 'Standaard') wordt aangeroepen");
                        System.out.println("=".repeat(80));
                        boolean succes3 = simulator.gastCheckin("Charlie", "Standaard");
                        if (succes3) {
                            System.out.println("PASS: Charlie is ingecheckt in Standaard kamer - status BEZET");
                        }
                    }

                    // US3.1 SCENARIO 2: CHECK-OUT - Alice verlaat hotel (na 10 sec = 200 ticks)
                    if (timestep == 200) {
                        System.out.println("\n" + "=".repeat(80));
                        System.out.println("[US3.1 SCENARIO 2: CHECKOUT & VERLAAT HOTEL - Alice");
                        System.out.println("Given: Alice verblijft in kamer");
                        System.out.println("When: simulator.gastCheckout('Alice') wordt aangeroepen");
                        System.out.println("Then:");
                        System.out.println("  - Alice verlaat het hotel (verwijderd uit simulatie)");
                        System.out.println("  - Kamerstatus gemarkeerd als VIES (SCHOONMAKEN)");
                        System.out.println("=".repeat(80));
                        simulator.gastCheckout("Alice");
                        System.out.println("PASS: Alice verlaten hotel succesvol");
                    }

                    // US3.1 SCENARIO 2: CHECK-OUT - Bob verlaat hotel (na 12.5 sec = 250 ticks)
                    if (timestep == 250) {
                        System.out.println("\n" + "=".repeat(80));
                        System.out.println("[US3.1 SCENARIO 2: CHECKOUT & VERLAAT HOTEL - Bob");
                        System.out.println("Given: Bob verblijft in kamer");
                        System.out.println("When: simulator.gastCheckout('Bob') wordt aangeroepen");
                        System.out.println("Then:");
                        System.out.println("  - Bob verlaat het hotel (verwijderd uit simulatie)");
                        System.out.println("  - Kamerstatus gemarkeerd als VIES (SCHOONMAKEN)");
                        System.out.println("=".repeat(80));
                        simulator.gastCheckout("Bob");
                        System.out.println("PASS: Bob verlaten hotel succesvol");
                    }

                    // US3.1 SCENARIO 2: CHECK-OUT - Charlie verlaat hotel (na 15 sec = 300 ticks)
                    if (timestep == 300) {
                        System.out.println("\n" + "=".repeat(80));
                        System.out.println("[US3.1 SCENARIO 2: CHECKOUT & VERLAAT HOTEL - Charlie");
                        System.out.println("=".repeat(80));
                        simulator.gastCheckout("Charlie");
                        System.out.println("PASS: Charlie verlaten hotel succesvol");
                    }
                });
                simulationTimer.start(); // De timer begint met 'lopen' op de achtergrond.

            } catch (Exception e) {
                // Foutopsporing: als er iets misgaat (bijv. bestand niet gevonden), zie je dat in de console.
                System.out.println("Fout: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * getAvailableLayouts:
     * Deze methode kijkt in de map 'layouts' en filtert alle bestanden die eindigen op .json.
     * Zo hoef je de namen van de bestanden niet hardcoded in je code te zetten.
     */
    private static String[] getAvailableLayouts() {
        File layoutDir = new File("layouts");
        String[] layouts = layoutDir.list((dir, name) -> name.endsWith(".json"));
        if (layouts == null || layouts.length == 0) {
            throw new RuntimeException("Fout: Geen .json layouts gevonden in de map.");
        }
        Arrays.sort(layouts); // Zet ze op alfabetische volgorde
        return layouts;
    }

    /**
     * loadLayout:
     * Wordt aangeroepen als je op de 'Laden' knop klikt.
     * Het 'ververst' het huidige hotel met een nieuw bestand.
     */
    private static void loadLayout() {
        try {
            // Pak de geselecteerde naam uit de dropdown
            String selectedLayout = (String) layoutDropdown.getSelectedItem();

            // Gebruik de LayoutLoader om de JSON te vertalen naar een Hotel-object
            Hotel newHotel = LayoutLoader.laadLayout("layouts/" + selectedLayout);

            // === KAMERS AANMAKEN ===
            initializeKamers(newHotel);

            // Geef het nieuwe hotel door aan het HotelPanel zodat de tekening verandert
            hotelPanel.setHotel(newHotel);

            addTestGuests(newHotel); // Zet de gasten weer terug op hun startpositie
            addSchoonmakers(newHotel); // Voeg schoonmakers toe

            // Maak de simulator helemaal opnieuw aan voor het nieuwe hotel.
            simulator = new Simulator(newHotel, hotelPanel);
            simulator.resetClock(); // Belangrijk: De klok moet weer op 0 beginnen.
            simulator.pause(); // Het nieuwe hotel begint altijd gepauzeerd.

            // === LINK LIFT AAN PANEL ===
            if (simulator.getLift() != null) {
                hotelPanel.setLift(simulator.getLift());
            }

            // Reset de Start-knop tekst
            if (startPauseButton != null) {
                startPauseButton.setText("Start");
            }
            statusLabel.setText("Nieuw hotel geladen: " + selectedLayout);

        } catch (Exception e) {
            statusLabel.setText("Fout bij laden: " + e.getMessage());
        }
    }

    /**
     * US3.1: addTestGuests - Gasten met Check-in mogelijkheid
     * De gasten worden geïnitialiseerd en kunnen via simulator.gastCheckin() ingecheckt worden.
     * Scenario 1: Succesvol inchecken van gasten bij aankomst
     * Scenario 2: Gast checkt uit en verlaat het hotel
     */
    private static void addTestGuests(Hotel hotel) {
        // === SCENARIO 1: Alice - Kan in Luxe kamer inchecken ===
        Gast gast1 = new Gast("Alice", 2, 6);
        gast1.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
        gast1.setHotel(hotel);  // Geef gast de hotel areas
        hotel.addPersoon(gast1);

        // === SCENARIO 1: Bob - Kan ook in Luxe kamer inchecken ===
        Gast gast2 = new Gast("Bob", 3, 6);
        gast2.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
        gast2.setHotel(hotel);
        hotel.addPersoon(gast2);

        // === SCENARIO 2: Charlie - Zal inchecken en later uitchecken ===
        Gast gast3 = new Gast("Charlie", 1, 7);
        gast3.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
        gast3.setHotel(hotel);
        hotel.addPersoon(gast3);
        
        System.out.println("[US3.1] ✅ Gasten aangemaakt (initialisatie klaar voor check-in/check-out tests)");
    }

    /**
     * initializeKamers:
     * Maakt kamers aan en koppelt ze aan de bijbehorende Area objecten
     */
    private static void initializeKamers(Hotel hotel) {
        // Zoek alle Room areas in de layout
        java.util.List<Area> roomAreas = new java.util.ArrayList<>();
        for (Area area : hotel.getAreas()) {
            if ("Room".equals(area.getAreaType())) {
                roomAreas.add(area);
            }
        }

        // Maak kamers aan en koppel ze aan areas
        // Voor nu: 4 kamers op vaste nummers
        int[] kamerNummers = {101, 102, 201, 202};
        String[] types = {"Luxe", "Luxe", "Standaard", "Standaard"};

        for (int i = 0; i < kamerNummers.length && i < roomAreas.size(); i++) {
            Kamer kamer = new Kamer(kamerNummers[i], types[i]);
            kamer.setArea(roomAreas.get(i));  // Koppel aan Area

            // TEST: meerdere kamers zijn vuil voor testing schoonmakers
            // Maak eerste 3 kamers viez zodat schoonmakers kunnen spreiden
            if (i < 3) {
                kamer.setStatus(Kamer.KamerStatus.SCHOONMAKEN);
            }

            hotel.addKamer(kamer);
        }
    }

    /**
     * addSchoonmakers:
     * Voegt schoonmakers toe aan de simulatie
     */
    private static void addSchoonmakers(Hotel hotel) {
        // Maak 2 schoonmakers aan op willekeurige startposities
        Schoonmaker schoonmaker1 = new Schoonmaker("Schoonmaker1", 3, 1);
        schoonmaker1.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
        schoonmaker1.setHotel(hotel);
        hotel.addPersoon(schoonmaker1);

        Schoonmaker schoonmaker2 = new Schoonmaker("Schoonmaker2", 4, 1);
        schoonmaker2.setGridBounds(hotel.getBreedte(), hotel.getHoogte());
        schoonmaker2.setHotel(hotel);
        hotel.addPersoon(schoonmaker2);
    }
}

