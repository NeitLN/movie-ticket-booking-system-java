package movieticketbooking.ui;

import movieticketbooking.model.Movie;
import movieticketbooking.service.MovieService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * CUSTOMER DASHBOARD SHOWCASE PANEL (Student 1 Custom Framework)
 * -------------------------------------------------------------------------
 * This is the beautiful client-facing storefront panel developed in Phase 1.
 * Retains our trademark dark theme, interactive movie cards, Luffy logo transparent processing,
 * search box, and high-quality animated gif support.
 * 
 * DESIGN UPDATE:
 * Implements a highly professional "Capped Justified Grid Gap" calculation.
 * Dynamically adjusts the horizontal gap between movie cards so they span the available
 * screen width perfectly from left to right, capping at 30px on wide/maximized screens
 * and centering the entire grid to keep the cards elegantly grouped together.
 */
public class DashboardPanel extends JPanel {
    private final MovieService movieService;
    private final JPanel cardsContainer;
    private final JTextField searchField;
    private final JScrollPane scrollPane;
    private final JPanel scrollContentWrapper;

    public DashboardPanel(MovieService movieService) {
        this.movieService = movieService;
        
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setFocusable(true); // Enable this panel to receive focus when clicked

        // 1. Hero Promo Banner Area
        add(buildHero(), BorderLayout.NORTH);
        
        // 2. Movie List Section
        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);
        section.setBorder(new EmptyBorder(0, 20, 28, 20));

        // Top controls of movies section: Heading and Search field
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel heading = new JLabel("PHIM ĐANG CHIẾU");
        heading.setForeground(Theme.CREAM);
        heading.setFont(Theme.FONT_HEADING);
        heading.setBorder(new EmptyBorder(0, 8, 10, 0));

        searchField = new JTextField("  Tìm phim theo tên hoặc thể loại");
        searchField.setPreferredSize(new Dimension(260, 32));
        searchField.setBackground(new Color(24, 21, 19));
        searchField.setForeground(Theme.SOFT);
        searchField.setCaretColor(Theme.GOLD);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        
        // Connect the search field dynamically with the MovieService search (Extension 4)
        searchField.addActionListener(e -> performSearch());
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().trim().equals("Tìm phim theo tên hoặc thể loại")) {
                    searchField.setText("");
                    searchField.setForeground(Theme.CREAM);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setText("  Tìm phim theo tên hoặc thể loại");
                    searchField.setForeground(Theme.SOFT);
                }
            }
        });

        top.add(heading, BorderLayout.WEST);
        top.add(searchField, BorderLayout.EAST);
        section.add(top, BorderLayout.NORTH);

        // Layout flow for compact vertical movie cards (up to 6 per row)
        // Set layout to standard Left alignment first, gaps are updated dynamically in recalculateContainerHeight
        cardsContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
        cardsContainer.setOpaque(false);
        
        // Wrap cardsContainer in an outer panel that will have dynamic centered side padding applied to it
        scrollContentWrapper = new JPanel(new BorderLayout());
        scrollContentWrapper.setOpaque(false);
        scrollContentWrapper.add(cardsContainer, BorderLayout.CENTER);
        
        scrollPane = new JScrollPane(scrollContentWrapper);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        
        // Explicitly styled the vertical scrollbar of our dark theme so it remains elegant and subtle
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getVerticalScrollBar().setBackground(Theme.BG);
        
        section.add(scrollPane, BorderLayout.CENTER);
        add(section, BorderLayout.CENTER);
        
        // Register a background MouseListener (Observer Pattern) so that clicking anywhere on the panel 
        // releases focus from the text field and automatically restores the placeholder text.
        MouseAdapter focusReleaseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow(); // Request focus on background panel to release search field
            }
        };
        this.addMouseListener(focusReleaseListener);
        cardsContainer.addMouseListener(focusReleaseListener);
        scrollContentWrapper.addMouseListener(focusReleaseListener);
        scrollPane.addMouseListener(focusReleaseListener);

        // Register a resize listener to dynamically recalculate our FlowLayout wrapping height and centered padding
        scrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recalculateContainerHeight();
            }
        });
        
        // Initial card rendering
        refreshMovieCards();
    }

    /**
     * Refreshes the display of movie cards dynamically from MovieService.
     */
    public void refreshMovieCards() {
        cardsContainer.removeAll();
        List<Movie> activeMovies = movieService.getAllMovies();
        for (Movie movie : activeMovies) {
            cardsContainer.add(new MovieCard(movie));
        }
        recalculateContainerHeight();
    }

    /**
     * Dynamically calculates and sets the preferred height of the cardsContainer 
     * optimized for our original compact 6-per-row movie cards (160x275px).
     * Also dynamically calculates centered padding so cards are perfectly centered horizontally.
     */
    private void recalculateContainerHeight() {
        List<Movie> activeMovies = movieService.getAllMovies();
        int cardCount = activeMovies.size();
        if (cardCount == 0) {
            cardsContainer.setPreferredSize(new Dimension(0, 0));
            scrollContentWrapper.setBorder(new EmptyBorder(0, 0, 0, 0));
            cardsContainer.revalidate();
            cardsContainer.repaint();
            return;
        }

        // Determine available viewport width
        int width = scrollPane.getViewport().getWidth();
        if (width <= 0) {
            width = 1040; // High-compatibility fallback width for standard layouts
        }

        // Available width for the grid, leaving a small safety margin of 24px (12px on each side)
        int availWidth = width - 24;

        int cardWidth = 160; // Original high-fidelity compact card width
        int minHGap = 10;
        int vGap = 14;
        int cardHeight = 275; // Original compact card height

        // Calculate columns mathematically based on available width and minimum gap
        int cols = Math.max(1, (availWidth + minHGap) / (cardWidth + minHGap));
        // Allow up to 6 columns maximum to sit side-by-side in one row!
        if (cols > 6) {
            cols = 6;
        }
        if (cols > cardCount) {
            cols = cardCount;
        }
        
        int rows = (int) Math.ceil((double) cardCount / cols);
        
        // EXTENSION 4 & USER VISUAL REQUEST: Justify/Distribute Gaps (Dàn đều)
        // If we have more than 1 column, calculate the exact gap needed to make the cards 
        // touch both the left and right boundaries flush, leaving ZERO awkward whitespace!
        int hGap = minHGap;
        if (cols > 1) {
            int remainingRowSpace = availWidth - (cols * cardWidth);
            hGap = remainingRowSpace / (cols - 1);
            // Cap the gap to a maximum of 30px to keep them tightly grouped and cohesive!
            if (hGap > 30) {
                hGap = 30;
            }
        }
        
        // Re-apply the FlowLayout layout manager with our newly calculated justified horizontal gap
        cardsContainer.setLayout(new FlowLayout(FlowLayout.LEFT, hGap, vGap));
        
        // Calculate the exact width occupied by the grid of cards
        int totalGridWidth = cols * cardWidth + (cols - 1) * hGap;
        
        // Calculate the remaining blank space and divide it evenly on left and right for perfect centering
        int remainingSpace = width - totalGridWidth;
        int sidePadding = Math.max(8, remainingSpace / 2);
        
        // Set dynamic centered padding border on the content wrapper
        scrollContentWrapper.setBorder(new EmptyBorder(0, sidePadding, 0, sidePadding - 8)); // leave tiny gap for scrollbar
        
        int calculatedHeight = rows * cardHeight + (rows + 1) * vGap;
        cardsContainer.setPreferredSize(new Dimension(totalGridWidth, calculatedHeight));
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    /**
     * Performs a dynamic query search through MovieService (Extension 4).
     */
    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.equals("Tìm phim theo tên hoặc thể loại")) {
            query = "";
        }
        
        cardsContainer.removeAll();
        List<Movie> results = movieService.searchMovies(query);
        for (Movie movie : results) {
            cardsContainer.add(new MovieCard(movie));
        }
        
        // Recalculate centered layout height for the filtered results subset
        int cardCount = results.size();
        int width = scrollPane.getViewport().getWidth();
        if (width <= 0) width = 1040;
        
        int availWidth = width - 24;
        int cols = Math.max(1, (availWidth + 10) / (160 + 10));
        if (cols > 6) cols = 6;
        if (cols > cardCount) cols = cardCount;
        
        int rows = (int) Math.ceil((double) cardCount / cols);
        
        int hGap = 10;
        if (cols > 1) {
            int remainingRowSpace = availWidth - (cols * 160);
            hGap = remainingRowSpace / (cols - 1);
            if (hGap > 30) {
                hGap = 30;
            }
        }
        
        cardsContainer.setLayout(new FlowLayout(FlowLayout.LEFT, hGap, 14));
        
        int totalGridWidth = cols * 160 + (cols - 1) * hGap;
        int remainingSpace = width - totalGridWidth;
        int sidePadding = Math.max(8, remainingSpace / 2);
        
        scrollContentWrapper.setBorder(new EmptyBorder(0, sidePadding, 0, sidePadding - 8));
        
        int calculatedHeight = rows * 275 + (rows + 1) * 14;
        cardsContainer.setPreferredSize(new Dimension(totalGridWidth, calculatedHeight));
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private JComponent buildHero() {
        JPanel hero = new JPanel(new BorderLayout());
        hero.setOpaque(false);
        hero.setPreferredSize(new Dimension(10, 265));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 265));
        hero.setBorder(new EmptyBorder(28, 36, 18, 36));

        RoundedPanel banner = new RoundedPanel(22, Theme.BG_2, null) {
            private ImageIcon bgIcon = new java.io.File("banner_bg.gif").exists() ? new ImageIcon("banner_bg.gif") : null;

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (bgIcon != null) {
                    g2.drawImage(bgIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
                    GradientPaint gp = new GradientPaint(
                        0, 0, new Color(45, 17, 22, 180),
                        getWidth(), getHeight(), new Color(16, 18, 23, 220)
                    );
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                } else {
                    GradientPaint gp = new GradientPaint(0, 0, new Color(45, 17, 22), getWidth(), getHeight(), new Color(16, 18, 23));
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        banner.setLayout(new BorderLayout());
        banner.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        
        JLabel small = new JLabel("  ƯU ĐÃI ĐẶC BIỆT THÁNG 7");
        small.setIcon(new DiamondIcon(Theme.GOLD));
        small.setForeground(Theme.GOLD);
        small.setFont(new Font("Segoe UI", Font.BOLD, 11));
        content.add(small);
        content.add(Box.createVerticalStrut(10));

        JLabel title = new JLabel("<html>BITTERSWEET <span style='color:#E9B838'>MEMBER<br>DAY</span></html>");
        title.setForeground(Theme.CREAM);
        title.setFont(Theme.FONT_TITLE);
        content.add(title);
        content.add(Box.createVerticalStrut(8));

        JLabel subtitle = new JLabel("<html>Ưu đãi đặc biệt cho thành viên — giảm <span style='color:#E9B838'>30%</span> tất cả vé phim mọi<br>ngày trong tháng 7. Đừng bỏ lỡ!</html>");
        subtitle.setForeground(Theme.MUTED);
        subtitle.setFont(Theme.FONT_NORMAL);
        content.add(subtitle);
        content.add(Box.createVerticalStrut(18));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        RoundedButton book = new RoundedButton("Đặt vé ngay", Theme.RED, new Color(235, 48, 86), Color.WHITE, null);
        book.setPreferredSize(new Dimension(120, 38));
        RoundedButton more = new RoundedButton("Tìm hiểu thêm", new Color(33, 23, 20), new Color(48, 35, 29), Theme.GOLD, Theme.BORDER);
        more.setPreferredSize(new Dimension(128, 38));
        actions.add(book);
        actions.add(more);
        content.add(actions);

        banner.add(content, BorderLayout.WEST);
        banner.add(buildHeroPosterShapes(), BorderLayout.EAST);
        hero.add(banner, BorderLayout.CENTER);
        return hero;
    }

    private JComponent buildHeroPosterShapes() {
        JComponent shapes = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(new Color(54, 30, 37, 110));
                g2.fillRoundRect(w - 190, 12, 120, h - 24, 12, 12);
                g2.setColor(new Color(86, 29, 38, 130));
                g2.fillRoundRect(w - 150, 2, 125, h - 10, 12, 12);
                g2.setColor(new Color(111, 32, 43, 120));
                g2.fillRoundRect(w - 118, 22, 110, h - 46, 12, 12);
                g2.dispose();
            }
        };
        shapes.setPreferredSize(new Dimension(330, 190));
        return shapes;
    }
}
