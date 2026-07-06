package com.bittersweetcinemas.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * APPLICATION ENTRY POINT
 * -------------------------------------------------------------------------
 * This is the main class containing the application's executable static main method.
 * Handles platform look-and-feel (L&F) initialization and starts the UI on the 
 * Event Dispatch Thread (EDT) for thread-safe Swing execution.
 */
public class Main {
    /**
     * Main execution method.
     */
    public static void main(String[] args) {
        // Enforce UTF-8 file encoding to guarantee accented Vietnamese text displays correctly
        System.setProperty("file.encoding", "UTF-8");
        
        try {
            // Set the system look and feel to native OS styling for integrated window decorations
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Graceful exception handling: ignore custom L&F errors and fallback to standard Java Metal L&F
        }
        
        // Swing GUI components are not thread-safe. They must be constructed and modified
        // strictly on the Event Dispatch Thread (EDT). invokeLater posts this task to the EDT queue.
        SwingUtilities.invokeLater(() -> new HomeFrame().setVisible(true));
    }
}
