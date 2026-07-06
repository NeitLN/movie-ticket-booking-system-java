package com.bittersweetcinemas.ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * CUSTOM ROUNDED PANEL CONTAINER
 * -------------------------------------------------------------------------
 * Extends JPanel (OOP: Inheritance).
 * Serves as a modular card-container with custom corner rounding, background fill,
 * and optional border rendering. Ideal for modern flat-card UI dashboards.
 */
public class RoundedPanel extends JPanel {
    // Encapsulated panel parameters (OOP: Encapsulation)
    private int radius;                  // Border radius for corners
    private Color backgroundColor;       // Background solid fill color
    private Color borderColor;           // Optional border outline color

    /**
     * Constructor Overloading 1 (Polymorphism)
     * Creates a borderless rounded panel.
     */
    public RoundedPanel(int radius, Color backgroundColor) {
        this(radius, backgroundColor, null);
    }

    /**
     * Constructor Overloading 2 (Polymorphism)
     * Creates a rounded panel with a custom border outline.
     */
    public RoundedPanel(int radius, Color backgroundColor, Color borderColor) {
        this.radius = radius;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        setOpaque(false);                // Ensure transparency outside the rounded boundary
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Enable high-fidelity anti-aliasing for smooth corner borders
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fill the panel's interior rounded area
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        
        // Paint the border outline if configured
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
        
        g2.dispose(); // Release graphic resource
        
        // Invoke superclass to paint any layout-managed children components on top
        super.paintComponent(g);
    }
}
