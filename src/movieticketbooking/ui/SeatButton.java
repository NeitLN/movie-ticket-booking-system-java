package movieticketbooking.ui;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * SEAT BUTTON — PHASE 7 & 8 (Student 3)
 * -----------------------------------------------
 * A custom JButton that represents a single cinema seat in the seat layout.
 *
 * Three visual states (Phase 8 mandatory colors):
 *   AVAILABLE  — green  (#4CAF50): seat is free to select
 *   SELECTED   — gold   (Theme.GOLD): seat is selected by the current user
 *   BOOKED     — red    (Theme.RED): seat is already taken; cannot be toggled
 *
 * Clicking a BOOKED seat does nothing.
 * Clicking an AVAILABLE seat marks it SELECTED.
 * Clicking a SELECTED seat deselects it back to AVAILABLE.
 */
public class SeatButton extends JButton {

    // -------------------------------------------------------------------------
    // State enum
    // -------------------------------------------------------------------------

    public enum SeatState {
        AVAILABLE, SELECTED, BOOKED
    }

    // -------------------------------------------------------------------------
    // Color constants (required by Phase 8)
    // -------------------------------------------------------------------------

    private static final Color COLOR_AVAILABLE = new Color(44, 110, 60);   // dark green fill
    private static final Color COLOR_AVAILABLE_BORDER = new Color(76, 175, 80);
    private static final Color COLOR_SELECTED  = new Color(180, 130, 20);  // gold-ish fill
    private static final Color COLOR_SELECTED_BORDER = Theme.GOLD;
    private static final Color COLOR_BOOKED    = new Color(160, 20, 50);   // dark red fill
    private static final Color COLOR_BOOKED_BORDER = Theme.RED;
    private static final Color COLOR_HOVER_OVERLAY = new Color(255, 255, 255, 30);

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final String seatNumber;
    private SeatState state;
    private boolean hovered;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public SeatButton(String seatNumber, SeatState initialState) {
        super(seatNumber);
        this.seatNumber = seatNumber;
        this.state = initialState;

        setFont(new Font("Segoe UI", Font.BOLD, 11));
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);

        updateCursor();
        updateToolTip();

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
        });
    }

    // -------------------------------------------------------------------------
    // State management
    // -------------------------------------------------------------------------

    public SeatState getState() {
        return state;
    }

    public void setState(SeatState newState) {
        this.state = newState;
        updateCursor();
        updateToolTip();
        repaint();
    }

    /** Returns the seat label, e.g. "A1". */
    public String getSeatNumber() {
        return seatNumber;
    }

    public boolean isSelected() {
        return state == SeatState.SELECTED;
    }

    public boolean isBooked() {
        return state == SeatState.BOOKED;
    }

    /**
     * Toggles between AVAILABLE ↔ SELECTED.
     * Does nothing when the seat is BOOKED.
     */
    public void toggle() {
        if (state == SeatState.BOOKED) return;
        state = (state == SeatState.AVAILABLE) ? SeatState.SELECTED : SeatState.AVAILABLE;
        updateToolTip();
        repaint();
    }

    private void updateCursor() {
        setCursor(state == SeatState.BOOKED
            ? Cursor.getDefaultCursor()
            : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void updateToolTip() {
        switch (state) {
            case BOOKED:
                setToolTipText("Seat " + seatNumber + " is already booked and cannot be selected.");
                break;
            case SELECTED:
                setToolTipText("Seat " + seatNumber + " is selected. Click to deselect it.");
                break;
            default:
                setToolTipText("Seat " + seatNumber + " is available. Click to select it.");
                break;
        }
    }

    // -------------------------------------------------------------------------
    // Custom paint
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill, border;
        switch (state) {
            case BOOKED:
                fill = COLOR_BOOKED;
                border = COLOR_BOOKED_BORDER;
                break;
            case SELECTED:
                fill = COLOR_SELECTED;
                border = COLOR_SELECTED_BORDER;
                break;
            default: // AVAILABLE
                fill = COLOR_AVAILABLE;
                border = COLOR_AVAILABLE_BORDER;
                break;
        }

        int arc = 8;
        int w = getWidth() - 1;
        int h = getHeight() - 1;

        // Background fill
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        // Hover overlay (only for interactive states)
        if (hovered && state != SeatState.BOOKED) {
            g2.setColor(COLOR_HOVER_OVERLAY);
            g2.fillRoundRect(0, 0, w, h, arc, arc);
        }

        // Border
        g2.setColor(border);
        g2.drawRoundRect(0, 0, w, h, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }
}
