package ui;

import model.Kamer;

import javax.swing.*;
import java.awt.*;

/**
 * Detail-venster voor kamer-informatie
 * Toont status, type, kamernummer en wie er verblijft
 */
public class RoomDetailWindow extends JFrame {

    private Kamer kamer;

    public RoomDetailWindow(Kamer kamer) {
        this.kamer = kamer;

        setTitle("🚪 Kamer Details - " + getKamerLabel(kamer));
        setSize(400, 300);
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
        JLabel titleLabel = new JLabel("🚪 Kamer " + kamer.getKamernummer());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Info-velden
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        infoPanel.setBackground(new Color(245, 245, 245));

        // Kamernummer
        addInfoRow(infoPanel, "Kamernummer:", String.valueOf(kamer.getKamernummer()));

        // Type
        addInfoRow(infoPanel, "Type:", kamer.getType());

        // Status
        String statusEmoji = getStatusEmoji(kamer.getStatus());
        addInfoRow(infoPanel, "Status:", statusEmoji + " " + kamer.getStatus().toString());

        // Positie (area)
        if (kamer.getArea() != null) {
            String position = String.format("X: %d, Y: %d", kamer.getArea().getX(), kamer.getArea().getY());
            addInfoRow(infoPanel, "Positie:", position);
        }

        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Beschrijving van status
        JLabel descLabel = new JLabel(getStatusDescription(kamer.getStatus()));
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
        labelComp.setPreferredSize(new Dimension(100, 20));

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Arial", Font.PLAIN, 12));

        rowPanel.add(labelComp);
        rowPanel.add(valueComp);
        panel.add(rowPanel);
    }

    private String getKamerLabel(Kamer k) {
        return k.getKamernummer() + " (" + k.getType() + ")";
    }

    private String getStatusEmoji(Kamer.KamerStatus status) {
        switch (status) {
            case VRIJ: return "✅";
            case BEZET: return "🔒";
            case SCHOONMAKEN: return "🧹";
            default: return "";
        }
    }

    private String getStatusDescription(Kamer.KamerStatus status) {
        switch (status) {
            case VRIJ: return "Kamer is schoon en beschikbaar voor gasten.";
            case BEZET: return "Kamer is momenteel door een gast bewoond.";
            case SCHOONMAKEN: return "Schoonmaker is deze kamer aan het schoonmaken.";
            default: return "";
        }
    }
}

