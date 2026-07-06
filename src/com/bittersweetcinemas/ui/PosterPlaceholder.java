package com.bittersweetcinemas.ui;

import javax.swing.JComponent;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import javax.imageio.ImageIO;

public class PosterPlaceholder extends JComponent {
    private Image posterImage = null;

    public PosterPlaceholder() {
        this(null);
    }

    public PosterPlaceholder(String posterPath) {
        if (posterPath != null && !posterPath.trim().isEmpty()) {
            try {
                File file = new File(posterPath);
                if (file.exists()) {
                    posterImage = ImageIO.read(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        // Rounded rectangle for matching border radius
        RoundRectangle2D roundedRect = new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 14, 14);

        if (posterImage != null) {
            // Draw image clipped to rounded corners
            g2.setClip(roundedRect);
            g2.drawImage(posterImage, 0, 0, w, h, this);
            g2.setClip(null); // Restore clip

            // Draw border
            g2.setColor(Theme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
        } else {
            // Fallback default empty placeholder
            g2.setColor(Theme.CARD_2);
            g2.fill(roundedRect);
            g2.setColor(Theme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

            int cx = w / 2;
            int cy = h / 2 - 10;
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
        }
        g2.dispose();
    }
}
