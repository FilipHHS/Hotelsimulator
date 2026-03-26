import model.Hotel;
import model.LayoutLoader;
import ui.HotelPanel;
import javax.swing.*;
import java.io.File;
import java.util.Arrays;

/**
 * Startpunt van de Hotel Simulator applicatie.
 * Maakt het hoofdvenster aan en beheert het wisselen tussen layouts.
 */
public class Main {

    // Het paneel waarop het hotel visueel wordt weergegeven
    private static HotelPanel hotelPanel;

    // Dropdown menu met beschikbare layout bestanden
    private static JComboBox<String> layoutDropdown;

    // Label dat de huidige status weergeeft
    private static JLabel statusLabel;

    /**
     * Startmethode van de applicatie.
     * Bouwt de GUI op en laadt de eerste beschikbare layout.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Maak het hoofdvenster aan
                JFrame frame = new JFrame("Hotel Simulator");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // Bovenpaneel met bedieningselementen
                JPanel controlPanel = new JPanel();
                controlPanel.add(new JLabel("Selecteer layout: "));

                // Vul de dropdown met beschikbare JSON layouts
                String[] layouts = getAvailableLayouts();
                layoutDropdown = new JComboBox<>(layouts);
                controlPanel.add(layoutDropdown);

                // Knop om de geselecteerde layout in te laden
                JButton loadButton = new JButton("Laden");
                loadButton.addActionListener(e -> loadLayout());
                controlPanel.add(loadButton);

                // Statuslabel toont welke layout actief is
                statusLabel = new JLabel("Hotel laden...");
                controlPanel.add(statusLabel);

                // Laad de eerste layout bij opstarten
                Hotel hotel = LayoutLoader.laadLayout("layouts/" + layouts[0]);
                hotelPanel = new HotelPanel(hotel);
                statusLabel.setText("Hotel geladen: " + layouts[0]);

                // Voeg panelen toe aan het venster
                frame.add(controlPanel, "North");
                frame.add(hotelPanel, "Center");

                // Centreer het venster op het scherm
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

            } catch (Exception e) {
                System.out.println("Fout: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Zoekt alle JSON layoutbestanden in de layouts/ map.
     * Gooit een fout als er geen bestanden gevonden worden.
     */
    private static String[] getAvailableLayouts() {
        File layoutDir = new File("layouts");
        String[] layouts = layoutDir.list((dir, name) -> name.endsWith(".json"));

        // Controleer of er layouts gevonden zijn
        if (layouts == null || layouts.length == 0) {
            throw new RuntimeException("Geen layout bestanden gevonden in layouts/ map");
        }

        // Sorteer alfabetisch zodat de volgorde consistent is
        Arrays.sort(layouts);
        return layouts;
    }

    /**
     * Laadt de layout die geselecteerd is in de dropdown.
     * Vervangt het huidige hotel zonder de applicatie te herstarten.
     */
    private static void loadLayout() {
        try {
            String selectedLayout = (String) layoutDropdown.getSelectedItem();

            // Laad het nieuwe hotel en update het paneel
            Hotel newHotel = LayoutLoader.laadLayout("layouts/" + selectedLayout);
            hotelPanel.setHotel(newHotel);
            statusLabel.setText("Hotel geladen: " + selectedLayout);

        } catch (Exception e) {
            // Toon foutmelding in het statuslabel
            statusLabel.setText("Fout: " + e.getMessage());
            System.out.println("Fout bij laden: " + e.getMessage());
        }
    }
}