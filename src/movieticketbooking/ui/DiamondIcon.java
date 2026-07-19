package movieticketbooking.ui;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class DiamondIcon implements Icon {
    private final Color color;

    public DiamondIcon(Color color) {
        this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        int cx = x + 4;
        int cy = y + 7;
        int[] xp = { cx, cx + 4, cx, cx - 4 };
        int[] yp = { cy - 4, cy, cy + 4, cy };
        g2.fillPolygon(xp, yp, 4);
        g2.dispose();
    }

    @Override public int getIconWidth() { return 10; }
    @Override public int getIconHeight() { return 14; }
}
