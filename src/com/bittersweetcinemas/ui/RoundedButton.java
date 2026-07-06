package com.bittersweetcinemas.ui;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * CUSTOM ROUNDED BUTTON COMPONENT
 * -------------------------------------------------------------------------
 * Extends JButton (OOP: Inheritance).
 * Implements a premium, modern flat-design button with smooth rounded corners,
 * mouse-hover fade highlights, and optional border styling.
 */
public class RoundedButton extends JButton {
    // Encapsulated button attributes (OOP: Encapsulation)
    private final int radius;             // Border radius for corner rounding
    private final Color normalColor;      // Background color in default state
    private final Color hoverColor;       // Background color on mouse hover
    private final Color borderColor;     // Optional outline border color (null for borderless)
    private boolean hovered;              // State tracker for mouse hover events

    /**
     * Constructor to initialize custom button colors, border, and state adapters.
     */
    public RoundedButton(String text, Color bg, Color hover, Color fg, Color border) {
        super(text);
        this.radius = 10;                 // Modern 10px corner rounding radius
        this.normalColor = bg;
        this.hoverColor = hover;
        this.borderColor = border;
        
        // Configure standard Swing button behaviors
        setForeground(fg);
        setFont(Theme.FONT_BOLD);
        setFocusPainted(false);           // Turn off standard dotted focus ring
        setBorderPainted(false);          // Turn off old rigid border rendering
        setContentAreaFilled(false);      // Disable default rectangular background filling
        setOpaque(false);                 // Enable transparent corners outside the rounded rect
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Hand pointer on hover
        
        // Register an event listener (Observer Pattern) to dynamically trigger hover state repaints
        addMouseListener(new MouseAdapter() {
            @Override 
            public void mouseEntered(MouseEvent e) { 
                hovered = true; 
                repaint(); // Forces EDT to invoke paintComponent()
            }
            @Override 
            public void mouseExited(MouseEvent e) { 
                hovered = false; 
                repaint(); // Forces EDT to invoke paintComponent()
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Enable high-fidelity vector anti-aliasing for smooth rounded corners
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fill the body of the rounded button depending on the current hovered state
        g2.setColor(hovered ? hoverColor : normalColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        
        // Render the optional border outline if configured
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
        
        g2.dispose(); // Release graphic context resource
        
        // Delegate to superclass to paint the foreground text/label nicely inside the button
        super.paintComponent(g);
    }
}
