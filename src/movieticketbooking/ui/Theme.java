package movieticketbooking.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JComponent;
import javax.swing.BorderFactory;

public final class Theme {
    private Theme() {}

    public static final Color BG = new Color(20, 17, 16);
    public static final Color BG_2 = new Color(26, 22, 20);
    public static final Color TOP_BAR = new Color(14, 12, 11);
    public static final Color NAV = new Color(18, 16, 15);
    public static final Color CARD = new Color(33, 25, 23);
    public static final Color CARD_2 = new Color(39, 29, 26);
    public static final Color BORDER = new Color(69, 54, 42);
    public static final Color RED = new Color(215, 31, 70);
    public static final Color RED_DARK = new Color(133, 24, 44);
    public static final Color GOLD = new Color(235, 184, 56);
    public static final Color CREAM = new Color(255, 241, 217);
    public static final Color MUTED = new Color(174, 143, 111);
    public static final Color SOFT = new Color(117, 95, 76);

    public static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_TITLE = new Font("Georgia", Font.BOLD, 42);
    public static final Font FONT_LOGO = new Font("Georgia", Font.BOLD, 16);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 18);

    /**
     * Unified, platform-independent combobox styler.
     * Sets the main closed box to WHITE with BLACK text for perfect readability,
     * while keeping the opened dropdown popup list dark to match the cinematic theme.
     */
    public static void styleCombo(JComboBox<?> combo) {
        combo.setFont(FONT_NORMAL);
        combo.setForeground(Color.BLACK); // Deep black text for perfect readability inside the white box
        combo.setBackground(Color.WHITE); // Solid clean white box background
        combo.setBorder(BorderFactory.createLineBorder(BORDER));
        
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                // Dropdown popup list container stays dark
                list.setBackground(Theme.BG_2);
                list.setSelectionBackground(Theme.RED);
                list.setSelectionForeground(Color.WHITE);
                
                if (isSelected) {
                    c.setBackground(Theme.RED);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Theme.BG_2);
                    c.setForeground(Theme.CREAM);
                }
                if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                }
                return c;
            }
        });
    }
}
