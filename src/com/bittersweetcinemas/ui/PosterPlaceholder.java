package com.bittersweetcinemas.ui;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

public class PosterPlaceholder extends JComponent {
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Theme.CARD_2);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
        g2.setColor(Theme.BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

        int cx = getWidth() / 2;
        int cy = getHeight() / 2 - 10;
        g2.setColor(Theme.RED_DARK);
        g2.fillOval(cx - 24, cy - 24, 48, 48);
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(Theme.GOLD);
        g2.drawOval(cx - 24, cy - 24, 48, 48);

        g2.setColor(Theme.GOLD);
        g2.draw(new RoundRectangle2D.Double(cx - 10, cy - 12, 20, 24, 4, 4));
        g2.drawLine(cx - 10, cy - 4, cx + 10, cy - 4);
        g2.drawLine(cx - 10, cy + 4, cx + 10, cy + 4);
        g2.drawLine(cx - 3, cy - 12, cx - 3, cy + 12);
        g2.drawLine(cx + 4, cy - 12, cx + 4, cy + 12);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2.setColor(Theme.SOFT);
        String text = "POSTER";
        int tw = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, cx - tw / 2, cy + 48);
        g2.dispose();
    }
}
