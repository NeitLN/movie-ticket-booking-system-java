package movieticketbooking.ui;

import movieticketbooking.exception.ValidationException;
import movieticketbooking.model.Booking;
import movieticketbooking.model.Movie;
import movieticketbooking.model.Screening;
import movieticketbooking.model.Seat;
import movieticketbooking.service.BookingService;
import movieticketbooking.service.MovieService;
import movieticketbooking.service.ScreeningService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * SEAT LAYOUT PANEL — PHASE 8 (Student 3)
 * -----------------------------------------------
 * Full "Bookings (Seat)" screen fulfilling all Phase 7 & 8 requirements:
 *
 *  ┌─ Left Column ──────────────────┐  ┌─ Right Column ─────────────────┐
 *  │ JComboBox — screening picker   │  │ Customer info form             │
 *  │ Screening details labels       │  │   Name field                   │
 *  │ ─────────────────────────────  │  │   Phone field                  │
 *  │ Legend (Available/Sel/Booked)  │  │ ─────────────────────────────  │
 *  │ Seat GridLayout (3×5)          │  │ Summary: seats selected        │
 *  │ ─────────────────────────────  │  │ Summary: total price           │
 *  │ SCREEN label at bottom         │  │ Confirm Booking button         │
 *  └────────────────────────────────┘  └────────────────────────────────┘
 *
 * Seat color rules (Phase 8 mandatory):
 *   Green  = Available
 *   Gold   = Selected (by current user)
 *   Red    = Booked (already taken)
 *
 * After a successful booking the booked seats flip to Red immediately.
 */
public class SeatLayoutPanel extends JPanel {

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private final BookingService bookingService;
    private final ScreeningService screeningService;
    private final MovieService movieService;

    // -------------------------------------------------------------------------
    // UI components
    // -------------------------------------------------------------------------

    private JComboBox<ScreeningItem> screeningCombo;
    private JLabel lblMovieTitle;
    private JLabel lblRoom;
    private JLabel lblDate;
    private JLabel lblTime;
    private JLabel lblBasePrice;

    private JPanel seatGrid;
    private SeatButton[] seatButtons;

    private JTextField txtCustomerName;
    private JTextField txtPhone;
    private JLabel lblSelectedCount;
    private JLabel lblTotalPrice;
    private JButton btnConfirm;
    private JButton btnClear;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public SeatLayoutPanel(BookingService bookingService,
                           ScreeningService screeningService,
                           MovieService movieService) {
        this.bookingService = bookingService;
        this.screeningService = screeningService;
        this.movieService = movieService;

        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        populateScreeningCombo();
    }

    // -------------------------------------------------------------------------
    // Builders
    // -------------------------------------------------------------------------

    /** Page header */
    private JComponent buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel heading = new JLabel("Book Tickets");
        heading.setFont(new Font("Georgia", Font.BOLD, 26));
        heading.setForeground(Theme.CREAM);

        JLabel sub = new JLabel("Select a screening, choose your seats, and confirm.");
        sub.setFont(Theme.FONT_NORMAL);
        sub.setForeground(Theme.MUTED);

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);
        text.add(heading);
        text.add(sub);
        bar.add(text, BorderLayout.WEST);

        return bar;
    }

    /** Two-column body: left = screening info + seat map, right = form + summary */
    private JComponent buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        GridBagConstraints left = new GridBagConstraints();
        left.fill = GridBagConstraints.BOTH;
        left.anchor = GridBagConstraints.NORTHWEST;
        left.weightx = 0.58;
        left.weighty = 1;
        left.gridx = 0; left.gridy = 0;
        left.insets = new Insets(0, 0, 0, 12);

        GridBagConstraints right = new GridBagConstraints();
        right.fill = GridBagConstraints.BOTH;
        right.anchor = GridBagConstraints.NORTHWEST;
        right.weightx = 0.42;
        right.weighty = 1;
        right.gridx = 1; right.gridy = 0;

        body.add(buildLeftColumn(), left);
        body.add(buildRightColumn(), right);
        return body;
    }

    /** Left: screening picker + details + seat map */
    private JComponent buildLeftColumn() {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setMinimumSize(new Dimension(520, 0));
        col.setPreferredSize(new Dimension(610, 510));

        // ── Screening picker ──
        RoundedPanel pickerCard = card();
        pickerCard.setLayout(new BoxLayout(pickerCard, BoxLayout.Y_AXIS));

        pickerCard.add(sectionLabel("Select Screening"));
        pickerCard.add(Box.createVerticalStrut(8));

        screeningCombo = new JComboBox<>();
        styleCombo(screeningCombo);
        screeningCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, 34));
        screeningCombo.addActionListener(e -> onScreeningSelected());
        pickerCard.add(screeningCombo);

        pickerCard.add(Box.createVerticalStrut(14));

        // Screening details
        lblMovieTitle = infoLabel("—");
        lblRoom      = infoLabel("—");
        lblDate      = infoLabel("—");
        lblTime      = infoLabel("—");
        lblBasePrice = infoLabel("—");

        pickerCard.add(detailRow("Movie:",      lblMovieTitle));
        pickerCard.add(Box.createVerticalStrut(4));
        pickerCard.add(detailRow("Room:",       lblRoom));
        pickerCard.add(Box.createVerticalStrut(4));
        pickerCard.add(detailRow("Date:",       lblDate));
        pickerCard.add(Box.createVerticalStrut(4));
        pickerCard.add(detailRow("Time:",       lblTime));
        pickerCard.add(Box.createVerticalStrut(4));
        pickerCard.add(detailRow("Base Price:", lblBasePrice));

        lockCardHeight(pickerCard);
        col.add(pickerCard);
        col.add(Box.createVerticalStrut(12));

        // ── Seat map card ──
        RoundedPanel seatCard = card();
        seatCard.setLayout(new BoxLayout(seatCard, BoxLayout.Y_AXIS));

        seatCard.add(sectionLabel("Seat Layout"));
        seatCard.add(Box.createVerticalStrut(10));
        seatCard.add(buildLegend());
        seatCard.add(Box.createVerticalStrut(12));
        seatCard.add(buildSeatGrid());
        seatCard.add(Box.createVerticalStrut(10));
        seatCard.add(buildScreenLabel());

        lockCardHeight(seatCard);
        col.add(seatCard);
        col.add(Box.createVerticalGlue());
        return col;
    }

    /** Legend showing the three seat color meanings. */
    private JComponent buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        legend.setOpaque(false);
        legend.add(legendDot(new Color(76, 175, 80)));
        legend.add(legendText("Standard"));
        legend.add(Box.createHorizontalStrut(4));
        legend.add(legendDot(new Color(156, 39, 176)));
        legend.add(legendText("VIP"));
        legend.add(Box.createHorizontalStrut(4));
        legend.add(legendDot(Theme.GOLD));
        legend.add(legendText("Selected"));
        legend.add(Box.createHorizontalStrut(4));
        legend.add(legendDot(Theme.RED));
        legend.add(legendText("Booked"));
        return legend;
    }

    private JLabel legendDot(Color c) {
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dot.setForeground(c);
        return dot;
    }

    private JLabel legendText(String t) {
        JLabel l = new JLabel(t);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.MUTED);
        return l;
    }

    /** 3-row × 5-column seat grid using SeatButton instances. */
    private JComponent buildSeatGrid() {
        // Rows: A, B, C  |  Cols: 1-5
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        // Row labels + seats side by side
        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);

        seatButtons = new SeatButton[BookingService.SEAT_NUMBERS.length];
        seatGrid = new JPanel(new GridLayout(3, 5, 6, 6));
        seatGrid.setOpaque(false);

        for (int i = 0; i < BookingService.SEAT_NUMBERS.length; i++) {
            SeatButton btn = new SeatButton(BookingService.SEAT_NUMBERS[i], SeatButton.SeatState.AVAILABLE);
            btn.setPreferredSize(new Dimension(54, 40));
            final int idx = i;
            btn.addActionListener(e -> {
                seatButtons[idx].toggle();
                refreshSummary();
            });
            seatButtons[i] = btn;
            seatGrid.add(btn);
        }

        wrapper.add(seatGrid, BorderLayout.CENTER);
        return wrapper;
    }

    /** "SCREEN" bar at the bottom of the seat map (cinema convention). */
    private JComponent buildScreenLabel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JPanel bar = new JPanel();
        bar.setBackground(new Color(60, 50, 40));
        bar.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        bar.setLayout(new GridBagLayout());

        JLabel scr = new JLabel("S C R E E N");
        scr.setFont(new Font("Segoe UI", Font.BOLD, 10));
        scr.setForeground(Theme.MUTED);
        bar.add(scr);

        p.add(bar);
        return p;
    }

    /** Right: customer form + summary + actions */
    private JComponent buildRightColumn() {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setMinimumSize(new Dimension(300, 0));
        col.setPreferredSize(new Dimension(380, 460));

        // ── Customer form card ──
        RoundedPanel formCard = card();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        formCard.add(sectionLabel("Customer Information"));
        formCard.add(Box.createVerticalStrut(12));

        formCard.add(fieldLabel("Customer Name *"));
        formCard.add(Box.createVerticalStrut(4));
        txtCustomerName = styledField("Enter full name");
        formCard.add(txtCustomerName);

        formCard.add(Box.createVerticalStrut(10));
        formCard.add(fieldLabel("Phone Number *"));
        formCard.add(Box.createVerticalStrut(4));
        txtPhone = styledField("e.g. 0901234567");
        formCard.add(txtPhone);

        lockCardHeight(formCard);
        col.add(formCard);
        col.add(Box.createVerticalStrut(12));

        // ── Summary card ──
        RoundedPanel summaryCard = card();
        summaryCard.setLayout(new BoxLayout(summaryCard, BoxLayout.Y_AXIS));

        summaryCard.add(sectionLabel("Booking Summary"));
        summaryCard.add(Box.createVerticalStrut(12));

        lblSelectedCount = infoLabel("0 seat(s) selected");
        lblSelectedCount.setForeground(Theme.CREAM);
        lblSelectedCount.setFont(Theme.FONT_BOLD);
        summaryCard.add(lblSelectedCount);
        summaryCard.add(Box.createVerticalStrut(6));

        lblTotalPrice = infoLabel("Total: 0 VND");
        lblTotalPrice.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalPrice.setForeground(Theme.GOLD);
        summaryCard.add(lblTotalPrice);

        lockCardHeight(summaryCard);
        col.add(summaryCard);
        col.add(Box.createVerticalStrut(12));

        // ── Action card ──
        RoundedPanel actionCard = card();
        actionCard.setLayout(new BoxLayout(actionCard, BoxLayout.Y_AXIS));

        btnConfirm = new RoundedButton("Confirm Booking", Theme.RED, Theme.RED_DARK, Color.WHITE, null);
        btnConfirm.setPreferredSize(new Dimension(280, 40));
        btnConfirm.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        btnConfirm.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConfirm.setToolTipText("Validate the customer information and save the selected seats.");
        btnConfirm.addActionListener(e -> onConfirmBooking());
        actionCard.add(btnConfirm);

        actionCard.add(Box.createVerticalStrut(8));

        btnClear = new RoundedButton("Clear Selection", Theme.CARD, Theme.CARD_2, Theme.MUTED, Theme.BORDER);
        btnClear.setPreferredSize(new Dimension(280, 36));
        btnClear.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        btnClear.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnClear.setToolTipText("Deselect all seats that have not been booked yet.");
        btnClear.addActionListener(e -> clearSelection());
        actionCard.add(btnClear);

        lockCardHeight(actionCard);
        col.add(actionCard);
        col.add(Box.createVerticalGlue());
        return col;
    }

    // -------------------------------------------------------------------------
    // Event handlers
    // -------------------------------------------------------------------------

    /** Called when the screening combo selection changes. */
    private void onScreeningSelected() {
        ScreeningItem item = (ScreeningItem) screeningCombo.getSelectedItem();
        if (item == null || item.screening == null) {
            clearScreeningDetails();
            resetSeatMap(null);
            return;
        }
        Screening s = item.screening;
        Movie m = findMovieById(s.getMovieId());

        lblMovieTitle.setText(m != null ? m.getTitle() : s.getMovieId());
        lblRoom.setText(s.getRoom());
        lblDate.setText(s.getScreeningDate().format(DATE_FMT));
        lblTime.setText(s.getStartTime().format(TIME_FMT));
        lblBasePrice.setText(String.format("%,.0f VND / seat", s.getBasePrice()));

        // Reset seats and mark already-booked ones
        List<String> bookedSeats = bookingService.getBookedSeatNumbers(s.getScreeningId());
        resetSeatMap(bookedSeats);
        refreshSummary();
    }

    /** Resets all seat buttons; marks given seats as BOOKED. */
    private void resetSeatMap(List<String> bookedSeatNumbers) {
        for (SeatButton btn : seatButtons) {
            boolean isBooked = bookedSeatNumbers != null &&
                               bookedSeatNumbers.contains(btn.getSeatNumber());
            btn.setState(isBooked ? SeatButton.SeatState.BOOKED : SeatButton.SeatState.AVAILABLE);
        }
        refreshSummary();
    }

    /** Updates selected-count and total-price labels. */
    private void refreshSummary() {
        List<String> selected = getSelectedSeatNumbers();
        lblSelectedCount.setText(selected.size() + " seat(s) selected: " +
            (selected.isEmpty() ? "none" : String.join(", ", selected)));

        ScreeningItem item = (ScreeningItem) screeningCombo.getSelectedItem();
        if (item != null && item.screening != null && !selected.isEmpty()) {
            double total = 0;
            for (String sn : selected) {
                total += Seat.create(sn).calculatePrice(item.screening.getBasePrice());
            }
            lblTotalPrice.setText(String.format("Total: %,.0f VND", total));
        } else {
            lblTotalPrice.setText("Total: 0 VND");
        }
    }

    /** Returns seat numbers of all SELECTED buttons. */
    private List<String> getSelectedSeatNumbers() {
        List<String> list = new ArrayList<>();
        for (SeatButton btn : seatButtons) {
            if (btn.isSelected()) list.add(btn.getSeatNumber());
        }
        return list;
    }

    /** Deselects all currently-SELECTED seats (BOOKED seats are unchanged). */
    private void clearSelection() {
        for (SeatButton btn : seatButtons) {
            if (btn.isSelected()) btn.setState(SeatButton.SeatState.AVAILABLE);
        }
        refreshSummary();
    }

    /** Validates input and creates a booking; shows result dialogs. */
    private void onConfirmBooking() {
        ScreeningItem item = (ScreeningItem) screeningCombo.getSelectedItem();
        if (item == null || item.screening == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a screening first.",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name  = txtCustomerName.getText().trim();
        String phone = txtPhone.getText().trim();
        List<String> seats = getSelectedSeatNumbers();

        try {
            Booking booking = bookingService.createBooking(
                item.screening.getScreeningId(), name, phone, seats);

            // Mark the booked seats red immediately
            for (SeatButton btn : seatButtons) {
                if (seats.contains(btn.getSeatNumber())) {
                    btn.setState(SeatButton.SeatState.BOOKED);
                }
            }
            txtCustomerName.setText("");
            txtPhone.setText("");
            refreshSummary();

            JOptionPane.showMessageDialog(this,
                "<html><b>Booking confirmed!</b><br>" +
                "Booking ID: " + booking.getBookingId() + "<br>" +
                "Seats: " + String.join(", ", seats) + "<br>" +
                String.format("Total: %,.0f VND", booking.getTotalPrice()) + "</html>",
                "Booking Successful", JOptionPane.INFORMATION_MESSAGE);

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Booking Error", JOptionPane.ERROR_MESSAGE);
        } catch (UncheckedIOException ex) {
            JOptionPane.showMessageDialog(this,
                "Unable to save the booking data. No booking was created.\n" + ex.getMessage(),
                "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------------------
    // Public refresh hook (called by MainFrame when navigating to this panel)
    // -------------------------------------------------------------------------

    public void refreshView() {
        populateScreeningCombo();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void populateScreeningCombo() {
        ScreeningItem previousItem = (ScreeningItem) screeningCombo.getSelectedItem();
        String previousId = (previousItem != null && previousItem.screening != null)
            ? previousItem.screening.getScreeningId() : null;

        screeningCombo.removeAllItems();
        screeningCombo.addItem(new ScreeningItem(null)); // placeholder

        List<Screening> all = screeningService.getAllScreenings();
        int selectIndex = 0;
        for (int i = 0; i < all.size(); i++) {
            Screening s = all.get(i);
            screeningCombo.addItem(new ScreeningItem(s));
            if (s.getScreeningId().equals(previousId)) {
                selectIndex = i + 1; // +1 for the placeholder
            }
        }
        screeningCombo.setSelectedIndex(selectIndex);
        onScreeningSelected();
    }

    private void clearScreeningDetails() {
        lblMovieTitle.setText("—");
        lblRoom.setText("—");
        lblDate.setText("—");
        lblTime.setText("—");
        lblBasePrice.setText("—");
    }

    private Movie findMovieById(String movieId) {
        for (Movie m : movieService.getAllMovies()) {
            if (m.getMovieId().equalsIgnoreCase(movieId)) return m;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Widget factories
    // -------------------------------------------------------------------------

    private RoundedPanel card() {
        RoundedPanel p = new RoundedPanel(14, Theme.CARD, Theme.BORDER);
        p.setBorder(new EmptyBorder(18, 18, 18, 18));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        return p;
    }

    /** Keeps a BoxLayout card at its natural height while allowing it to fill the column width. */
    private void lockCardHeight(JComponent component) {
        int preferredHeight = component.getPreferredSize().height;
        component.setMaximumSize(new Dimension(Short.MAX_VALUE, preferredHeight));
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_HEADING);
        l.setForeground(Theme.CREAM);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BOLD);
        l.setForeground(Theme.MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel infoLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_NORMAL);
        l.setForeground(Theme.MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel detailRow(String labelText, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel key = new JLabel(labelText);
        key.setFont(Theme.FONT_BOLD);
        key.setForeground(Theme.SOFT);
        key.setPreferredSize(new Dimension(90, 20));

        row.add(key, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.CENTER);
        return row;
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(Theme.FONT_NORMAL);
        f.setForeground(Theme.CREAM);
        f.setBackground(Theme.BG);
        f.setCaretColor(Theme.CREAM);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            new EmptyBorder(6, 10, 6, 10)
        ));
        f.setMaximumSize(new Dimension(Short.MAX_VALUE, 34));
        f.setToolTipText(placeholder);
        return f;
    }

    @SuppressWarnings("unchecked")
    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(Theme.FONT_NORMAL);
        combo.setForeground(Theme.CREAM);
        combo.setBackground(Theme.BG);
        combo.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? Theme.CARD_2 : Theme.CARD);
                setForeground(Theme.CREAM);
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
    }

    // -------------------------------------------------------------------------
    // Screening combo item wrapper
    // -------------------------------------------------------------------------

    /** Wrapper that produces a readable label in the JComboBox. */
    private class ScreeningItem {
        final Screening screening;

        ScreeningItem(Screening screening) {
            this.screening = screening;
        }

        @Override
        public String toString() {
            if (screening == null) return "— Select a screening —";
            Movie m = findMovieById(screening.getMovieId());
            String title = m != null ? m.getTitle() : screening.getMovieId();
            return String.format("[%s] %s | %s | %s %s",
                screening.getScreeningId(),
                title,
                screening.getRoom(),
                screening.getScreeningDate().format(DATE_FMT),
                screening.getStartTime().format(TIME_FMT));
        }
    }
}
