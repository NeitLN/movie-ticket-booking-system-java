package movieticketbooking.ui;

import movieticketbooking.exception.ValidationException;
import movieticketbooking.model.Movie;
import movieticketbooking.service.MovieService;
import movieticketbooking.service.ScreeningService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * ADMIN MOVIE MANAGEMENT CONTROL PANEL (Scaffold for Student 2 / Implemented by Student 1)
 * -------------------------------------------------------------------------
 * Fulfills all functional constraints of Phase 4: Movie Management.
 * Renders a data-editing form, a fully functional interactive JTable displaying
 * all records loaded from movies.txt, and CRUD buttons (Add, Update, Delete, Clear, Search, Sort).
 * Employs clean Event-driven action listeners and input validation checks.
 */
public class MoviePanel extends JPanel {
    private final MovieService movieService;
    private final DashboardPanel dashboardPanel; // Reference to refresh storefront cards
    private final ScreeningService screeningService; // Phase 5: deletion integrity guard

    private final JTable movieTable;
    private final DefaultTableModel tableModel;
    
    // Form input fields
    private final JTextField idField;
    private final JTextField titleField;
    private final JTextField genreField;
    private final JTextField durationField;
    private final JComboBox<String> ageRatingCombo;
    private final JTextField scoreField;
    private final JTextField posterField;
    private final JTextField searchField;
    private final JComboBox<String> sortCombo;

    public MoviePanel(MovieService movieService, DashboardPanel dashboardPanel, ScreeningService screeningService) {
        this.movieService = movieService;
        this.dashboardPanel = dashboardPanel;
        this.screeningService = screeningService;

        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Heading Label
        JLabel heading = new JLabel("MOVIE MANAGEMENT (ADMIN)");
        heading.setForeground(Theme.CREAM);
        heading.setFont(Theme.FONT_HEADING);
        heading.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(heading, BorderLayout.NORTH);

        // Split Layout: Left is input form, Right is JTable and controls
        JPanel mainSplit = new JPanel(new GridBagLayout());
        mainSplit.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // --- LEFT COLUMN: Input Form Panel ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 0, 15);
        
        RoundedPanel formPanel = new RoundedPanel(16, Theme.BG_2, Theme.BORDER);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        
        GridBagConstraints fGbc = new GridBagConstraints();
        fGbc.fill = GridBagConstraints.HORIZONTAL;
        fGbc.insets = new Insets(6, 4, 6, 4);
        fGbc.weightx = 1.0;

        // Form Row 0: Movie ID
        fGbc.gridx = 0; fGbc.gridy = 0;
        formPanel.add(createFormLabel("Movie ID (Auto):"), fGbc);
        idField = new JTextField();
        idField.setBackground(Theme.BG);
        idField.setForeground(Theme.CREAM);
        idField.setCaretColor(Theme.GOLD);
        idField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        idField.setPreferredSize(new Dimension(100, 28));
        idField.setEditable(false); // Locked for automatic index incrementing
        fGbc.gridx = 1;
        formPanel.add(idField, fGbc);

        // Form Row 1: Title
        fGbc.gridx = 0; fGbc.gridy = 1;
        formPanel.add(createFormLabel("Movie Title * :"), fGbc);
        titleField = new JTextField();
        titleField.setBackground(Theme.BG);
        titleField.setForeground(Theme.CREAM);
        titleField.setCaretColor(Theme.GOLD);
        titleField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        titleField.setPreferredSize(new Dimension(100, 28));
        fGbc.gridx = 1;
        formPanel.add(titleField, fGbc);

        // Form Row 2: Genre
        fGbc.gridx = 0; fGbc.gridy = 2;
        formPanel.add(createFormLabel("Genre * :"), fGbc);
        genreField = new JTextField();
        genreField.setBackground(Theme.BG);
        genreField.setForeground(Theme.CREAM);
        genreField.setCaretColor(Theme.GOLD);
        genreField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        genreField.setPreferredSize(new Dimension(100, 28));
        fGbc.gridx = 1;
        formPanel.add(genreField, fGbc);

        // Form Row 3: Duration
        fGbc.gridx = 0; fGbc.gridy = 3;
        formPanel.add(createFormLabel("Duration (mins) * :"), fGbc);
        durationField = new JTextField();
        durationField.setBackground(Theme.BG);
        durationField.setForeground(Theme.CREAM);
        durationField.setCaretColor(Theme.GOLD);
        durationField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        durationField.setPreferredSize(new Dimension(100, 28));
        fGbc.gridx = 1;
        formPanel.add(durationField, fGbc);

        // Form Row 4: Age Rating
        fGbc.gridx = 0; fGbc.gridy = 4;
        formPanel.add(createFormLabel("Age Rating * :"), fGbc);
        ageRatingCombo = new JComboBox<>(new String[]{"P", "T13", "T16", "T18"});
        ageRatingCombo.setBackground(Theme.BG);
        ageRatingCombo.setForeground(Theme.CREAM);
        fGbc.gridx = 1;
        formPanel.add(ageRatingCombo, fGbc);

        // Form Row 5: Score
        fGbc.gridx = 0; fGbc.gridy = 5;
        formPanel.add(createFormLabel("Rating Score (e.g. 8.4):"), fGbc);
        scoreField = new JTextField();
        scoreField.setBackground(Theme.BG);
        scoreField.setForeground(Theme.CREAM);
        scoreField.setCaretColor(Theme.GOLD);
        scoreField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scoreField.setPreferredSize(new Dimension(100, 28));
        fGbc.gridx = 1;
        formPanel.add(scoreField, fGbc);

        // Form Row 6: Poster File Path
        fGbc.gridx = 0; fGbc.gridy = 6;
        formPanel.add(createFormLabel("Poster File Path:"), fGbc);
        posterField = new JTextField();
        posterField.setBackground(Theme.BG);
        posterField.setForeground(Theme.CREAM);
        posterField.setCaretColor(Theme.GOLD);
        posterField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        posterField.setPreferredSize(new Dimension(100, 28));
        fGbc.gridx = 1;
        formPanel.add(posterField, fGbc);

        // Form Row 7: Form Control Buttons Stack
        fGbc.gridx = 0; fGbc.gridy = 7;
        fGbc.gridwidth = 2;
        fGbc.weighty = 1.0;
        fGbc.fill = GridBagConstraints.NONE;
        fGbc.anchor = GridBagConstraints.SOUTH;
        
        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        formButtons.setOpaque(false);
        
        RoundedButton btnAdd = new RoundedButton("Add", Theme.RED, new Color(235, 48, 86), Color.WHITE, null);
        btnAdd.setPreferredSize(new Dimension(76, 32));
        RoundedButton btnUpdate = new RoundedButton("Update", new Color(33, 23, 20), new Color(48, 35, 29), Theme.GOLD, Theme.BORDER);
        btnUpdate.setPreferredSize(new Dimension(86, 32));
        RoundedButton btnDelete = new RoundedButton("Delete", new Color(40, 20, 20), new Color(60, 20, 20), Color.RED, Theme.BORDER);
        btnDelete.setPreferredSize(new Dimension(86, 32));
        RoundedButton btnClear = new RoundedButton("Clear", Theme.NAV, Theme.TOP_BAR, Theme.MUTED, Theme.BORDER);
        btnClear.setPreferredSize(new Dimension(76, 32));
        
        formButtons.add(btnAdd);
        formButtons.add(btnUpdate);
        formButtons.add(btnDelete);
        formButtons.add(btnClear);
        formPanel.add(formButtons, fGbc);

        mainSplit.add(formPanel, gbc);

        // --- RIGHT COLUMN: Table View & Search Filter Panel ---
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        gbc.insets = new Insets(0, 0, 0, 0);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);

        // Top Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);
        filterBar.setBorder(new EmptyBorder(0, 0, 10, 0));

        filterBar.add(createFormLabel("Search:"));
        searchField = new JTextField();
        searchField.setBackground(Theme.BG_2);
        searchField.setForeground(Theme.CREAM);
        searchField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        searchField.setPreferredSize(new Dimension(160, 28));
        searchField.setCaretColor(Theme.GOLD);
        filterBar.add(searchField);

        RoundedButton btnSearch = new RoundedButton("Search", Theme.NAV, Theme.TOP_BAR, Theme.GOLD, Theme.BORDER);
        btnSearch.setPreferredSize(new Dimension(86, 28));
        filterBar.add(btnSearch);

        filterBar.add(createFormLabel("Sort by:"));
        sortCombo = new JComboBox<>(new String[]{"None", "Name (A-Z)", "Name (Z-A)", "Duration (Short-Long)", "Duration (Long-Short)", "Genre (A-Z)"});
        sortCombo.setBackground(Theme.BG_2);
        sortCombo.setForeground(Theme.CREAM);
        filterBar.add(sortCombo);

        tablePanel.add(filterBar, BorderLayout.NORTH);

        // JTable Configuration
        String[] columnNames = {"ID", "Title", "Genre", "Duration (mins)", "Age Rating", "Score", "Poster"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        movieTable = new JTable(tableModel);
        movieTable.setBackground(Theme.BG_2);
        movieTable.setForeground(Theme.CREAM);
        movieTable.setGridColor(Theme.BORDER);
        movieTable.setSelectionBackground(Theme.RED);
        movieTable.setSelectionForeground(Color.WHITE);
        movieTable.setFont(Theme.FONT_NORMAL);
        movieTable.getTableHeader().setBackground(Theme.TOP_BAR);
        movieTable.getTableHeader().setForeground(Theme.CREAM);
        movieTable.getTableHeader().setFont(Theme.FONT_BOLD);
        movieTable.setRowHeight(24);

        JScrollPane scrollPane = new JScrollPane(movieTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scrollPane.getViewport().setBackground(Theme.BG_2);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        mainSplit.add(tablePanel, gbc);
        add(mainSplit, BorderLayout.CENTER);

        // --- REGISTER ACTION LISTENERS & EVENTS ---
        
        movieTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = movieTable.getSelectedRow();
            if (selectedRow >= 0) {
                idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
                titleField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                genreField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                durationField.setText(tableModel.getValueAt(selectedRow, 3).toString());
                ageRatingCombo.setSelectedItem(tableModel.getValueAt(selectedRow, 4).toString());
                scoreField.setText(tableModel.getValueAt(selectedRow, 5).toString());
                posterField.setText(tableModel.getValueAt(selectedRow, 6).toString());
            }
        });

        btnAdd.addActionListener(e -> {
            try {
                String nextId = movieService.generateNextMovieId();
                String title = titleField.getText().trim();
                String genre = genreField.getText().trim();
                int duration = Integer.parseInt(durationField.getText().trim());
                String ageRating = ageRatingCombo.getSelectedItem().toString();
                
                double score = parseOptionalScore();
                
                String poster = posterField.getText().trim();
                
                Movie movie = new Movie(nextId, title, genre, duration, ageRating, score, poster);
                movieService.addMovie(movie);
                
                JOptionPane.showMessageDialog(this, "Movie successfully added: " + title, "Success", JOptionPane.INFORMATION_MESSAGE);
                clearInputs();
                refreshTable(movieService.getAllMovies());
                dashboardPanel.refreshMovieCards();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Duration and score fields must contain valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (UncheckedIOException ex) {
                showFileError(ex);
            }
        });

        btnUpdate.addActionListener(e -> {
            String id = idField.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a movie from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String title = titleField.getText().trim();
                String genre = genreField.getText().trim();
                int duration = Integer.parseInt(durationField.getText().trim());
                String ageRating = ageRatingCombo.getSelectedItem().toString();
                
                double score = parseOptionalScore();
                
                String poster = posterField.getText().trim();
                
                Movie movie = new Movie(id, title, genre, duration, ageRating, score, poster);
                movieService.updateMovie(movie);
                
                JOptionPane.showMessageDialog(this, "Movie successfully updated: " + title, "Success", JOptionPane.INFORMATION_MESSAGE);
                clearInputs();
                refreshTable(movieService.getAllMovies());
                dashboardPanel.refreshMovieCards();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Duration and score fields must contain valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (UncheckedIOException ex) {
                showFileError(ex);
            }
        });

        btnDelete.addActionListener(e -> {
            String id = idField.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a movie from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (screeningService.hasScreeningsForMovie(id)) {
                JOptionPane.showMessageDialog(this,
                    "This movie cannot be deleted because it has existing screenings.",
                    "Deletion Blocked", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int option = JOptionPane.showConfirmDialog(this,
                "Are you absolutely sure you want to delete this movie?", 
                "Verify Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (option == JOptionPane.YES_OPTION) {
                try {
                    movieService.deleteMovie(id);
                    JOptionPane.showMessageDialog(this, "Movie successfully deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearInputs();
                    refreshTable(movieService.getAllMovies());
                    dashboardPanel.refreshMovieCards();
                } catch (ValidationException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                } catch (UncheckedIOException ex) {
                    showFileError(ex);
                }
            }
        });

        btnClear.addActionListener(e -> clearInputs());

        btnSearch.addActionListener(e -> {
            String query = searchField.getText().trim();
            List<Movie> results = movieService.searchMovies(query);
            refreshTable(results);
        });

        sortCombo.addActionListener(e -> {
            int selectedIndex = sortCombo.getSelectedIndex();
            List<Movie> sorted;
            switch (selectedIndex) {
                case 1:
                    sorted = movieService.getMoviesSortedByName(true);
                    break;
                case 2:
                    sorted = movieService.getMoviesSortedByName(false);
                    break;
                case 3:
                    sorted = movieService.getMoviesSortedByDuration(true);
                    break;
                case 4:
                    sorted = movieService.getMoviesSortedByDuration(false);
                    break;
                case 5:
                    sorted = movieService.getMoviesSortedByGenre(true);
                    break;
                default:
                    sorted = movieService.getAllMovies();
                    break;
            }
            refreshTable(sorted);
        });

        refreshTable(movieService.getAllMovies());
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.MUTED);
        label.setFont(Theme.FONT_BOLD);
        return label;
    }

    private void clearInputs() {
        movieTable.clearSelection();
        idField.setText("");
        titleField.setText("");
        genreField.setText("");
        durationField.setText("");
        ageRatingCombo.setSelectedIndex(0);
        scoreField.setText("");
        posterField.setText("");
    }

    private double parseOptionalScore() {
        String value = scoreField.getText().trim();
        if (value.isEmpty()) {
            return 0.0;
        }
        double score = Double.parseDouble(value);
        if (!Double.isFinite(score)) {
            throw new NumberFormatException("Score must be finite.");
        }
        return score;
    }

    private void showFileError(UncheckedIOException ex) {
        JOptionPane.showMessageDialog(this,
            "Could not save changes. Please check the data file and try again.\n" + ex.getMessage(),
            "File Error", JOptionPane.ERROR_MESSAGE);
    }

    private void refreshTable(List<Movie> list) {
        tableModel.setRowCount(0);
        for (Movie m : list) {
            tableModel.addRow(new Object[]{
                m.getMovieId(),
                m.getTitle(),
                m.getGenre(),
                m.getDuration(),
                m.getAgeRating(),
                m.getScore(),
                m.getPosterPath()
            });
        }
    }
}
