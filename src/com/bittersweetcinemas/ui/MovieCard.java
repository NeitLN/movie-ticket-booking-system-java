package com.bittersweetcinemas.ui;

import javax.swing.*;
import java.awt.*;

public class MovieCard extends RoundedPanel {
    public MovieCard(Movie movie) {
        super(16, Theme.CARD, Theme.BORDER);
        setPreferredSize(new Dimension(200, 315));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));

        PosterPlaceholder poster = new PosterPlaceholder(movie.getPosterPath());
        poster.setPreferredSize(new Dimension(184, 170));
        add(poster, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT); // Explicitly align left in BoxLayout
        
        JLabel title = new JLabel(movie.getTitle());
        title.setForeground(Theme.CREAM);
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JLabel badge = new JLabel(movie.getRating());
        Color badgeFg = Theme.RED;
        Color badgeBorder = Theme.RED_DARK;
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

        JLabel genre = new JLabel(movie.getGenre());
        genre.setForeground(Theme.MUTED);
        genre.setFont(Theme.FONT_SMALL);
        genre.setAlignmentX(Component.LEFT_ALIGNMENT); // Explicitly align left in BoxLayout
        info.add(genre);
        info.add(Box.createVerticalStrut(8));

        JPanel meta = new JPanel(new BorderLayout());
        meta.setOpaque(false);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT); // Explicitly align left in BoxLayout
        
        JLabel duration = new JLabel(movie.getDuration() + " phút");
        duration.setForeground(Theme.SOFT);
        duration.setFont(Theme.FONT_SMALL);
        
        JLabel score = new JLabel(" " + movie.getScore());
        score.setIcon(new StarIcon()); // Vector gold star instead of font-glitched character
        score.setForeground(Theme.GOLD);
        score.setFont(new Font("Segoe UI", Font.BOLD, 11));
        
        meta.add(duration, BorderLayout.WEST);
        meta.add(score, BorderLayout.EAST);
        info.add(meta);
        info.add(Box.createVerticalStrut(12));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT); // Explicitly align left in BoxLayout
        
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

// 100% Vector-drawn Star Icon to prevent square tofu symbol glitches on any machine
class StarIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.GOLD);
        
        int cx = x + 6;
        int cy = y + 6;
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];
        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI / 5 - Math.PI / 2;
            double r = (i % 2 == 0) ? 6 : 2.5;
            xPoints[i] = (int) (cx + r * Math.cos(angle));
            yPoints[i] = (int) (cy + r * Math.sin(angle));
        }
        g2.fillPolygon(xPoints, yPoints, 10);
        g2.dispose();
    }

    @Override public int getIconWidth() { return 12; }
    @Override public int getIconHeight() { return 12; }
}
