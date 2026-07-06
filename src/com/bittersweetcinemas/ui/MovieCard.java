package com.bittersweetcinemas.ui;

import javax.swing.*;
import java.awt.*;

public class MovieCard extends RoundedPanel {
    public MovieCard(Movie movie) {
        super(16, Theme.CARD, Theme.BORDER);
        setPreferredSize(new Dimension(200, 315));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        PosterPlaceholder poster = new PosterPlaceholder();
        poster.setPreferredSize(new Dimension(176, 170));
        add(poster, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
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
        info.add(genre);
        info.add(Box.createVerticalStrut(8));

        JPanel meta = new JPanel(new BorderLayout());
        meta.setOpaque(false);
        JLabel duration = new JLabel(movie.getDuration() + " phút");
        duration.setForeground(Theme.SOFT);
        duration.setFont(Theme.FONT_SMALL);
        JLabel score = new JLabel("★ " + movie.getScore());
        score.setForeground(Theme.GOLD);
        score.setFont(new Font("Segoe UI", Font.BOLD, 11));
        meta.add(duration, BorderLayout.WEST);
        meta.add(score, BorderLayout.EAST);
        info.add(meta);
        info.add(Box.createVerticalStrut(12));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        RoundedButton book = new RoundedButton("Đặt vé", Theme.RED, new Color(235, 48, 86), Color.WHITE, null);
        book.setPreferredSize(new Dimension(70, 32));
        RoundedButton detail = new RoundedButton("Chi tiết", new Color(30, 25, 22), new Color(47, 38, 30), Theme.GOLD, Theme.BORDER);
        detail.setPreferredSize(new Dimension(74, 32));
        book.addActionListener(e -> JOptionPane.showMessageDialog(this, "Bạn bấm Đặt vé: " + movie.getTitle()));
        detail.addActionListener(e -> JOptionPane.showMessageDialog(this, "Bạn bấm Chi tiết: " + movie.getTitle()));
        buttons.add(book);
        buttons.add(detail);
        info.add(buttons);

        add(info, BorderLayout.CENTER);
    }
}
