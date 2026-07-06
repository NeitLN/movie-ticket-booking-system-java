package com.bittersweetcinemas.ui;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * CUSTOM DOTTED DIVIDER COMPONENT
 * -------------------------------------------------------------------------
 * Inherits from JComponent (OOP: Inheritance).
 * Paints a beautiful horizontal row of decorative, theater marquee-style dotted lights.
 * Utilizes custom AWT painting to cycle colors smoothly across the width.
 */
public class DottedDivider extends JComponent {
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Enable anti-aliasing for smooth, round, non-pixelated dot rendering
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int y = getHeight() / 2;
        
        // Theme-aligned colors representing neon theater marquee lights
        Color[] colors = {
            Theme.RED,
            new Color(255, 110, 130), // Light coral red/pink
            Theme.RED_DARK,
            Theme.GOLD,
            Theme.CREAM,
            Theme.MUTED
        };
        
        int index = 0;
        // Loop horizontally and draw vector circle dots with alternating colors
        for (int x = 0; x < getWidth(); x += 10) {
            g2.setColor(colors[index % colors.length]);
            g2.fillOval(x, y - 2, 4, 4);
            index++;
        }
        
        g2.dispose(); // Release system graphics context resource
    }
}
