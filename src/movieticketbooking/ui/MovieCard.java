package movieticketbooking.ui;

import movieticketbooking.model.Movie;
import javax.swing.*;
import java.awt.*;

/**
 * ULTRA-COMPACT VERTICAL MOVIE CARD (Student 1 - Sidebar-Optimized)
 * -------------------------------------------------------------------------
 * Extends RoundedPanel (OOP: Inheritance).
 * Optimized to 160x275px width to fit exactly 6 movies per row alongside the 240px Left Sidebar.
 * Retains perfect left-alignments, vector-drawn icons, and fits both buttons side-by-side.
 */
public class MovieCard extends RoundedPanel {
    public MovieCard(Movie movie) {
        super(16, Theme.CARD, Theme.BORDER);
        setPreferredSize(new Dimension(160, 275)); // Compact size optimized for 6-per-row with sidebar
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8)); // Thin margins to optimize space

        // NORTH: Movie Poster Area (Dynamically rendered, scaled to fit 160px width)
        PosterPlaceholder poster = new PosterPlaceholder(movie.getPosterPath());
        poster.setPreferredSize(new Dimension(144, 140));
        add(poster, BorderLayout.NORTH);

        // CENTER: Movie Info Panel Stack (BoxLayout Y_AXIS)
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        // Row 1: Title and Age Rating Classification Badge
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT); // Align flush left
        
        JLabel title = new JLabel(movie.getTitle());
        title.setForeground(Theme.CREAM);
        title.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Slightly smaller font for compact width
        
        JLabel badge = new JLabel(movie.getAgeRating());
        Color badgeFg = Theme.RED;
        Color badgeBorder = Theme.RED_DARK;
        if ("P".equalsIgnoreCase(movie.getAgeRating())) {
            badgeFg = new Color(40, 167, 69); // Modern Green
            badgeBorder = new Color(25, 105, 44); // Dark Green
        }
        badge.setForeground(badgeFg);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 8));
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(badgeBorder),
            BorderFactory.createEmptyBorder(1, 4, 1, 4)
        ));
        
        titleRow.add(title, BorderLayout.CENTER);
        titleRow.add(badge, BorderLayout.EAST);
        info.add(titleRow);
        info.add(Box.createVerticalStrut(6));

        // Row 2: Genre
        JLabel genre = new JLabel(movie.getGenre());
        genre.setForeground(Theme.MUTED);
        genre.setFont(Theme.FONT_SMALL);
        genre.setAlignmentX(Component.LEFT_ALIGNMENT); // Align flush left
        info.add(genre);
        info.add(Box.createVerticalStrut(6));

        // Row 3: Meta details (Duration and rating score)
        JPanel meta = new JPanel(new BorderLayout());
        meta.setOpaque(false);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT); // Align flush left
        
        JLabel duration = new JLabel(movie.getDuration() + " phút");
        duration.setForeground(Theme.SOFT);
        duration.setFont(Theme.FONT_SMALL);
        
        JLabel score = new JLabel(" " + movie.getScore());
        score.setIcon(new StarIcon()); // Custom vector StarIcon
        score.setForeground(Theme.GOLD);
        score.setFont(new Font("Segoe UI", Font.BOLD, 10));
        
        meta.add(duration, BorderLayout.WEST);
        meta.add(score, BorderLayout.EAST);
        info.add(meta);
        info.add(Box.createVerticalStrut(10));

        // Row 4: Call-to-action buttons ("Đặt vé" and "Chi tiết")
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT); // Align flush left
        
        // Buttons adjusted to 64px and 68px to fit perfectly within the 144px inner width limit
        RoundedButton book = new RoundedButton("Đặt vé", Theme.RED, new Color(235, 48, 86), Color.WHITE, null);
        book.setPreferredSize(new Dimension(64, 28));
        book.setFont(new Font("Segoe UI", Font.BOLD, 10)); // Compact font size
        
        RoundedButton detail = new RoundedButton("Chi tiết", new Color(30, 25, 22), new Color(47, 38, 30), Theme.GOLD, Theme.BORDER);
        detail.setPreferredSize(new Dimension(68, 28));
        detail.setFont(new Font("Segoe UI", Font.BOLD, 10)); // Compact font size
        
        book.addActionListener(e -> JOptionPane.showMessageDialog(this, "Bạn bấm Đặt vé: " + movie.getTitle()));
        detail.addActionListener(e -> JOptionPane.showMessageDialog(this, "Bạn bấm Chi tiết: " + movie.getTitle()));
        buttons.add(book);
        buttons.add(detail);
        info.add(buttons);

        add(info, BorderLayout.CENTER);
    }
}

/**
 * 100% VECTOR-DRAWN GOLD STAR ICON (Custom Platform-Independent Icon)
 */
class StarIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.GOLD);
        
        int cx = x + 5;
        int cy = y + 5;
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];
        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI / 5 - Math.PI / 2;
            double r = (i % 2 == 0) ? 5 : 2.0; // Scaled specifically to fit 10x10 area inside compact card
            xPoints[i] = (int) (cx + r * Math.cos(angle));
            yPoints[i] = (int) (cy + r * Math.sin(angle));
        }
        g2.fillPolygon(xPoints, yPoints, 10);
        g2.dispose();
    }

    @Override public int getIconWidth() { return 10; }
    @Override public int getIconHeight() { return 10; }
}
