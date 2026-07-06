package com.bittersweetcinemas.ui;

import java.awt.Color;
import java.awt.Font;

public final class Theme {
    private Theme() {}

    // Dark cinema palette, but slightly brighter than pure black.
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
    public static final Font FONT_HEADING = new Font("Georgia", Font.BOLD, 18);
    public static final Font FONT_LOGO = new Font("Georgia", Font.BOLD, 16);
}
