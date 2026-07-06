package com.bittersweetcinemas.ui;

import javax.swing.*;
import java.awt.*;

/**
 * MOVIE CARD PANEL COMPONENT
 * -------------------------------------------------------------------------
 * Extends RoundedPanel (OOP: Inheritance).
 * Represents a clean, vertical, cinema-themed info-card for a movie.
 * Uses a BorderLayout root containing a BoxLayout vertical stack for details.
 */
public class MovieCard extends RoundedPanel {
    public MovieCard(Movie movie) {
        super(16, Theme.CARD, Theme.BORDER);
        setPreferredSize(new Dimension(200, 315));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8)); // Thin margins to optimize space

        // NORTH: Movie Poster Area (Dynamically rendered)
        PosterPlaceholder poster = new PosterPlaceholder(movie.getPosterPath());
        poster.setPreferredSize(new Dimension(184, 170));
        add(poster, BorderLayout.NORTH);

        // CENTER: Movie Info Panel Stack (BoxLayout Y_AXIS)
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        // Rows in a vertical BoxLayout align by their default "alignmentX" values.
        // JPanels default to 0.5 (center) and JLabels to 0.0 (left), creating offset gaps.
        // We set Component.LEFT_ALIGNMENT (0.0f) on all child panels/labels to keep them perfectly aligned.

        // Row 1: Title and Age Rating Classification Badge
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT); // Align flush left
        
        JLabel title = new JLabel(movie.getTitle());
        title.setForeground(Theme.CREAM);
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JLabel badge = new JLabel(movie.getRating());
        Color badgeFg = Theme.RED;
        Color badgeBorder = Theme.RED_DARK;
        // Dynamic visual coloring (Green for P - General Audience, Red/Pink for age restrictions)
        if ("P".equalsIgnoreCase(movie.getRating())) {
            badgeFg = new Color(40, 167, 69); // Modern Green
            badgeBorder = new Color(25, 105, 44); // Dark Green
        }
        badge.setForeground(badgeFg);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(badgeBorder),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        
        titleRow.add(title, BorderLayout.CENTER);
        titleRow.add(badge, BorderLayout.EAST);
        info.add(titleRow);
        info.add(Box.createVerticalStrut(8));

        // Row 2: Genre
        JLabel genre = new JLabel(movie.getGenre());
        genre.setForeground(Theme.MUTED);
        genre.setFont(Theme.FONT_SMALL);
        genre.setAlignmentX(Component.LEFT_ALIGNMENT); // Align flush left
        info.add(genre);
        info.add(Box.createVerticalStrut(8));

        // Row 3: Meta details (Duration and rating score)
        JPanel meta = new JPanel(new BorderLayout());
        meta.setOpaque(false);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT); // Align flush left
        
        JLabel duration = new JLabel(movie.getDuration() + " phút");
        duration.setForeground(Theme.SOFT);
        duration.setFont(Theme.FONT_SMALL);
        
        JLabel score = new JLabel(" " + movie.getScore());
        score.setIcon(new StarIcon()); // Uses our custom-drawn vector StarIcon (100% font-bug safe)
        score.setForeground(Theme.GOLD);
        score.setFont(new Font("Segoe UI", Font.BOLD, 11));
        
        meta.add(duration, BorderLayout.WEST);
        meta.add(score, BorderLayout.EAST);
        info.add(meta);
        info.add(Box.createVerticalStrut(12));

        // Row 4: Call-to-action buttons ("Đặt vé" and "Chi tiết")
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT); // Align flush left
        
        // Buttons set to wider sizes (80px and 84px) with tightly packed FlowLayout gaps to fully display Vietnamese text
        RoundedButton book = new RoundedButton("Đặt vé", Theme.RED, new Color(235, 48, 86), Color.WHITE, null);
        book.setPreferredSize(new Dimension(80, 32));
        RoundedButton detail = new RoundedButton("Chi tiết", new Color(30, 25, 22), new Color(47, 38, 30), Theme.GOLD, Theme.BORDER);
        detail.setPreferredSize(new Dimension(84, 32));
        
        book.addActionListener(e -> JOptionPane.showMessageDialog(this, "Bạn bấm Đặt vé: " + movie.getTitle()));
        detail.addActionListener(e -> JOptionPane.showMessageDialog(this, "Bạn bấm Chi tiết: " + movie.getTitle()));
        buttons.add(book);
        buttons.add(detail);
        info.add(buttons);

        add(info, BorderLayout.CENTER);
    }
}

/**
 * 100% VECTOR-DRAWN GOLD STAR ICON
 * -------------------------------------------------------------------------
 * Implements javax.swing.Icon (OOP: Interface Abstraction).
 * Standard Unicode rating star symbols ('★') show up as empty hollow squares (tofu blocks)
 * on systems with incomplete font engines. This class bypasses the OS font engine entirely
 * by drawing a smooth, anti-aliased vector star polygon directly using geometric coordinates.
 */
class StarIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.GOLD);
        
        // Geometric calculations for centered 10-point star polygon
        int cx = x + 6;
        int cy = y + 6;
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];
        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI / 5 - Math.PI / 2;
            double r = (i % 2 == 0) ? 6 : 2.5; // Alternating outer (6px) and inner (2.5px) radius
            xPoints[i] = (int) (cx + r * Math.cos(angle));
            yPoints[i] = (int) (cy + r * Math.sin(angle));
        }
        g2.fillPolygon(xPoints, yPoints, 10);
        g2.dispose();
    }

    @Override public int getIconWidth() { return 12; }
    @Override public int getIconHeight() { return 12; }
}
