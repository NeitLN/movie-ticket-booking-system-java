package movieticketbooking.ui;

import movieticketbooking.exception.ValidationException;
import movieticketbooking.model.Booking;
import movieticketbooking.model.Movie;
import movieticketbooking.model.Screening;
import movieticketbooking.model.Seat;
import movieticketbooking.service.BookingService;
import movieticketbooking.service.MovieService;
import movieticketbooking.service.ScreeningService;
import movieticketbooking.service.InvoiceService;
import movieticketbooking.util.FormatUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * BOOKING HISTORY PANEL — PHASE 8 (Student 3)
 * -----------------------------------------------
 * Fulfills the "Booking History" requirements:
 *
 *  • JTable listing all bookings (ID, Screening, Customer, Phone, Seats, Total, Status)
 *  • Search by Booking ID / Customer Name / Phone Number
 *  • "View Details" dialog showing complete booking info
 *  • Inline editing of Customer Name and Phone (Update button)
 *  • Cancel Booking — seats are released back to the pool
 *  • All changes persisted to bookings.txt via BookingService
 *
 * Cancelled rows are rendered in a muted style to distinguish them from CONFIRMED ones.
 */
public class BookingHistoryPanel extends JPanel {

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private final BookingService bookingService;
    private final ScreeningService screeningService;
    private final MovieService movieService;
    private final InvoiceService invoiceService;

    // -------------------------------------------------------------------------
    // Table
    // -------------------------------------------------------------------------

    private static final String[] COLUMNS = {
        "Booking ID", "Screening ID", "Customer", "Phone", "Seats", "Total (VND)", "Status"
    };
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel statusLabel;
    private List<Booking> displayedBookings; // parallel list; same order as table rows

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    private JTextField txtSearch;
    private JComboBox<String> comboSearchField;

    // -------------------------------------------------------------------------
    // Detail / edit panel
    // -------------------------------------------------------------------------

    private JPanel detailPanel;
    private JSplitPane bodySplitPane;
    private JScrollPane tableScrollPane;
    private JScrollPane detailScrollPane;
    private JLabel lblDetailBookingId;
    private JTextArea lblDetailScreening;
    private JLabel lblDetailSeats;
    private JLabel lblDetailTotal;
    private JLabel lblDetailStatus;
    private JTextField txtEditName;
    private JTextField txtEditPhone;
    private JButton btnUpdate;
    private JButton btnCancel;
    private JButton btnExport;
    private JLabel lblEditState;

    private Booking selectedBooking; // the booking currently shown in detail panel

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public BookingHistoryPanel(BookingService bookingService,
                               ScreeningService screeningService,
                               MovieService movieService) {
        this.bookingService = bookingService;
        this.screeningService = screeningService;
        this.movieService = movieService;
        this.invoiceService = new InvoiceService(screeningService, movieService);
        this.displayedBookings = new ArrayList<>();

        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        refreshTable(bookingService.getAllBookings());
    }

    // -------------------------------------------------------------------------
    // Builders
    // -------------------------------------------------------------------------

    private JComponent buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 18, 0));

        // Title
        JLabel heading = new JLabel("Booking History");
        heading.setFont(new Font("Georgia", Font.BOLD, 26));
        heading.setForeground(Theme.CREAM);
        JLabel sub = new JLabel("Search, view details, update contact info, or cancel bookings.");
        sub.setFont(Theme.FONT_NORMAL);
        sub.setForeground(Theme.MUTED);
        JPanel titleCol = new JPanel(new GridLayout(2, 1, 0, 2));
        titleCol.setOpaque(false);
        titleCol.add(heading);
        titleCol.add(sub);
        bar.add(titleCol, BorderLayout.WEST);

        // Search row
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchRow.setOpaque(false);

        comboSearchField = new JComboBox<>(new String[]{"All Fields", "Booking ID", "Customer Name", "Phone"});
        Theme.styleCombo(comboSearchField);
        comboSearchField.setPreferredSize(new Dimension(140, 32));

        txtSearch = new JTextField();
        txtSearch.setFont(Theme.FONT_NORMAL);
        txtSearch.setForeground(Theme.CREAM);
        txtSearch.setBackground(Theme.BG);
        txtSearch.setCaretColor(Theme.CREAM);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            new EmptyBorder(4, 8, 4, 8)));
        txtSearch.setPreferredSize(new Dimension(220, 32));
        txtSearch.setToolTipText("Search bookings…");

        JButton btnSearch = new RoundedButton("Search", Theme.CARD, Theme.CARD_2, Theme.CREAM, Theme.BORDER);
        btnSearch.setPreferredSize(new Dimension(80, 32));
        btnSearch.setToolTipText("Search booking history using the selected field.");
        btnSearch.addActionListener(e -> performSearch());

        JButton btnRefresh = new RoundedButton("Refresh", Theme.CARD, Theme.CARD_2, Theme.MUTED, Theme.BORDER);
        btnRefresh.setPreferredSize(new Dimension(90, 32));
        btnRefresh.setToolTipText("Reload booking history from data/bookings.txt.");
        btnRefresh.addActionListener(e -> {
            try {
                bookingService.reload();
                txtSearch.setText("");
                refreshTable(bookingService.getAllBookings());
                clearDetailPanel();
            } catch (UncheckedIOException ex) {
                showReadFileError(ex);
            }
        });

        // Allow pressing Enter in search field
        txtSearch.addActionListener(e -> performSearch());

        searchRow.add(comboSearchField);
        searchRow.add(txtSearch);
        searchRow.add(btnSearch);
        searchRow.add(btnRefresh);
        bar.add(searchRow, BorderLayout.EAST);

        return bar;
    }

    /** Two-column body: left = JTable, right = detail/edit panel */
    private JComponent buildBody() {
        bodySplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        bodySplitPane.setOpaque(false);
        bodySplitPane.setBorder(null);
        bodySplitPane.setDividerSize(6);
        bodySplitPane.setContinuousLayout(true);
        bodySplitPane.setResizeWeight(0.67);

        JComponent tablePanel = buildTablePanel();
        JComponent details = buildDetailPanel();
        tablePanel.setMinimumSize(new Dimension(500, 320));
        details.setMinimumSize(new Dimension(285, 320));

        bodySplitPane.setLeftComponent(tablePanel);
        bodySplitPane.setRightComponent(details);

        // setDividerLocation(double) needs a realised width. Reapply it once
        // Swing has laid out the non-maximised frame.
        SwingUtilities.invokeLater(() -> bodySplitPane.setDividerLocation(0.67));
        return bodySplitPane;
    }

    private JComponent buildTablePanel() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(Theme.FONT_NORMAL);
        table.setForeground(Theme.CREAM);
        table.setBackground(Theme.CARD);
        table.setSelectionBackground(Theme.CARD_2);
        table.setSelectionForeground(Theme.GOLD);
        table.setGridColor(Theme.BORDER);
        table.setRowHeight(28);
        table.setIntercellSpacing(new Dimension(8, 4));
        table.getTableHeader().setFont(Theme.FONT_BOLD);
        table.getTableHeader().setBackground(Theme.TOP_BAR);
        table.getTableHeader().setForeground(Color.BLACK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        // Colour CANCELLED rows differently
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                setBackground(isSelected ? Theme.CARD_2 : Theme.CARD);
                if (row < displayedBookings.size()) {
                    Booking b = displayedBookings.get(row);
                    boolean cancelled = "CANCELLED".equalsIgnoreCase(b.getStatus());
                    setForeground(isSelected ? Theme.GOLD :
                        (cancelled ? Theme.SOFT : Theme.CREAM));
                    if (cancelled && !isSelected) {
                        setFont(Theme.FONT_SMALL);
                    } else {
                        setFont(Theme.FONT_NORMAL);
                    }
                }
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return this;
            }
        });

        // Selection listener — populate detail panel
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });

        // Column widths
        int[] widths = {80, 95, 130, 100, 90, 100, 90};
        int[] minimums = {62, 72, 82, 82, 65, 78, 72};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(minimums[i]);
        }

        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        tableScrollPane.getViewport().setBackground(Theme.CARD);
        tableScrollPane.setBackground(Theme.CARD);
        tableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tableScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        return tableScrollPane;
    }

    /** Right-hand detail and edit panel. */
    private JComponent buildDetailPanel() {
        detailPanel = new WidthTrackingPanel();
        detailPanel.setBackground(Theme.BG_2);
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Heading
        JLabel heading = new JLabel("Booking Details");
        heading.setFont(Theme.FONT_HEADING);
        heading.setForeground(Theme.CREAM);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailPanel.add(heading);
        detailPanel.add(Box.createVerticalStrut(14));

        // Read-only detail labels
        lblDetailBookingId = detailValueLabel("—");
        lblDetailScreening  = detailWrappingText("—");
        lblDetailSeats      = detailValueLabel("—");
        lblDetailTotal      = detailValueLabel("—");
        lblDetailStatus     = detailValueLabel("—");

        detailPanel.add(detailRow("Booking ID:", lblDetailBookingId));
        detailPanel.add(Box.createVerticalStrut(6));
        detailPanel.add(detailRow("Screening:",  lblDetailScreening));
        detailPanel.add(Box.createVerticalStrut(6));
        detailPanel.add(detailRow("Seats:",      lblDetailSeats));
        detailPanel.add(Box.createVerticalStrut(6));
        detailPanel.add(detailRow("Total:",      lblDetailTotal));
        detailPanel.add(Box.createVerticalStrut(6));
        detailPanel.add(detailRow("Status:",     lblDetailStatus));

        detailPanel.add(Box.createVerticalStrut(18));
        detailPanel.add(separator());
        detailPanel.add(Box.createVerticalStrut(14));

        // Editable fields
        JLabel editHeading = new JLabel("Edit Contact Info");
        editHeading.setFont(Theme.FONT_BOLD);
        editHeading.setForeground(Theme.MUTED);
        editHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailPanel.add(editHeading);
        detailPanel.add(Box.createVerticalStrut(6));

        lblEditState = new JLabel(" ");
        lblEditState.setFont(Theme.FONT_SMALL);
        lblEditState.setForeground(Theme.RED);
        lblEditState.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblEditState.setVisible(false);
        detailPanel.add(lblEditState);
        detailPanel.add(Box.createVerticalStrut(8));

        detailPanel.add(fieldLabel("Name"));
        detailPanel.add(Box.createVerticalStrut(4));
        txtEditName = editField();
        detailPanel.add(txtEditName);
        detailPanel.add(Box.createVerticalStrut(8));

        detailPanel.add(fieldLabel("Phone"));
        detailPanel.add(Box.createVerticalStrut(4));
        txtEditPhone = editField();
        detailPanel.add(txtEditPhone);
        detailPanel.add(Box.createVerticalStrut(14));

        btnUpdate = new RoundedButton("Save Changes", Theme.CARD, Theme.CARD_2, Theme.GOLD, Theme.BORDER);
        btnUpdate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnUpdate.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnUpdate.setEnabled(false);
        btnUpdate.setToolTipText("Save the edited customer name and phone number.");
        btnUpdate.addActionListener(e -> onUpdateBooking());
        detailPanel.add(btnUpdate);

        detailPanel.add(Box.createVerticalStrut(10));

        btnCancel = new RoundedButton("Cancel Booking", Theme.RED_DARK, Theme.RED, Color.WHITE, null);
        btnCancel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnCancel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCancel.setEnabled(false);
        btnCancel.setToolTipText("Cancel the selected booking and release its seats.");
        btnCancel.addActionListener(e -> onCancelBooking());
        detailPanel.add(btnCancel);

        detailPanel.add(Box.createVerticalStrut(10));

        btnExport = new RoundedButton("Export Invoice", Theme.CARD_2, Theme.CARD, Theme.CREAM, Theme.BORDER);
        btnExport.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnExport.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnExport.setEnabled(false);
        btnExport.setToolTipText("Export a text invoice for the selected booking.");
        btnExport.addActionListener(e -> onExportInvoice());
        detailPanel.add(btnExport);

        detailPanel.add(Box.createVerticalGlue());

        detailScrollPane = new JScrollPane(detailPanel);
        detailScrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        detailScrollPane.getViewport().setBackground(Theme.BG_2);
        detailScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        detailScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        detailScrollPane.getVerticalScrollBar().setUnitIncrement(14);
        detailScrollPane.setMinimumSize(new Dimension(285, 320));
        return detailScrollPane;
    }

    /** Empty-state feedback bar, shown only when the table has no rows to display. */
    private JComponent buildStatusBar() {
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Theme.MUTED);
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setBorder(new EmptyBorder(8, 4, 0, 0));
        statusLabel.setVisible(false);
        return statusLabel;
    }

    /**
     * Hides the label when the table has rows. When empty, distinguishes a genuinely
     * empty source ("No bookings found.") from a search that matched nothing
     * ("No bookings match the current search."). Never called from a failed reload
     * (see refreshView()), so a read error is never shown as an ordinary empty state.
     */
    private void updateStatusLabel() {
        if (!displayedBookings.isEmpty()) {
            statusLabel.setVisible(false);
            return;
        }
        boolean sourceIsEmpty = bookingService.getAllBookings().isEmpty();
        statusLabel.setText(sourceIsEmpty ? "No bookings found." : "No bookings match the current search.");
        statusLabel.setVisible(true);
    }

    // -------------------------------------------------------------------------
    // Event handlers
    // -------------------------------------------------------------------------

    private void performSearch() {
        String query = txtSearch.getText().trim();
        String field = (String) comboSearchField.getSelectedItem();
        List<Booking> all = bookingService.getAllBookings();

        if (query.isEmpty() || "All Fields".equals(field)) {
            if (!query.isEmpty()) {
                // Filter across all searchable fields
                String lq = query.toLowerCase();
                List<Booking> filtered = new ArrayList<>();
                for (Booking b : all) {
                    if (b.getBookingId().toLowerCase().contains(lq) ||
                        b.getCustomerName().toLowerCase().contains(lq) ||
                        b.getPhone().contains(query)) {
                        filtered.add(b);
                    }
                }
                refreshTable(filtered);
            } else {
                refreshTable(all);
            }
        } else if ("Booking ID".equals(field)) {
            List<Booking> result = new ArrayList<>();
            for (Booking b : all) {
                if (b.getBookingId().toLowerCase().contains(query.toLowerCase())) result.add(b);
            }
            refreshTable(result);
        } else if ("Customer Name".equals(field)) {
            refreshTable(bookingService.searchByCustomerName(query));
        } else { // Phone
            refreshTable(bookingService.searchByPhone(query));
        }
        clearDetailPanel();
    }

    /** Populates the detail/edit panel when a table row is selected. */
    private void onRowSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= displayedBookings.size()) {
            clearDetailPanel();
            return;
        }
        selectedBooking = displayedBookings.get(row);
        populateDetailPanel(selectedBooking);
    }

    /** Saves updated name/phone for the selected booking. */
    private void onUpdateBooking() {
        if (selectedBooking == null) return;
        if ("CANCELLED".equalsIgnoreCase(selectedBooking.getStatus())) {
            JOptionPane.showMessageDialog(this,
                "Cancelled bookings are read-only.",
                "Booking Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String newName  = txtEditName.getText().trim();
        String newPhone = txtEditPhone.getText().trim();
        try {
            bookingService.updateCustomerInfo(selectedBooking.getBookingId(), newName, newPhone);
            // Re-read the updated object
            selectedBooking = bookingService.getBookingById(selectedBooking.getBookingId());
            populateDetailPanel(selectedBooking);
            refreshTableKeepingSelection();
            JOptionPane.showMessageDialog(this,
                "Contact information updated successfully.",
                "Update Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (UncheckedIOException ex) {
            JOptionPane.showMessageDialog(this,
                "Unable to save the updated booking data. The previous values were restored.\n" + ex.getMessage(),
                "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Cancels the selected booking after confirmation. */
    private void onCancelBooking() {
        if (selectedBooking == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html>Cancel booking <b>" + selectedBooking.getBookingId() + "</b>?<br>" +
            "Seats will be released back to the pool.</html>",
            "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            bookingService.cancelBooking(selectedBooking.getBookingId());
            selectedBooking = bookingService.getBookingById(selectedBooking.getBookingId());
            populateDetailPanel(selectedBooking);
            refreshTableKeepingSelection();
            JOptionPane.showMessageDialog(this,
                "Booking " + selectedBooking.getBookingId() + " has been cancelled.\nSeats have been released.",
                "Booking Cancelled", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Cancellation Error", JOptionPane.ERROR_MESSAGE);
        } catch (UncheckedIOException ex) {
            JOptionPane.showMessageDialog(this,
                "Unable to save the cancellation. The booking remains confirmed.\n" + ex.getMessage(),
                "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onExportInvoice() {
        if (selectedBooking == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a booking to export.",
                "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            java.nio.file.Path path = invoiceService.exportInvoice(selectedBooking);
            JOptionPane.showMessageDialog(this,
                "Invoice successfully exported to:\n" + path.toString(),
                "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidationException | UncheckedIOException ex) {
            JOptionPane.showMessageDialog(this,
                "Unable to export invoice:\n" + ex.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------------------
    // Table helpers
    // -------------------------------------------------------------------------

    private void refreshTable(List<Booking> list) {
        displayedBookings = new ArrayList<>(list);
        tableModel.setRowCount(0);
        for (Booking b : displayedBookings) {
            List<String> seatNums = new ArrayList<>();
            for (Seat s : b.getSeats()) seatNums.add(s.getSeatNumber());
            tableModel.addRow(new Object[]{
                b.getBookingId(),
                b.getScreeningId(),
                b.getCustomerName(),
                b.getPhone(),
                String.join(", ", seatNums),
                String.format("%,.0f", b.getTotalPrice()),
                b.getStatus()
            });
        }
        updateStatusLabel();
    }

    /** Re-renders the table while keeping the same row selected. */
    private void refreshTableKeepingSelection() {
        String bookingId = selectedBooking != null ? selectedBooking.getBookingId() : null;
        refreshTable(bookingService.getAllBookings());

        if (bookingId != null) {
            for (int i = 0; i < displayedBookings.size(); i++) {
                if (displayedBookings.get(i).getBookingId().equals(bookingId)) {
                    table.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Detail panel helpers
    // -------------------------------------------------------------------------

    private void populateDetailPanel(Booking b) {
        Screening scr = screeningService.getScreeningById(b.getScreeningId());
        Movie    mov  = scr != null ? findMovieById(scr.getMovieId()) : null;

        lblDetailBookingId.setText(b.getBookingId());

        if (scr != null) {
            String movieTitle = mov != null ? mov.getTitle() : scr.getMovieId();
            String screeningText = scr.getScreeningId() + " - " + movieTitle;
            String scheduleText = scr.getRoom() + " | "
                + scr.getScreeningDate().format(DATE_FMT) + " "
                + scr.getStartTime().format(TIME_FMT);
            lblDetailScreening.setText(screeningText + "\n" + scheduleText);
            lblDetailScreening.setToolTipText(screeningText + " | " + scheduleText);
        } else {
            lblDetailScreening.setText(b.getScreeningId() + " (screening not found)");
        }

        List<String> seatNums = new ArrayList<>();
        for (Seat s : b.getSeats()) seatNums.add(s.getSeatNumber());
        lblDetailSeats.setText(String.join(", ", seatNums));
        lblDetailTotal.setText(FormatUtils.formatVnd(b.getTotalPrice()));

        boolean cancelled = "CANCELLED".equalsIgnoreCase(b.getStatus());
        lblDetailStatus.setText(b.getStatus());
        lblDetailStatus.setForeground(cancelled ? Theme.RED : new Color(76, 175, 80));

        txtEditName.setText(b.getCustomerName());
        txtEditPhone.setText(b.getPhone());

        // A cancelled booking is historical data. Keep its contact details visible,
        // but lock the editor and both actions so the panel cannot look active.
        setContactEditorEnabled(!cancelled);
        btnCancel.setEnabled(!cancelled);
        btnExport.setEnabled(true);
        lblEditState.setText(cancelled
            ? "Cancelled booking - contact information is read-only."
            : " ");
        lblEditState.setVisible(cancelled);

        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void clearDetailPanel() {
        selectedBooking = null;
        lblDetailBookingId.setText("—");
        lblDetailScreening.setText("—");
        lblDetailSeats.setText("—");
        lblDetailTotal.setText("—");
        lblDetailStatus.setText("—");
        lblDetailStatus.setForeground(Theme.MUTED);
        lblDetailScreening.setToolTipText(null);
        txtEditName.setText("");
        txtEditPhone.setText("");
        setContactEditorEnabled(false);
        btnCancel.setEnabled(false);
        btnExport.setEnabled(false);
        lblEditState.setText(" ");
        lblEditState.setVisible(false);
    }

    // -------------------------------------------------------------------------
    // Public refresh hook (called by MainFrame when navigating here)
    // -------------------------------------------------------------------------

    public void refreshView() {
        try {
            bookingService.reload();
            String query = txtSearch.getText().trim();
            if (query.isEmpty()) {
                refreshTable(bookingService.getAllBookings());
            } else {
                performSearch();
            }
            if (selectedBooking != null) {
                selectedBooking = bookingService.getBookingById(selectedBooking.getBookingId());
                if (selectedBooking != null) populateDetailPanel(selectedBooking);
                else clearDetailPanel();
            }
        } catch (UncheckedIOException ex) {
            showReadFileError(ex);
        }
    }

    private void showReadFileError(UncheckedIOException ex) {
        JOptionPane.showMessageDialog(this,
            "Unable to reload booking history. The currently displayed data was kept.\n" + ex.getMessage(),
            "File Error", JOptionPane.ERROR_MESSAGE);
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    private Movie findMovieById(String movieId) {
        for (Movie m : movieService.getAllMovies()) {
            if (m.getMovieId().equalsIgnoreCase(movieId)) return m;
        }
        return null;
    }

    private void setContactEditorEnabled(boolean enabled) {
        txtEditName.setEditable(enabled);
        txtEditPhone.setEditable(enabled);
        txtEditName.setFocusable(enabled);
        txtEditPhone.setFocusable(enabled);
        txtEditName.setBackground(enabled ? Theme.BG : Theme.CARD);
        txtEditPhone.setBackground(enabled ? Theme.BG : Theme.CARD);
        txtEditName.setForeground(enabled ? Theme.CREAM : Theme.SOFT);
        txtEditPhone.setForeground(enabled ? Theme.CREAM : Theme.SOFT);
        btnUpdate.setEnabled(enabled);
    }

    // ── Widget factories ──

    private JLabel detailValueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_NORMAL);
        l.setForeground(Theme.CREAM);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel detailRow(String key, JComponent value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JLabel keyLbl = new JLabel(key);
        keyLbl.setFont(Theme.FONT_BOLD);
        keyLbl.setForeground(Theme.MUTED);
        keyLbl.setPreferredSize(new Dimension(90, 20));
        row.add(keyLbl, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        return row;
    }

    private JTextArea detailWrappingText(String text) {
        JTextArea area = new JTextArea(text, 2, 1);
        area.setFont(Theme.FONT_NORMAL);
        area.setForeground(Theme.CREAM);
        area.setOpaque(false);
        area.setEditable(false);
        area.setFocusable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(null);
        area.setMargin(new Insets(0, 0, 0, 0));
        area.setMinimumSize(new Dimension(80, 38));
        area.setPreferredSize(new Dimension(150, 42));
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        return area;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BOLD);
        l.setForeground(Theme.MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField editField() {
        JTextField f = new JTextField();
        f.setFont(Theme.FONT_NORMAL);
        f.setForeground(Theme.CREAM);
        f.setBackground(Theme.BG);
        f.setCaretColor(Theme.CREAM);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            new EmptyBorder(5, 8, 5, 8)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    /**
     * A panel placed inside a JScrollPane that always follows the viewport
     * width. This keeps the details card responsive and prevents an unwanted
     * horizontal scrollbar in a normal (non-maximised) window.
     */
    private static final class WidthTrackingPanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 14;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(14, visibleRect.height - 28);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
