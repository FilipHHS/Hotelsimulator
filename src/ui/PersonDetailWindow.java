package ui;

import model.Gast;
import model.Persoon;
import model.Schoonmaker;

import javax.swing.*;
import java.awt.*;

/**
 * Detail-venster voor persoon-informatie
 * Toont naam, type, activiteit, positie en huidge kamer (voor gasten)
 */
public class PersonDetailWindow extends JFrame {

    private Persoon persoon;

    public PersonDetailWindow(Persoon persoon) {
        this.persoon = persoon;

        String icon = getPersonIcon(persoon);
        setTitle(icon + " Details - " + persoon.getNaam());
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);

        initializeUI();
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Titel
        String icon = getPersonIcon(persoon);
        JLabel titleLabel = new JLabel(icon + " " + persoon.getNaam());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Info-velden
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        infoPanel.setBackground(new Color(245, 245, 245));

        // Naam
        addInfoRow(infoPanel, "Naam:", persoon.getNaam());

        // Type
        String typeLabel = (persoon instanceof Gast) ? "👤 Gast" : "👷 Schoonmaker";
        addInfoRow(infoPanel, "Type:", typeLabel);

        // Huidge Activiteit
        String activiteit = persoon.getHuidigeActiviteit();
        if (activiteit == null || activiteit.isEmpty()) {
            activiteit = "Onbekend";
        }
        addInfoRow(infoPanel, "Activiteit:", activiteit);

        // Positie
        String position = String.format("X: %.1f, Y: %.1f", persoon.getX(), persoon.getY());
        addInfoRow(infoPanel, "Positie:", position);

        // Extra info voor gasten
        if (persoon instanceof Gast) {
            Gast gast = (Gast) persoon;

            // Huidge Kamer
            String kamerInfo = (gast.getHuidigKamer() != null)
                ? "Kamer " + gast.getHuidigKamer().getKamernummer()
                : "Geen kamer";
            addInfoRow(infoPanel, "Huidge Kamer:", kamerInfo);

            // Evacuatie status
            if (gast.isFireAlarmActive()) {
                addInfoRow(infoPanel, "Brand-alarm:", "🔥 ACTIEF - Aan het evacueren!");
            }
        }

        // Extra info voor schoonmakers
        if (persoon instanceof Schoonmaker) {
            Schoonmaker schoonmaker = (Schoonmaker) persoon;
            addInfoRow(infoPanel, "Rol:", "👷 Schoonmaak personeelslid");
        }

        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Status beschrijving
        JLabel descLabel = new JLabel("<html>" + getActivityDescription(persoon) + "</html>");
        descLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        descLabel.setForeground(Color.DARK_GRAY);
        mainPanel.add(descLabel);

        mainPanel.add(Box.createVerticalGlue());

        // Sluit-knop
        JButton closeButton = new JButton("Sluiten");
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(e -> dispose());
        mainPanel.add(closeButton);

        add(mainPanel);
    }

    private void addInfoRow(JPanel panel, String label, String value) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rowPanel.setBackground(new Color(245, 245, 245));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.BOLD, 12));
        labelComp.setPreferredSize(new Dimension(120, 20));

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Arial", Font.PLAIN, 12));
        valueComp.setMaximumSize(new Dimension(300, 30));

        rowPanel.add(labelComp);
        rowPanel.add(valueComp);
        panel.add(rowPanel);
    }

    private String getPersonIcon(Persoon p) {
        if (p instanceof Gast) {
            return "👤";
        } else if (p instanceof Schoonmaker) {
            return "👷";
        }
        return "👥";
    }

    private String getActivityDescription(Persoon p) {
        String activiteit = p.getHuidigeActiviteit();

        if (activiteit == null || activiteit.isEmpty()) {
            return "Geen activiteit op dit moment.";
        }

        if (activiteit.contains("Wandel")) {
            return "Deze persoon loopt door het hotel.";
        } else if (activiteit.contains("Lift")) {
            return "Deze persoon zit momenteel in de lift.";
        } else if (activiteit.contains("Eet") || activiteit.contains("Restaurant")) {
            return "Deze persoon geniet van het restaurant.";
        } else if (activiteit.contains("Sport") || activiteit.contains("Fitness")) {
            return "Deze persoon traint in de fitnessruimte.";
        } else if (activiteit.contains("Chill") || activiteit.contains("bed")) {
            return "Deze persoon ontspant in de kamer.";
        } else if (activiteit.contains("Schoon")) {
            return "Schoonmaker is kamers aan het schoonmaken.";
        } else if (activiteit.contains("EVACUATIE") || activiteit.contains("Evacu")) {
            return "🔥 NOOD: Deze persoon is aan het evacueren!";
        } else if (activiteit.contains("Vertrekt")) {
            return "Deze persoon verlaat het hotel.";
        }

        return "Activiteit: " + activiteit;
    }
}


