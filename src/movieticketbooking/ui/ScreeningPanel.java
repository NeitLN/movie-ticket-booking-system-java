package movieticketbooking.ui;

import movieticketbooking.exception.ValidationException;
import movieticketbooking.model.Movie;
import movieticketbooking.model.Screening;
import movieticketbooking.service.MovieService;
import movieticketbooking.service.ScreeningService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * SCREENING MANAGEMENT ADMIN PANEL (Phase 5)
 * -------------------------------------------------------------------------
 * Displays screenings loaded through ScreeningService in a read-only JTable,
 * resolving movie title/duration through MovieService for display only -
 * the Screening model and TXT format are untouched.
 * Add/Edit open a modal dialog; Delete requires confirmation. All mutations
 * go through ScreeningService so conflict detection, movie-existence checks,
 * and safe file save/rollback stay owned by the service layer.
 */
public class ScreeningPanel extends JPanel {
    private final ScreeningService screeningService;
    private final MovieService movieService;

    private final JTable screeningTable;
    private final DefaultTableModel tableModel;
    private final JTextField searchField;
    private final JComboBox<MovieOption> movieFilterCombo;
    private final JLabel statusLabel;
    private final RoundedButton btnEdit;
    private final RoundedButton btnDelete;

    private List<Screening> currentRows = new ArrayList<>();

    public ScreeningPanel(ScreeningService screeningService, MovieService movieService) {
        this.screeningService = screeningService;
        this.movieService = movieService;

        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("SCREENING MANAGEMENT");
        heading.setForeground(Theme.CREAM);
        heading.setFont(Theme.FONT_HEADING);
        heading.setBorder(new EmptyBorder(0, 0, 15, 0));

        // --- Controls bar: search, movie filter, refresh, add/edit/delete ---
        JPanel controlsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        controlsBar.setOpaque(false);

        controlsBar.add(createFormLabel("Search:"));
        searchField = new JTextField();
        searchField.setBackground(Theme.BG_2);
        searchField.setForeground(Theme.CREAM);
        searchField.setCaretColor(Theme.GOLD);
        searchField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        searchField.setPreferredSize(new Dimension(180, 28));
        controlsBar.add(searchField);

        controlsBar.add(createFormLabel("Movie:"));
        movieFilterCombo = new JComboBox<>();
        Theme.styleCombo(movieFilterCombo);
        movieFilterCombo.setPreferredSize(new Dimension(220, 28));
        controlsBar.add(movieFilterCombo);

        RoundedButton btnRefresh = new RoundedButton("Refresh", Theme.NAV, Theme.TOP_BAR, Theme.GOLD, Theme.BORDER);
        btnRefresh.setPreferredSize(new Dimension(86, 28));
        controlsBar.add(btnRefresh);

        RoundedButton btnAdd = new RoundedButton("Add", Theme.RED, new Color(235, 48, 86), Color.WHITE, null);
        btnAdd.setPreferredSize(new Dimension(76, 28));
        controlsBar.add(btnAdd);

        btnEdit = new RoundedButton("Edit", new Color(33, 23, 20), new Color(48, 35, 29), Theme.GOLD, Theme.BORDER);
        btnEdit.setPreferredSize(new Dimension(76, 28));
        btnEdit.setEnabled(false);
        controlsBar.add(btnEdit);

        btnDelete = new RoundedButton("Delete", new Color(40, 20, 20), new Color(60, 20, 20), Color.RED, Theme.BORDER);
        btnDelete.setPreferredSize(new Dimension(80, 28));
        btnDelete.setEnabled(false);
        controlsBar.add(btnDelete);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(controlsBar, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // --- Table ---
        String[] columnNames = {
            "Screening ID", "Movie Title", "Date", "Start Time", "Room", "Duration (mins)", "Base Price", "End Time"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        screeningTable = new JTable(tableModel);
        screeningTable.setBackground(Theme.BG_2);
        screeningTable.setForeground(Theme.CREAM);
        screeningTable.setGridColor(Theme.BORDER);
        screeningTable.setSelectionBackground(Theme.RED);
        screeningTable.setSelectionForeground(Color.WHITE);
        screeningTable.setFont(Theme.FONT_NORMAL);
        screeningTable.getTableHeader().setBackground(Theme.TOP_BAR);
        screeningTable.getTableHeader().setForeground(Color.BLACK);
        screeningTable.getTableHeader().setFont(Theme.FONT_BOLD);
        screeningTable.setRowHeight(24);

        JScrollPane scrollPane = new JScrollPane(screeningTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scrollPane.getViewport().setBackground(Theme.BG_2);
        add(scrollPane, BorderLayout.CENTER);

        // --- Status / empty-state feedback ---
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Theme.MUTED);
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setBorder(new EmptyBorder(8, 4, 0, 0));
        add(statusLabel, BorderLayout.SOUTH);

        // --- Event wiring ---
        screeningTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        searchField.addActionListener(e -> applyFilters());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });
        movieFilterCombo.addActionListener(e -> applyFilters());

        btnRefresh.addActionListener(e -> {
            searchField.setText("");
            refreshView();
        });
        btnAdd.addActionListener(e -> openAddDialog());
        btnEdit.addActionListener(e -> openEditDialog());
        btnDelete.addActionListener(e -> deleteSelectedScreening());

        refreshView();
    }

    /**
     * Reloads the movie filter/combo choices from MovieService and re-renders the
     * table from ScreeningService's current in-memory list. Never triggers a file
     * reload or rewrite - add/update/delete already keep the in-memory list and
     * disk file consistent, so simply re-reading getAllScreenings() is sufficient.
     */
    public void refreshView() {
        populateMovieFilterCombo();
        applyFilters();
    }

    private void populateMovieFilterCombo() {
        MovieOption previous = (MovieOption) movieFilterCombo.getSelectedItem();
        String previousMovieId = previous == null ? null : previous.movieId;

        movieFilterCombo.removeAllItems();
        movieFilterCombo.addItem(MovieOption.allMovies());
        int restoreIndex = 0;
        int index = 1;
        for (Movie m : movieService.getAllMovies()) {
            movieFilterCombo.addItem(new MovieOption(m.getMovieId(), m.getTitle(), false));
            if (previousMovieId != null && previousMovieId.equalsIgnoreCase(m.getMovieId())) {
                restoreIndex = index;
            }
            index++;
        }
        movieFilterCombo.setSelectedIndex(restoreIndex);
    }

    private void applyFilters() {
        String query = searchField.getText();
        MovieOption filter = (MovieOption) movieFilterCombo.getSelectedItem();
        String filterMovieId = (filter == null) ? null : filter.movieId;

        List<Screening> rows = new ArrayList<>();
        for (Screening s : screeningService.getAllScreenings()) {
            if (filterMovieId != null && !s.getMovieId().equalsIgnoreCase(filterMovieId)) {
                continue;
            }
            Movie movie = findMovie(s.getMovieId());
            String title = movie != null ? movie.getTitle() : "";
            if (matchesQuery(s, title, query)) {
                rows.add(s);
            }
        }
        renderRows(rows);
    }

    /**
     * Case-insensitive, null/blank-safe match across screening ID, movie ID,
     * resolved movie title, and room. Extracted static for isolated testing.
     */
    static boolean matchesQuery(Screening s, String movieTitle, String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String q = query.toLowerCase().trim();
        return s.getScreeningId().toLowerCase().contains(q)
            || s.getMovieId().toLowerCase().contains(q)
            || (movieTitle != null && movieTitle.toLowerCase().contains(q))
            || s.getRoom().toLowerCase().contains(q);
    }

    private void renderRows(List<Screening> rows) {
        currentRows = rows;
        tableModel.setRowCount(0);
        for (Screening s : rows) {
            Movie movie = findMovie(s.getMovieId());
            String title = movie != null ? movie.getTitle() : "(missing movie: " + s.getMovieId() + ")";
            Object duration = movie != null ? movie.getDuration() : "N/A";
            String endTime = movie != null
                ? s.getStartDateTime().plusMinutes(movie.getDuration()).toLocalTime().toString()
                : "N/A";
            tableModel.addRow(new Object[]{
                s.getScreeningId(),
                title,
                s.getScreeningDate().toString(),
                s.getStartTime().toString(),
                s.getRoom(),
                duration,
                s.getBasePrice(),
                endTime
            });
        }
        updateStatusLabel(rows.size());
        updateButtonStates();
    }

    private void updateStatusLabel(int count) {
        if (count == 0) {
            statusLabel.setText("No screenings found. Click Add to create one.");
        } else {
            statusLabel.setText("Showing " + count + " screening" + (count == 1 ? "" : "s") + ".");
        }
    }

    private void updateButtonStates() {
        boolean hasSelection = getSelectedScreening() != null;
        btnEdit.setEnabled(hasSelection);
        btnDelete.setEnabled(hasSelection);
    }

    private Screening getSelectedScreening() {
        int viewRow = screeningTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = screeningTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= currentRows.size()) {
            return null;
        }
        return currentRows.get(modelRow);
    }

    private Movie findMovie(String movieId) {
        for (Movie m : movieService.getAllMovies()) {
            if (m.getMovieId().equalsIgnoreCase(movieId)) {
                return m;
            }
        }
        return null;
    }

    private void openAddDialog() {
        openScreeningDialog(null);
    }

    private void openEditDialog() {
        Screening selected = getSelectedScreening();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a screening to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Screening current = screeningService.getScreeningById(selected.getScreeningId());
        if (current == null) {
            JOptionPane.showMessageDialog(this, "This screening no longer exists. The table has been refreshed.", "Screening Not Found", JOptionPane.WARNING_MESSAGE);
            refreshView();
            return;
        }
        openScreeningDialog(current);
    }

    private void deleteSelectedScreening() {
        Screening selected = getSelectedScreening();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a screening to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Screening current = screeningService.getScreeningById(selected.getScreeningId());
        if (current == null) {
            JOptionPane.showMessageDialog(this, "This screening no longer exists. The table has been refreshed.", "Screening Not Found", JOptionPane.WARNING_MESSAGE);
            refreshView();
            return;
        }

        Movie movie = findMovie(current.getMovieId());
        String movieLabel = movie != null ? movie.getTitle() : current.getMovieId() + " (missing movie)";
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete screening " + current.getScreeningId() + "?\n" +
            "Movie: " + movieLabel + "\n" +
            "Date/Time: " + current.getScreeningDate() + " " + current.getStartTime() + "\n" +
            "Room: " + current.getRoom(),
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (!isDeletionConfirmed(confirm)) {
            return;
        }

        try {
            screeningService.deleteScreening(current.getScreeningId());
            JOptionPane.showMessageDialog(this, "Screening deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshView();
        } catch (ValidationException ex) {
            showError(this, ex.getMessage());
        } catch (UncheckedIOException ex) {
            showFileError(this, ex);
        }
    }

    /**
     * Extracted so delete-confirmation logic is testable without automating a real
     * modal JOptionPane.
     */
    static boolean isDeletionConfirmed(int dialogResult) {
        return dialogResult == JOptionPane.YES_OPTION;
    }

    private void openScreeningDialog(Screening existing) {
        boolean isEdit = existing != null;
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, isEdit ? "Edit Screening" : "Add Screening", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());

        RoundedPanel form = new RoundedPanel(16, Theme.BG_2, Theme.BORDER);
        form.setLayout(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(createFormLabel("Movie *:"), gbc);
        JComboBox<MovieOption> movieCombo = new JComboBox<>();
        for (Movie m : movieService.getAllMovies()) {
            movieCombo.addItem(new MovieOption(m.getMovieId(), m.getTitle(), false));
        }
        Theme.styleCombo(movieCombo);
        gbc.gridx = 1;
        form.add(movieCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(createFormLabel("Date *:"), gbc);
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        gbc.gridx = 1;
        form.add(dateSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(createFormLabel("Start Time *:"), gbc);
        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));
        gbc.gridx = 1;
        form.add(timeSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        form.add(createFormLabel("Room *:"), gbc);
        JTextField roomField = new JTextField();
        roomField.setBackground(Theme.BG);
        roomField.setForeground(Theme.CREAM);
        roomField.setCaretColor(Theme.GOLD);
        roomField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        gbc.gridx = 1;
        form.add(roomField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        form.add(createFormLabel("Base Price *:"), gbc);
        JTextField priceField = new JTextField();
        priceField.setBackground(Theme.BG);
        priceField.setForeground(Theme.CREAM);
        priceField.setCaretColor(Theme.GOLD);
        priceField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        gbc.gridx = 1;
        form.add(priceField, gbc);

        // Pre-fill values for edit mode
        if (isEdit) {
            boolean movieFound = false;
            for (int i = 0; i < movieCombo.getItemCount(); i++) {
                MovieOption option = movieCombo.getItemAt(i);
                if (option.movieId.equalsIgnoreCase(existing.getMovieId())) {
                    movieCombo.setSelectedIndex(i);
                    movieFound = true;
                    break;
                }
            }
            if (!movieFound) {
                MovieOption missing = new MovieOption(existing.getMovieId(), "", true);
                movieCombo.addItem(missing);
                movieCombo.setSelectedItem(missing);
            }
            dateSpinner.setValue(toDate(existing.getScreeningDate()));
            timeSpinner.setValue(toDate(existing.getStartTime()));
            roomField.setText(existing.getRoom());
            priceField.setText(String.valueOf(existing.getBasePrice()));
        } else {
            dateSpinner.setValue(toDate(LocalDate.now()));
            timeSpinner.setValue(toDate(LocalTime.now().withSecond(0).withNano(0)));
        }

        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        buttonsRow.setOpaque(false);
        RoundedButton btnOk = new RoundedButton(isEdit ? "Save" : "Add", Theme.RED, new Color(235, 48, 86), Color.WHITE, null);
        btnOk.setPreferredSize(new Dimension(86, 32));
        RoundedButton btnCancel = new RoundedButton("Cancel", Theme.NAV, Theme.TOP_BAR, Theme.MUTED, Theme.BORDER);
        btnCancel.setPreferredSize(new Dimension(86, 32));
        buttonsRow.add(btnOk);
        buttonsRow.add(btnCancel);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        form.add(buttonsRow, gbc);

        dialog.add(form, BorderLayout.CENTER);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnOk.addActionListener(e -> {
            try {
                MovieOption selectedMovie = (MovieOption) movieCombo.getSelectedItem();
                Date dateVal = (Date) dateSpinner.getValue();
                Date timeVal = (Date) timeSpinner.getValue();
                String room = roomField.getText();
                String priceText = priceField.getText();
                String id = isEdit ? existing.getScreeningId() : screeningService.generateNextScreeningId();

                Screening candidate = buildScreeningFromForm(id, selectedMovie, dateVal, timeVal, room, priceText);

                if (isEdit) {
                    screeningService.updateScreening(candidate);
                } else {
                    screeningService.addScreening(candidate);
                }

                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                    isEdit ? "Screening updated successfully." : "Screening added successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshView();
            } catch (ValidationException ex) {
                showError(dialog, ex.getMessage());
            } catch (UncheckedIOException ex) {
                showFileError(dialog, ex);
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Parses and validates raw form input into a Screening candidate. Extracted as a
     * static, package-private helper so form-to-model logic can be unit-tested without
     * instantiating Swing components.
     */
    static Screening buildScreeningFromForm(String screeningId, MovieOption movie, Date datePart, Date timePart, String roomText, String priceText) throws ValidationException {
        if (movie == null) {
            throw new ValidationException("Please select a movie.");
        }
        if (datePart == null) {
            throw new ValidationException("Screening date is required.");
        }
        if (timePart == null) {
            throw new ValidationException("Screening start time is required.");
        }
        String room = roomText == null ? "" : roomText.trim();
        if (room.isEmpty()) {
            throw new ValidationException("Room is required.");
        }
        double price;
        try {
            price = Double.parseDouble(priceText == null ? "" : priceText.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Ticket price must be a valid number.");
        }
        if (!Double.isFinite(price) || price <= 0) {
            throw new ValidationException("Ticket price must be a finite number greater than zero.");
        }
        LocalDate date = datePart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime time = timePart.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
        return new Screening(screeningId, movie.movieId, date, time, room, price);
    }

    private static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static Date toDate(LocalTime time) {
        return Date.from(java.time.LocalDateTime.of(LocalDate.now(), time).atZone(ZoneId.systemDefault()).toInstant());
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.MUTED);
        label.setFont(Theme.FONT_BOLD);
        return label;
    }

    private void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showFileError(Component parent, UncheckedIOException ex) {
        JOptionPane.showMessageDialog(parent,
            "Unable to save screening data: " + ex.getMessage(),
            "File Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display option for the movie combo/filter. toString() drives the default
     * JComboBox renderer, so no custom ListCellRenderer is needed.
     */
    static final class MovieOption {
        final String movieId; // null denotes the "All Movies" filter sentinel
        final String title;
        final boolean missing;

        MovieOption(String movieId, String title, boolean missing) {
            this.movieId = movieId;
            this.title = title;
            this.missing = missing;
        }

        static MovieOption allMovies() {
            return new MovieOption(null, "All Movies", false);
        }

        @Override
        public String toString() {
            if (movieId == null) {
                return "All Movies";
            }
            return missing ? (movieId + " — [Missing Movie]") : (movieId + " — " + title);
        }
    }
}
