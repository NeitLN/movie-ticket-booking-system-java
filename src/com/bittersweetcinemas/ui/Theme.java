package com.bittersweetcinemas.ui;

import java.awt.Color;
import java.awt.Font;

/**
 * THEME SYSTEM CONFIGURATION CLASS
 * -------------------------------------------------------------------------
 * This class defines the global color palette and font typography for the
 * Bittersweet Cinemas customer interface.
 * 
 * Designed using a "Utility Class" pattern with a private constructor to 
 * prevent instantiation (Encapsulation). It holds global public static final constants.
 */
public final class Theme {
    // Private constructor to prevent instantiation of utility class (OOP: Encapsulation)
    private Theme() {}

    // Dark Cinema-themed Color Palette
    public static final Color BG = new Color(20, 17, 16);          // Main background (Rich Dark Chocolate)
    public static final Color BG_2 = new Color(26, 22, 20);        // Secondary background (Slightly lighter dark)
    public static final Color TOP_BAR = new Color(14, 12, 11);     // Top utility bar (Rich obsidian)
    public static final Color NAV = new Color(18, 16, 15);         // Main Navigation Bar background
    public static final Color CARD = new Color(33, 25, 23);        // Movie Card background panel
    public static final Color CARD_2 = new Color(39, 29, 26);      // Secondary card/empty placeholder background
    public static final Color BORDER = new Color(69, 54, 42);      // Borders and divider lines
    public static final Color RED = new Color(215, 31, 70);        // Cinema Accent Red (Buttons, highlights)
    public static final Color RED_DARK = new Color(133, 24, 44);   // Darker Red (Rating borders)
    public static final Color GOLD = new Color(235, 184, 56);      // Golden yellow (Stars, highlights, accents)
    public static final Color CREAM = new Color(255, 241, 217);    // Light Cream (Title text, high contrast)
    public static final Color MUTED = new Color(174, 143, 111);    // Muted chestnut (Subtext, secondary labels)
    public static final Color SOFT = new Color(117, 95, 76);       // Soft taupe (Tertiary detail labels)

    // Typography Fonts (Segoe UI is chosen for flawless Vietnamese and global glyph rendering)
    public static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    
    // Georgia Font (Classic serif) provides an elegant, artistic film-site feel for logos and large banner headings
    public static final Font FONT_TITLE = new Font("Georgia", Font.BOLD, 42);
    public static final Font FONT_LOGO = new Font("Georgia", Font.BOLD, 16);
    
    // Heading font uses Segoe UI to ensure full support for uppercase Vietnamese accented characters (like "Ế")
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 18);
}
