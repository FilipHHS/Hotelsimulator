package ui;

import model.EventBusImpl;
import hotelevents.HotelEventType;

import javax.swing.*;
import java.awt.*;

/**
 * Debug-venster om alle HotelEvents real-time te zien
 */
public class EventDebugWindow extends JFrame {
    private JTextArea eventLog;
    private EventBusImpl eventBus;

    public EventDebugWindow(EventBusImpl eventBus) {
        this.eventBus = eventBus;

        setTitle("🎬 Hotel Event Monitor");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Real-time HotelEvent Log");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(title, BorderLayout.NORTH);

        eventLog = new JTextArea();
        eventLog.setEditable(false);
        eventLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        eventLog.setBackground(new Color(30, 30, 30));
        eventLog.setForeground(new Color(0, 255, 0));

        JScrollPane scrollPane = new JScrollPane(eventLog);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton clearButton = new JButton("Clear Log");
        clearButton.addActionListener(_ -> {
            eventLog.setText("");
            if (eventBus != null) eventBus.clearEventLog();
        });
        panel.add(clearButton, BorderLayout.SOUTH);

        add(panel);

        // Update every 500ms
        Timer updateTimer = new Timer(500, _ -> refreshLog());
        updateTimer.start();
    }

    private void refreshLog() {
        if (eventBus == null) return;

        java.util.List<String> logs = eventBus.getEventLog();
        StringBuilder sb = new StringBuilder();

        // Show last 20 events
        int start = Math.max(0, logs.size() - 20);
        for (int i = start; i < logs.size(); i++) {
            sb.append(logs.get(i)).append("\n");
        }

        eventLog.setText(sb.toString());
        eventLog.setCaretPosition(eventLog.getDocument().getLength());
    }
}

