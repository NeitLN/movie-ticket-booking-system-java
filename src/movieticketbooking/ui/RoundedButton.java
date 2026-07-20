package movieticketbooking.ui;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoundedButton extends JButton {
    private final int radius;
    private final Color normalColor;
    private final Color hoverColor;
    private final Color borderColor;
    private boolean hovered;

    public RoundedButton(String text, Color bg, Color hover, Color fg, Color border) {
        super(text);
        this.radius = 10;
        this.normalColor = bg;
        this.hoverColor = hover;
        this.borderColor = border;
        
        setForeground(fg);
        setFont(Theme.FONT_BOLD);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        addMouseListener(new MouseAdapter() {
            @Override 
            public void mouseEntered(MouseEvent e) { 
                if (isEnabled()) {
                    hovered = true;
                    repaint();
                }
            }
            @Override 
            public void mouseExited(MouseEvent e) { 
                hovered = false; 
                repaint();
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        hovered = false;
        setCursor(enabled
            ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            : Cursor.getDefaultCursor());
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fillColor = isEnabled()
            ? (hovered ? hoverColor : normalColor)
            : blend(normalColor, Theme.BG, 0.68f);
        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

        if (borderColor != null) {
            g2.setColor(isEnabled() ? borderColor : blend(borderColor, Theme.BG, 0.62f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
        g2.dispose();
        super.paintComponent(g);
    }

    private static Color blend(Color source, Color target, float targetWeight) {
        float weight = Math.max(0f, Math.min(1f, targetWeight));
        int red = Math.round(source.getRed() * (1f - weight) + target.getRed() * weight);
        int green = Math.round(source.getGreen() * (1f - weight) + target.getGreen() * weight);
        int blue = Math.round(source.getBlue() * (1f - weight) + target.getBlue() * weight);
        return new Color(red, green, blue);
    }
}
