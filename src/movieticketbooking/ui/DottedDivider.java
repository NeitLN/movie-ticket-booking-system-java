package movieticketbooking.ui;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class DottedDivider extends JComponent {
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int y = getHeight() / 2;
        Color[] colors = {
            Theme.RED,
            new Color(255, 110, 130),
            Theme.RED_DARK,
            Theme.GOLD,
            Theme.CREAM,
            Theme.MUTED
        };
        int index = 0;
        for (int x = 0; x < getWidth(); x += 10) {
            g2.setColor(colors[index % colors.length]);
            g2.fillOval(x, y - 2, 4, 4);
            index++;
        }
        g2.dispose();
    }
}
