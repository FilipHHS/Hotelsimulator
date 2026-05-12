package ui;

import model.Gast;
import model.Hotel;
import model.Persoon;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * US2.1: Guest Overview Window
 * Toont een venster met alle gasten die momenteel in het hotel verblijven
 * met relevante statusinformatie (naam, positie, status, huidige activiteit, kamer)
 */
public class GuestListWindow extends JFrame {
    
    private Hotel hotel;
    private JTable guestTable;
    private DefaultTableModel tableModel;
    
    public GuestListWindow(Hotel hotel) {
        this.hotel = hotel;
        
        setTitle("🏨 Gastenoverzicht - Hotel Simulator");
        setSize(900, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        
        initializeUI();
        updateGuestList();
    }
    
    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // === TITEL ===
        JLabel titleLabel = new JLabel("📋 Status-overzicht van alle gasten");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // === TABEL SETUP ===
        String[] columnNames = {"#", "Naam", "X", "Y", "Huidge Kamer", "Activiteit", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Read-only table
            }
        };
        
        guestTable = new JTable(tableModel);
        guestTable.setRowHeight(25);
        guestTable.setFont(new Font("Arial", Font.PLAIN, 11));
        guestTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        guestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        guestTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(guestTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // === ONDERKANT PANEL (STATISTIEKEN) ===
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel statsLabel = new JLabel();
        updateStatsLabel(statsLabel);
        bottomPanel.add(statsLabel);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // === REFRESH BUTTON ===
        JButton refreshButton = new JButton("🔄 Vernieuwen");
        refreshButton.addActionListener(e -> {
            updateGuestList();
            updateStatsLabel(statsLabel);
        });
        bottomPanel.add(refreshButton);
        
        add(mainPanel);
    }
    
    /**
     * Bijwerkt de gastentabel met de huidge gasten in het hotel
     */
    private void updateGuestList() {
        // Maak tabel leeg
        tableModel.setRowCount(0);
        
        // Haal alle gasten op
        List<Gast> gasten = new ArrayList<>();
        for (Persoon p : hotel.getPersonen()) {
            if (p instanceof Gast) {
                gasten.add((Gast) p);
            }
        }
        
        // Voeg rijen toe voor elke gast
        int rowNumber = 1;
        for (Gast gast : gasten) {
            Object[] rowData = {
                rowNumber++,
                gast.getNaam(),
                String.format("%.1f", gast.getX()),
                String.format("%.1f", gast.getY()),
                gast.getHuidigKamer() != null ? "Kamer " + gast.getHuidigKamer().getKamernummer() : "-",
                gast.getHuidigeActiviteit(),
                getGastStatus(gast)
            };
            tableModel.addRow(rowData);
        }
    }
    
    /**
     * Bepaalt de status van een gast op basis van zijn huidge situatie
     */
    private String getGastStatus(Gast gast) {
        if (gast.getX() < 0) {
            return "Vertrokken ❌";
        } else if (gast.getHuidigKamer() != null) {
            return "In Kamer 🛏️";
        } else {
            String activiteit = gast.getHuidigeActiviteit();
            if (activiteit.contains("Lift")) {
                return "In Lift 🛗";
            } else if (activiteit.contains("Evacu")) {
                return "EVACUATIE 🔥";
            } else if (activiteit.contains("Sport")) {
                return "Fitness 💪";
            } else if (activiteit.contains("Eet")) {
                return "Restaurant 🍽️";
            } else if (activiteit.contains("Vertrekt")) {
                return "Check-out ✓";
            } else {
                return "Wandelt 🚶";
            }
        }
    }
    
    /**
     * Update het statistics label met aantal gasten
     */
    private void updateStatsLabel(JLabel label) {
        long gastenCount = hotel.getPersonen().stream()
            .filter(p -> p instanceof Gast)
            .count();
        label.setText("📊 Totaal gasten in hotel: " + gastenCount);
        label.setFont(new Font("Arial", Font.BOLD, 12));
    }
}


