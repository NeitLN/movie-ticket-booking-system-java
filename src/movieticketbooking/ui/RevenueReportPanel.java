package movieticketbooking.ui;

import movieticketbooking.exception.ValidationException;
import movieticketbooking.model.Movie;
import movieticketbooking.service.MovieService;
import movieticketbooking.service.ReportService;
import movieticketbooking.util.FormatUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * REVENUE REPORTS PANEL (Phase 6)
 * -------------------------------------------------------------------------
 * Read-only reporting surface over ReportService. Never creates, edits, or
 * deletes bookings - it only displays aggregates and lets the user filter
 * by movie and screening date. All business math (revenue eligibility,
 * aggregation, date filtering, unresolved-record handling) lives in
 * ReportService; this panel only renders what the service returns.
 */
public class RevenueReportPanel extends JPanel {
    private final ReportService reportService;
    private final MovieService movieService;

    private final JLabel grossRevenueValue;
    private final JLabel confirmedBookingsValue;
    private final JLabel ticketsSoldValue;
    private final JLabel averageBookingValue;

    private final JComboBox<ScreeningPanel.MovieOption> movieFilterCombo;
    private final JCheckBox useStartDate;
    private final JCheckBox useEndDate;
    private final JSpinner startDateSpinner;
    private final JSpinner endDateSpinner;

    private final DefaultTableModel tableModel;
    private final JTable revenueTable;
    private final JLabel statusLabel;
    private final JLabel unresolvedLabel;
    private final JLabel errorLabel;

    public RevenueReportPanel(ReportService reportService, MovieService movieService) {
        this.reportService = reportService;
        this.movieService = movieService;

        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("REVENUE REPORTS");
        heading.setForeground(Theme.CREAM);
        heading.setFont(Theme.FONT_HEADING);
        heading.setBorder(new EmptyBorder(0, 0, 15, 0));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(heading);

        // --- Summary cards ---
        JPanel summaryRow = new JPanel(new GridLayout(1, 4, 12, 0));
        summaryRow.setOpaque(false);
        summaryRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        grossRevenueValue = new JLabel("0 ₫");
        confirmedBookingsValue = new JLabel("0");
        ticketsSoldValue = new JLabel("0");
        averageBookingValue = new JLabel("0 ₫");

        summaryRow.add(createStatCard("Gross Revenue", grossRevenueValue));
        summaryRow.add(createStatCard("Confirmed Bookings", confirmedBookingsValue));
        summaryRow.add(createStatCard("Tickets Sold", ticketsSoldValue));
        summaryRow.add(createStatCard("Average Booking Value", averageBookingValue));

        top.add(Box.createVerticalStrut(12));
        top.add(summaryRow);
        top.add(Box.createVerticalStrut(14));

        // --- Filters ---
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterBar.setOpaque(false);
        filterBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        filterBar.add(createFormLabel("Movie:"));
        movieFilterCombo = new JComboBox<>();
        movieFilterCombo.setBackground(Theme.BG_2);
        movieFilterCombo.setForeground(Theme.CREAM);
        movieFilterCombo.setPreferredSize(new Dimension(200, 28));
        filterBar.add(movieFilterCombo);

        filterBar.add(createFormLabel("Screening date from:"));
        useStartDate = new JCheckBox();
        useStartDate.setOpaque(false);
        filterBar.add(useStartDate);
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd"));
        startDateSpinner.setEnabled(false);
        filterBar.add(startDateSpinner);

        filterBar.add(createFormLabel("to:"));
        useEndDate = new JCheckBox();
        useEndDate.setOpaque(false);
        filterBar.add(useEndDate);
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));
        endDateSpinner.setEnabled(false);
        filterBar.add(endDateSpinner);

        RoundedButton btnApply = new RoundedButton("Apply", Theme.RED, new Color(235, 48, 86), Color.WHITE, null);
        btnApply.setPreferredSize(new Dimension(80, 28));
        filterBar.add(btnApply);

        RoundedButton btnReset = new RoundedButton("Reset", Theme.NAV, Theme.TOP_BAR, Theme.MUTED, Theme.BORDER);
        btnReset.setPreferredSize(new Dimension(80, 28));
        filterBar.add(btnReset);

        RoundedButton btnRefresh = new RoundedButton("Refresh", Theme.NAV, Theme.TOP_BAR, Theme.GOLD, Theme.BORDER);
        btnRefresh.setPreferredSize(new Dimension(86, 28));
        filterBar.add(btnRefresh);

        top.add(filterBar);
        add(top, BorderLayout.NORTH);

        // --- Table ---
        String[] columnNames = {"Movie", "Screening ID", "Screening Date", "Confirmed Bookings", "Tickets Sold", "Gross Revenue"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        revenueTable = new JTable(tableModel);
        revenueTable.setBackground(Theme.BG_2);
        revenueTable.setForeground(Theme.CREAM);
        revenueTable.setGridColor(Theme.BORDER);
        revenueTable.setSelectionBackground(Theme.RED);
        revenueTable.setSelectionForeground(Color.WHITE);
        revenueTable.setFont(Theme.FONT_NORMAL);
        revenueTable.getTableHeader().setBackground(Theme.TOP_BAR);
        revenueTable.getTableHeader().setForeground(Theme.CREAM);
        revenueTable.getTableHeader().setFont(Theme.FONT_BOLD);
        revenueTable.setRowHeight(24);

        JScrollPane scrollPane = new JScrollPane(revenueTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scrollPane.getViewport().setBackground(Theme.BG_2);
        add(scrollPane, BorderLayout.CENTER);

        // --- Data-quality / status feedback ---
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Theme.MUTED);
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        unresolvedLabel = new JLabel(" ");
        unresolvedLabel.setForeground(Theme.GOLD);
        unresolvedLabel.setFont(Theme.FONT_SMALL);
        unresolvedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Theme.RED);
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottom.add(statusLabel);
        bottom.add(unresolvedLabel);
        bottom.add(errorLabel);
        bottom.setBorder(new EmptyBorder(8, 4, 0, 0));
        add(bottom, BorderLayout.SOUTH);

        // --- Event wiring ---
        useStartDate.addItemListener(e -> startDateSpinner.setEnabled(useStartDate.isSelected()));
        useEndDate.addItemListener(e -> endDateSpinner.setEnabled(useEndDate.isSelected()));
        btnApply.addActionListener(e -> applyFilters());
        btnReset.addActionListener(e -> resetFilters());
        btnRefresh.addActionListener(e -> refreshReport());

        populateMovieFilterCombo();
        updateErrorLabel();
        applyFilters();
    }

    /** Reloads bookings from disk (never rewriting the file), then re-renders. Wired to the Refresh button and MainFrame's nav hook. */
    public void refreshReport() {
        reportService.reloadBookings();
        updateErrorLabel();
        if (reportService.isLastLoadFailed()) {
            return; // preserve the last valid table/cards rather than overwriting with a false empty/zero state
        }
        populateMovieFilterCombo();
        applyFilters();
    }

    private void updateErrorLabel() {
        if (reportService.isLastLoadFailed()) {
            errorLabel.setText("Error: " + reportService.getLastLoadErrorMessage());
        } else {
            errorLabel.setText(" ");
        }
    }

    private void populateMovieFilterCombo() {
        ScreeningPanel.MovieOption previous = (ScreeningPanel.MovieOption) movieFilterCombo.getSelectedItem();
        String previousMovieId = previous == null ? null : previous.movieId;

        movieFilterCombo.removeAllItems();
        movieFilterCombo.addItem(ScreeningPanel.MovieOption.allMovies());
        int restoreIndex = 0;
        int index = 1;
        for (Movie m : movieService.getAllMovies()) {
            movieFilterCombo.addItem(new ScreeningPanel.MovieOption(m.getMovieId(), m.getTitle(), false));
            if (previousMovieId != null && previousMovieId.equalsIgnoreCase(m.getMovieId())) {
                restoreIndex = index;
            }
            index++;
        }
        movieFilterCombo.setSelectedIndex(restoreIndex);
    }

    private void applyFilters() {
        ScreeningPanel.MovieOption selected = (ScreeningPanel.MovieOption) movieFilterCombo.getSelectedItem();
        String movieId = (selected == null) ? null : selected.movieId;
        LocalDate start = useStartDate.isSelected() ? toLocalDate((Date) startDateSpinner.getValue()) : null;
        LocalDate end = useEndDate.isSelected() ? toLocalDate((Date) endDateSpinner.getValue()) : null;

        try {
            ReportService.ReportFilter filter = ReportService.ReportFilter.of(movieId, start, end);
            ReportService.ReportData data = reportService.getReportData(filter);
            renderReportData(data);
        } catch (ValidationException ex) {
            showError(ex.getMessage());
        }
    }

    private void resetFilters() {
        movieFilterCombo.setSelectedIndex(0);
        useStartDate.setSelected(false);
        useEndDate.setSelected(false);
        startDateSpinner.setValue(toDate(LocalDate.now()));
        endDateSpinner.setValue(toDate(LocalDate.now()));
        applyFilters();
    }

    private void renderReportData(ReportService.ReportData data) {
        tableModel.setRowCount(0);
        for (ReportService.ScreeningRevenueRow row : data.getRows()) {
            tableModel.addRow(new Object[]{
                row.getMovieLabel(),
                row.getScreeningId(),
                row.getScreeningDate() != null ? row.getScreeningDate().toString() : "N/A",
                row.getConfirmedBookings(),
                row.getTicketsSold(),
                FormatUtils.formatVnd(row.getGrossRevenue())
            });
        }

        grossRevenueValue.setText(FormatUtils.formatVnd(data.getGrossRevenue()));
        confirmedBookingsValue.setText(String.valueOf(data.getConfirmedBookingCount()));
        ticketsSoldValue.setText(String.valueOf(data.getTicketsSold()));
        averageBookingValue.setText(FormatUtils.formatVnd(data.getAverageBookingValue()));

        int rowCount = data.getRows().size();
        if (rowCount == 0) {
            statusLabel.setText("No confirmed bookings found for the current filter.");
        } else {
            statusLabel.setText("Showing " + rowCount + " screening" + (rowCount == 1 ? "" : "s") + ".");
        }

        StringBuilder warning = new StringBuilder();
        int unresolvedTotal = reportService.getUnresolvedBookingCount();
        if (unresolvedTotal > 0) {
            warning.append(unresolvedTotal).append(" booking").append(unresolvedTotal == 1 ? "" : "s")
                .append(" reference a missing screening or movie.");
        }
        if (data.getUnresolvedExcludedCount() > 0) {
            if (warning.length() > 0) {
                warning.append(' ');
            }
            warning.append(data.getUnresolvedExcludedCount())
                .append(data.getUnresolvedExcludedCount() == 1 ? " of them was" : " of them were")
                .append(" excluded from this date range because its screening date is unknown.");
        }
        unresolvedLabel.setText(warning.length() == 0 ? " " : warning.toString());
    }

    private JComponent createStatCard(String title, JLabel valueLabel) {
        RoundedPanel card = new RoundedPanel(14, Theme.BG_2, Theme.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Theme.MUTED);
        titleLabel.setFont(Theme.FONT_SMALL);

        valueLabel.setForeground(Theme.CREAM);
        valueLabel.setFont(Theme.FONT_HEADING);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.MUTED);
        label.setFont(Theme.FONT_BOLD);
        return label;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
