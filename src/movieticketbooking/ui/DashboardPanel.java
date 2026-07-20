package movieticketbooking.ui;

import movieticketbooking.model.Movie;
import movieticketbooking.service.MovieService;
import movieticketbooking.service.ReportService;
import movieticketbooking.service.ScreeningService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

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
    private final ScreeningService screeningService;
    private final ReportService reportService;
    private final JPanel cardsContainer;
    private final JTextField searchField;
    private final JScrollPane scrollPane;
    private final JPanel scrollContentWrapper;

    private JLabel totalMoviesValue;
    private JLabel totalScreeningsValue;
    private JLabel upcomingScreeningsValue;
    private JLabel confirmedBookingsValue;
    private JLabel ticketsSoldValue;
    private JLabel grossRevenueValue;
    private JLabel statsErrorLabel;

    public DashboardPanel(MovieService movieService, ScreeningService screeningService, ReportService reportService) {
        this.movieService = movieService;
        this.screeningService = screeningService;
        this.reportService = reportService;

        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setFocusable(true); // Enable this panel to receive focus when clicked

        // 1. Hero Promo Banner Area + 1b. Real-data stats bar (Phase 6), stacked vertically
        JPanel topStack = new JPanel();
        topStack.setOpaque(false);
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
        topStack.add(buildHero());
        topStack.add(buildStatsBar());
        add(topStack, BorderLayout.NORTH);

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
     * Refreshes the display of movie cards dynamically from MovieService, and the
     * real-data stats bar alongside it (both read already-in-memory service state -
     * no disk I/O here; see refreshDashboard() for the disk-reloading variant).
     */
    public void refreshMovieCards() {
        cardsContainer.removeAll();
        List<Movie> activeMovies = movieService.getAllMovies();
        for (Movie movie : activeMovies) {
            cardsContainer.add(new MovieCard(movie));
        }
        recalculateContainerHeight();
        refreshStats();
    }

    /**
     * Full dashboard refresh: reloads bookings from disk (never rewriting the file)
     * via ReportService, then re-renders stats and movie cards. Wired to MainFrame's
     * Dashboard navigation hook so opening the Dashboard always shows current data.
     */
    public void refreshDashboard() {
        reportService.reloadBookings();
        refreshMovieCards();
    }

    private JComponent buildStatsBar() {
        JPanel bar = new JPanel(new GridLayout(1, 6, 10, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 20, 14, 20));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalMoviesValue = new JLabel("0");
        totalScreeningsValue = new JLabel("0");
        upcomingScreeningsValue = new JLabel("0");
        confirmedBookingsValue = new JLabel("0");
        ticketsSoldValue = new JLabel("0");
        grossRevenueValue = new JLabel("0 ₫");

        // Matching labels exactly to your image: "Total Movies", "Screenings", "Upcoming", "Confirmed Bookings", "Tickets Sold", "Gross Revenue"
        bar.add(createStatCard("Total Movies", totalMoviesValue, new StatMovieIcon()));
        bar.add(createStatCard("Screenings", totalScreeningsValue, new StatScreeningIcon()));
        bar.add(createStatCard("Upcoming", upcomingScreeningsValue, new StatUpcomingIcon()));
        bar.add(createStatCard("Confirmed Bookings", confirmedBookingsValue, new StatBookingIcon()));
        bar.add(createStatCard("Tickets Sold", ticketsSoldValue, new StatTicketIcon()));
        bar.add(createStatCard("Gross Revenue", grossRevenueValue, new StatRevenueIcon()));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        statsErrorLabel = new JLabel(" ");
        statsErrorLabel.setForeground(Theme.RED);
        statsErrorLabel.setFont(Theme.FONT_SMALL);
        statsErrorLabel.setBorder(new EmptyBorder(0, 24, 0, 24));

        wrapper.add(bar, BorderLayout.CENTER);
        wrapper.add(statsErrorLabel, BorderLayout.SOUTH);
        return wrapper;
    }

    private JComponent createStatCard(String title, JLabel valueLabel, Icon icon) {
        RoundedPanel card = new RoundedPanel(14, Theme.BG_2, Theme.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(8, 10, 8, 10)); // Compact padding to maximize text space

        // Left icon container (WEST)
        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setBorder(new EmptyBorder(0, 0, 0, 6)); // Compact 6px gap to prevent truncation
            card.add(iconLabel, BorderLayout.WEST);
        }

        // Right text container (CENTER)
        JPanel textContainer = new JPanel();
        textContainer.setOpaque(false);
        textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Theme.MUTED);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // 10px compact font to guarantee zero truncation

        valueLabel.setForeground(Theme.CREAM);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Adjusted value font size

        textContainer.add(titleLabel);
        textContainer.add(Box.createVerticalStrut(2));
        textContainer.add(valueLabel);

        card.add(textContainer, BorderLayout.CENTER);

        return card;
    }

    /**
     * Recomputes all six stat values from current in-memory service state.
     * Movie/screening counts always reflect current state (their services keep
     * memory and disk in sync on every CRUD operation). Booking/revenue values
     * reflect whatever ReportService currently holds - if its last reload failed,
     * a clear error is shown instead of a misleading zero.
     */
    private void refreshStats() {
        totalMoviesValue.setText(String.valueOf(movieService.getAllMovies().size()));
        totalScreeningsValue.setText(String.valueOf(screeningService.getAllScreenings().size()));
        upcomingScreeningsValue.setText(String.valueOf(screeningService.getUpcomingScreeningCount()));

        if (reportService.isLastLoadFailed()) {
            confirmedBookingsValue.setText("N/A");
            ticketsSoldValue.setText("N/A");
            grossRevenueValue.setText("N/A");
            statsErrorLabel.setText("Error: " + reportService.getLastLoadErrorMessage());
        } else {
            confirmedBookingsValue.setText(String.valueOf(reportService.getConfirmedBookingCount()));
            ticketsSoldValue.setText(String.valueOf(reportService.getTicketsSold()));
            grossRevenueValue.setText(formatVnd(reportService.getGrossRevenue()));
            statsErrorLabel.setText(" ");
        }
    }

    /**
     * Formats the BigDecimal directly (DecimalFormat has a dedicated BigDecimal code path) so
     * arbitrarily large finite totals never get narrowed through long/double and can never throw
     * ArithmeticException/overflow - only the displayed text is rounded (HALF_UP), not the underlying value.
     */
    private static String formatVnd(BigDecimal amount) {
        DecimalFormat format = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.US));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(amount) + " ₫";
    }

    /**
     * Dynamically calculates and sets the preferred height of the cardsContainer 
     * optimized for our original compact 6-per-row movie cards (160x275px).
     * Also dynamically calculates centered padding so cards are perfectly centered horizontally.
     */
    private void recalculateContainerHeight() {
        int cardCount = cardsContainer.getComponentCount(); // Dynamically look at actual added cards
        if (cardCount == 0) {
            cardsContainer.setPreferredSize(new Dimension(0, 0));
            scrollContentWrapper.setBorder(new EmptyBorder(0, 0, 0, 0));
            cardsContainer.revalidate();
            cardsContainer.repaint();
            scrollContentWrapper.revalidate();
            scrollContentWrapper.repaint();
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
        int cardHeight = 260; // Original compact card height

        // Calculate columns mathematically based on available width and minimum gap
        int cols = Math.max(1, (availWidth + minHGap) / (cardWidth + minHGap));
        // Allow up to 6 columns maximum to sit side-by-side in one row!
        if (cols > 6) {
            cols = 6;
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

        // Propagate size changes to the wrapper & viewport so scrollbars are instantly updated
        scrollContentWrapper.revalidate();
        scrollContentWrapper.repaint();
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
        
        // Delegate completely to our highly robust, generic layout engine!
        // This ensures the grid and scrollbars are always 100% identical and stable.
        recalculateContainerHeight();
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
                // Call super first to let the component prepare and paint standard properties
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Clip the painting to the rounded corners so the video/GIF doesn't bleed out
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 22, 22));
                
                if (bgIcon != null) {
                    g2.drawImage(bgIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
                    // Beautiful horizontal gradient: solid/dark on the left for perfect text readability,
                    // fading to highly transparent on the right to let the video shine clearly.
                    GradientPaint gp = new GradientPaint(
                        0, 0, new Color(26, 22, 20, 240), // Dark theme color (BG_2) with high opacity
                        getWidth() * 0.7f, 0, new Color(26, 22, 20, 45) // Fades to very transparent on the right
                    );
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                } else {
                    GradientPaint gp = new GradientPaint(0, 0, new Color(45, 17, 22), getWidth(), getHeight(), new Color(16, 18, 23));
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                }
                g2.dispose();
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

        banner.add(content, BorderLayout.WEST);
        hero.add(banner, BorderLayout.CENTER);
        return hero;
    }
}

/**
 * 100% VECTOR-DRAWN STATS: MOVIE ICON
 */
class StatMovieIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.GOLD); // Unified gold color matching your reference image
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(x + 2, y + 2, 16, 16);
        // Inner film reel holes
        g2.fillOval(x + 5, y + 5, 3, 3);
        g2.fillOval(x + 12, y + 5, 3, 3);
        g2.fillOval(x + 5, y + 12, 3, 3);
        g2.fillOval(x + 12, y + 12, 3, 3);
        g2.fillOval(x + 8, y + 8, 4, 4);
        g2.dispose();
    }
    @Override public int getIconWidth() { return 20; }
    @Override public int getIconHeight() { return 20; }
}

/**
 * 100% VECTOR-DRAWN STATS: SCREENINGS ICON
 */
class StatScreeningIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.GOLD); // Unified gold color matching your reference image
        g2.setStroke(new BasicStroke(1.5f));
        // Film strip fragment
        g2.drawRoundRect(x + 2, y + 2, 16, 16, 2, 2);
        g2.drawLine(x + 5, y + 2, x + 5, y + 18);
        g2.drawLine(x + 15, y + 2, x + 15, y + 18);
        g2.fillRect(x + 3, y + 4, 1, 1);
        g2.fillRect(x + 3, y + 8, 1, 1);
        g2.fillRect(x + 3, y + 12, 1, 1);
        g2.fillRect(x + 16, y + 4, 1, 1);
        g2.fillRect(x + 16, y + 8, 1, 1);
        g2.fillRect(x + 16, y + 12, 1, 1);
        g2.dispose();
    }
    @Override public int getIconWidth() { return 20; }
    @Override public int getIconHeight() { return 20; }
}

/**
 * 100% VECTOR-DRAWN STATS: UPCOMING SCREENINGS ICON
 */
class StatUpcomingIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.GOLD); // Unified gold color matching your reference image
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(x + 2, y + 2, 16, 16);
        // Clock hands indicating 3 o'clock
        g2.drawLine(x + 10, y + 10, x + 10, y + 5);
        g2.drawLine(x + 10, y + 10, x + 14, y + 10);
        g2.dispose();
    }
    @Override public int getIconWidth() { return 20; }
    @Override public int getIconHeight() { return 20; }
}

/**
 * 100% VECTOR-DRAWN STATS: BOOKING ICON
 */
class StatBookingIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.GOLD); // Unified gold color matching your reference image
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(x + 2, y + 2, 16, 16);
        // Checkmark
        g2.drawLine(x + 6, y + 10, x + 9, y + 13);
        g2.drawLine(x + 9, y + 13, x + 14, y + 7);
        g2.dispose();
    }
    @Override public int getIconWidth() { return 20; }
    @Override public int getIconHeight() { return 20; }
}

/**
 * 100% VECTOR-DRAWN STATS: TICKET ICON
 */
class StatTicketIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.GOLD); // Unified gold color matching your reference image
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x + 2, y + 4, 16, 12, 2, 2);
        // Cutout notch circle effect using BG_2 as mask
        g2.setColor(Theme.BG_2);
        g2.fillOval(x - 2, y + 8, 6, 4);
        g2.fillOval(x + 16, y + 8, 6, 4);
        // Redraw cutout borders
        g2.setColor(Theme.GOLD);
        g2.setStroke(new BasicStroke(1f));
        g2.drawArc(x - 2, y + 8, 6, 4, 270, 180);
        g2.drawArc(x + 16, y + 8, 6, 4, 90, 180);
        // Dashed slot
        g2.drawLine(x + 7, y + 5, x + 7, y + 15);
        g2.dispose();
    }
    @Override public int getIconWidth() { return 20; }
    @Override public int getIconHeight() { return 20; }
}

/**
 * 100% VECTOR-DRAWN STATS: REVENUE ICON
 */
class StatRevenueIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.GOLD); // Unified gold color matching your reference image
        g2.setStroke(new BasicStroke(1.5f));
        // Coin 1 (bottom)
        g2.drawOval(x + 3, y + 12, 14, 5);
        g2.drawLine(x + 3, y + 14, x + 3, y + 10);
        g2.drawLine(x + 17, y + 14, x + 17, y + 10);
        // Coin 2 (middle)
        g2.drawOval(x + 3, y + 8, 14, 5);
        g2.drawLine(x + 3, y + 10, x + 3, y + 6);
        g2.drawLine(x + 17, y + 10, x + 17, y + 6);
        // Coin 3 (top)
        g2.drawOval(x + 3, y + 4, 14, 5);
        g2.fillOval(x + 3, y + 4, 14, 5);
        g2.dispose();
    }
    @Override public int getIconWidth() { return 20; }
    @Override public int getIconHeight() { return 20; }
}
