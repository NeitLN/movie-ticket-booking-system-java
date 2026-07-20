package movieticketbooking.ui;

import movieticketbooking.service.BookingService;
import movieticketbooking.service.MovieService;
import movieticketbooking.service.ReportService;
import movieticketbooking.service.ScreeningService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * APPLICATION MAIN FRAME CORE (Student 1 - Phase 1 Foundation)
 * -------------------------------------------------------------------------
 * This is the parent window frame of the application (MainFrame) utilizing a 
 * CardLayout content area on the right and an elegant sidebar navigation panel on the left.
 * Houses the Dashboard (storefront showcase), Movies management (JTable JScrollPane CRUD),
 * Screenings, seat booking, booking history, dashboard, and revenue-report panels.
 */
public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final JPanel sidebar;
    private final MovieService movieService;
    private final ScreeningService screeningService;
    private final ReportService reportService;
    private final BookingService bookingService;

    // Side navigation button indicators to highlight the active menu selection
    private RoundedButton activeMenuButton = null;

    public MainFrame() {
        movieService = new MovieService(); // Single shared data service loaded on startup
        screeningService = new ScreeningService(movieService); // Shared screening service (Phase 5)
        bookingService = new BookingService(screeningService); // Phase 7 — booking CRUD service (Student 3)
        screeningService.setBookingService(bookingService); // Prevent deleting screenings with confirmed bookings
        reportService = new ReportService(movieService, screeningService); // Shared read-only reporting service (Phase 6)
        
        setTitle("Bittersweet Cinemas - Movie Ticket Booking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 760));
        setSize(1366, 768);
        setLocationRelativeTo(null); // Center window on user screen

        // Root container splitting Left Sidebar and Right CardLayout Content Area
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);

        // --- LEFT COLUMN: Sidebar Navigation Panel ---
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(240, 768));
        sidebar.setBackground(Theme.NAV);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));

        // 1. Sidebar Header (Brand Logo Mark and Text)
        sidebar.add(buildSidebarLogoHeader());
        sidebar.add(Box.createVerticalStrut(10));
        
        // Horizontal marquee dotted lights divider
        DottedDivider headerDivider = new DottedDivider();
        headerDivider.setPreferredSize(new Dimension(240, 8));
        headerDivider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        sidebar.add(headerDivider);
        sidebar.add(Box.createVerticalStrut(20));

        // 2. Navigation Action Buttons Container
        JPanel navButtonsContainer = new JPanel();
        navButtonsContainer.setLayout(new BoxLayout(navButtonsContainer, BoxLayout.Y_AXIS));
        navButtonsContainer.setOpaque(false);
        navButtonsContainer.setBorder(new EmptyBorder(0, 16, 0, 16));

        // Instantiate navigation components (Student 1 required routes)
        RoundedButton btnDashboard = createNavButton("Dashboard");
        RoundedButton btnMovies = createNavButton("Movies (Admin)");
        RoundedButton btnScreenings = createNavButton("Screenings");
        RoundedButton btnBookings = createNavButton("Bookings (Seat)");
        RoundedButton btnHistory = createNavButton("Booking History");
        RoundedButton btnRevenue = createNavButton("Revenue Reports");
        RoundedButton btnExit = createNavButton("Exit");

        // Set high-fidelity custom vector icons
        btnDashboard.setIcon(new HomeIcon());
        btnMovies.setIcon(new FilmIcon());
        btnScreenings.setIcon(new CalendarIcon());
        btnBookings.setIcon(new TicketIcon());
        btnHistory.setIcon(new HistoryIcon());
        btnRevenue.setIcon(new RevenueIcon());
        btnExit.setIcon(new ExitIcon());

        // Pile them vertically with elastic spacing
        navButtonsContainer.add(btnDashboard);
        navButtonsContainer.add(Box.createVerticalStrut(12));
        navButtonsContainer.add(btnMovies);
        navButtonsContainer.add(Box.createVerticalStrut(12));
        navButtonsContainer.add(btnScreenings);
        navButtonsContainer.add(Box.createVerticalStrut(12));
        navButtonsContainer.add(btnBookings);
        navButtonsContainer.add(Box.createVerticalStrut(12));
        navButtonsContainer.add(btnHistory);
        navButtonsContainer.add(Box.createVerticalStrut(12));
        navButtonsContainer.add(btnRevenue);
        navButtonsContainer.add(Box.createVerticalStrut(12));
        navButtonsContainer.add(btnExit);
        
        sidebar.add(navButtonsContainer);
        sidebar.add(Box.createVerticalGlue()); // Elastic spring pushes everything to top
        root.add(sidebar, BorderLayout.WEST);

        // --- RIGHT COLUMN: CardLayout Main Content Panel ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        // Core Screens Construction
        DashboardPanel dashboardView = new DashboardPanel(movieService, screeningService, reportService);
        MoviePanel moviesAdminView = new MoviePanel(movieService, dashboardView, screeningService);

        ScreeningPanel screeningsView = new ScreeningPanel(screeningService, movieService);
        // Phase 7 & 8 — Student 3
        SeatLayoutPanel bookingsView = new SeatLayoutPanel(bookingService, screeningService, movieService);
        BookingHistoryPanel historyView = new BookingHistoryPanel(bookingService, screeningService, movieService);
        RevenueReportPanel revenueView = new RevenueReportPanel(reportService, movieService, screeningService);

        // Stack them into CardLayout
        contentPanel.add(dashboardView, "Dashboard");
        contentPanel.add(moviesAdminView, "Movies (Admin)");
        contentPanel.add(screeningsView, "Screenings");
        contentPanel.add(bookingsView, "Bookings (Seat)");
        contentPanel.add(historyView, "Booking History");
        contentPanel.add(revenueView, "Revenue Reports");

        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);

        // Initialize active selections
        selectNavMenu(btnDashboard, "Dashboard");

        // Event-driven navigation routing
        btnDashboard.addActionListener(e -> selectNavMenu(btnDashboard, "Dashboard"));
        btnMovies.addActionListener(e -> selectNavMenu(btnMovies, "Movies (Admin)"));
        btnScreenings.addActionListener(e -> selectNavMenu(btnScreenings, "Screenings"));
        btnBookings.addActionListener(e -> selectNavMenu(btnBookings, "Bookings (Seat)"));
        btnHistory.addActionListener(e -> selectNavMenu(btnHistory, "Booking History"));
        btnRevenue.addActionListener(e -> selectNavMenu(btnRevenue, "Revenue Reports"));
        btnExit.addActionListener(e -> {
            int verify = JOptionPane.showConfirmDialog(this, "Are you sure you want to close the system?", "Confirm Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (verify == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
    }

    /**
     * Renders brand logo and name header in sidebar with high compatibility.
     */
    private JComponent buildSidebarLogoHeader() {
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 16));
        logo.setOpaque(false);
        logo.setMaximumSize(new Dimension(240, 74));
        logo.setPreferredSize(new Dimension(240, 74));

        ImageIcon logoIcon = loadAndProcessLogo();
        JComponent mark;
        if (logoIcon != null) {
            JLabel imgLabel = new JLabel(logoIcon);
            imgLabel.setPreferredSize(new Dimension(34, 34));
            mark = imgLabel;
        } else {
            RoundedPanel fallback = new RoundedPanel(8, Theme.RED);
            fallback.setPreferredSize(new Dimension(30, 30));
            fallback.setLayout(new GridBagLayout());
            JLabel m = new JLabel("▦");
            m.setForeground(Color.WHITE);
            m.setFont(new Font("Segoe UI", Font.BOLD, 15));
            fallback.add(m);
            mark = fallback;
        }

        JLabel text = new JLabel("<html><div style='line-height:10px'>BITTERSWEET<br><span style='color:#E9B838'>CINEMAS</span></div></html>");
        text.setForeground(Theme.CREAM);
        text.setFont(Theme.FONT_LOGO);
        logo.add(mark);
        logo.add(text);
        return logo;
    }

    /**
     * Programmatic white background removal of logo image.
     */
    private ImageIcon loadAndProcessLogo() {
        try {
            File file = new File("images (1).jpg");
            if (!file.exists()) return null;
            
            java.awt.image.BufferedImage source = javax.imageio.ImageIO.read(file);
            int w = source.getWidth();
            int h = source.getHeight();
            java.awt.image.BufferedImage target = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int rgb = source.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    
                    if (r > 240 && g > 240 && b > 240) {
                        target.setRGB(x, y, 0x00FFFFFF & rgb);
                    } else {
                        target.setRGB(x, y, rgb);
                    }
                }
            }
            Image scaled = target.getScaledInstance(34, 34, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Sidebar button generator.
     */
    private RoundedButton createNavButton(String text) {
        RoundedButton btn = new RoundedButton(text, Theme.NAV, Theme.TOP_BAR, Theme.MUTED, null);
        btn.setPreferredSize(new Dimension(208, 38));
        btn.setMaximumSize(new Dimension(208, 38));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(12);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 16));
        return btn;
    }

    /**
     * Switches right card container screen and updates left button selections.
     */
    private void selectNavMenu(RoundedButton button, String cardName) {
        // Reset old selection
        if (activeMenuButton != null) {
            activeMenuButton.setForeground(Theme.MUTED);
            activeMenuButton.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 16));
        }
        
        // Highlight active selection
        activeMenuButton = button;
        activeMenuButton.setForeground(Theme.GOLD);
        activeMenuButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, Theme.GOLD), // gold left stripe
            BorderFactory.createEmptyBorder(0, 17, 0, 16) // stable padding (20px - 3px = 17px)
        ));
        
        // Refresh dashboard metrics and movie cards upon returning to storefront Dashboard
        if ("Dashboard".equalsIgnoreCase(cardName)) {
            for (Component c : contentPanel.getComponents()) {
                if (c instanceof DashboardPanel) {
                    ((DashboardPanel) c).refreshDashboard();
                    break;
                }
            }
        }

        // Refresh screening table and movie combo upon returning to Screenings
        if ("Screenings".equalsIgnoreCase(cardName)) {
            for (Component c : contentPanel.getComponents()) {
                if (c instanceof ScreeningPanel) {
                    ((ScreeningPanel) c).refreshView();
                    break;
                }
            }
        }

        // Reload movie data and preserve still-valid E5 filters on navigation.
        if ("Movies (Admin)".equalsIgnoreCase(cardName)) {
            for (Component c : contentPanel.getComponents()) {
                if (c instanceof MoviePanel) {
                    ((MoviePanel) c).refreshView();
                    break;
                }
            }
        }

        // Reload booking/report data and refresh the table upon returning to Revenue Reports
        if ("Revenue Reports".equalsIgnoreCase(cardName)) {
            for (Component c : contentPanel.getComponents()) {
                if (c instanceof RevenueReportPanel) {
                    ((RevenueReportPanel) c).refreshReport();
                    break;
                }
            }
        }

        // Refresh seat layout (screening list may have changed) — Phase 8 Student 3
        if ("Bookings (Seat)".equalsIgnoreCase(cardName)) {
            for (Component c : contentPanel.getComponents()) {
                if (c instanceof SeatLayoutPanel) {
                    ((SeatLayoutPanel) c).refreshView();
                    break;
                }
            }
        }

        // Refresh booking history table upon navigation — Phase 8 Student 3
        if ("Booking History".equalsIgnoreCase(cardName)) {
            for (Component c : contentPanel.getComponents()) {
                if (c instanceof BookingHistoryPanel) {
                    ((BookingHistoryPanel) c).refreshView();
                    break;
                }
            }
        }

        cardLayout.show(contentPanel, cardName);
        revalidate();
        repaint();
    }

    /**
     * Builds clean, beautifully styled placeholder panels for Student 2 & 3's pending phases.
     */
    private JPanel createPlaceholderPanel(String title, String description) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BG);
        
        RoundedPanel card = new RoundedPanel(16, Theme.BG_2, Theme.BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 32, 32, 32));
        card.setPreferredSize(new Dimension(600, 260));
        
        JLabel icon = new JLabel("▦");
        icon.setForeground(Theme.RED);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(Theme.CREAM);
        lblTitle.setFont(Theme.FONT_HEADING);
        lblTitle.setBorder(new EmptyBorder(16, 0, 8, 0));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblDesc = new JLabel("<html><center>" + description + "</center></html>");
        lblDesc.setBackground(Color.RED);
        lblDesc.setForeground(Theme.MUTED);
        lblDesc.setFont(Theme.FONT_NORMAL);
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(icon);
        card.add(lblTitle);
        card.add(lblDesc);
        
        panel.add(card);
        return panel;
    }
}

/**
 * 100% VECTOR-DRAWN HOMEPAGE/DASHBOARD ICON
 */
class HomeIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(c.getForeground());
        g2.setStroke(new BasicStroke(1.5f));
        // Roof
        g2.drawLine(x + 2, y + 7, x + 8, y + 2);
        g2.drawLine(x + 8, y + 2, x + 14, y + 7);
        // Base
        g2.drawRect(x + 4, y + 7, 8, 7);
        // Door
        g2.fillRect(x + 7, y + 10, 2, 4);
        g2.dispose();
    }
    @Override public int getIconWidth() { return 16; }
    @Override public int getIconHeight() { return 16; }
}

/**
 * 100% VECTOR-DRAWN FILM STRIP / MOVIES ICON
 */
class FilmIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(c.getForeground());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x + 2, y + 2, 12, 12, 2, 2);
        // Vertical frames
        g2.drawLine(x + 5, y + 2, x + 5, y + 14);
        g2.drawLine(x + 11, y + 2, x + 11, y + 14);
        // Small perforations
        g2.fillRect(x + 3, y + 4, 1, 1);
        g2.fillRect(x + 3, y + 7, 1, 1);
        g2.fillRect(x + 3, y + 10, 1, 1);
        g2.fillRect(x + 12, y + 4, 1, 1);
        g2.fillRect(x + 12, y + 7, 1, 1);
        g2.fillRect(x + 12, y + 10, 1, 1);
        g2.dispose();
    }
    @Override public int getIconWidth() { return 16; }
    @Override public int getIconHeight() { return 16; }
}

/**
 * 100% VECTOR-DRAWN CALENDAR / SCREENINGS ICON
 */
class CalendarIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(c.getForeground());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x + 2, y + 3, 12, 11, 2, 2);
        g2.drawLine(x + 2, y + 7, x + 14, y + 7);
        // Binder loops
        g2.drawLine(x + 5, y + 1, x + 5, y + 4);
        g2.drawLine(x + 11, y + 1, x + 11, y + 4);
        // Mini dots
        g2.fillRect(x + 4, y + 9, 2, 2);
        g2.fillRect(x + 7, y + 9, 2, 2);
        g2.fillRect(x + 10, y + 9, 2, 2);
        g2.dispose();
    }
    @Override public int getIconWidth() { return 16; }
    @Override public int getIconHeight() { return 16; }
}

/**
 * 100% VECTOR-DRAWN TICKET ICON
 */
class TicketIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color iconColor = c.getForeground();
        g2.setColor(iconColor);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x + 1, y + 3, 14, 10, 2, 2);
        // Cutout circle effect: draw overlapping ovals with background color
        g2.setColor(Theme.NAV);
        g2.fillOval(x - 3, y + 6, 5, 4);
        g2.fillOval(x + 14, y + 6, 5, 4);
        // Redraw cutout borders
        g2.setColor(iconColor);
        g2.setStroke(new BasicStroke(1f));
        g2.drawArc(x - 3, y + 6, 5, 4, 270, 180);
        g2.drawArc(x + 14, y + 6, 5, 4, 90, 180);
        // Dash ticket slit line
        g2.drawLine(x + 5, y + 4, x + 5, y + 12);
        g2.dispose();
    }
    @Override public int getIconWidth() { return 16; }
    @Override public int getIconHeight() { return 16; }
}

/**
 * 100% VECTOR-DRAWN HISTORY / CLOCK ICON
 */
class HistoryIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(c.getForeground());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawArc(x + 2, y + 2, 12, 12, 45, 270); // clockwise clock circle
        // Hands
        g2.drawLine(x + 8, y + 8, x + 8, y + 5);
        g2.drawLine(x + 8, y + 8, x + 11, y + 8);
        // Arrowhead
        g2.drawLine(x + 10, y + 1, x + 12, y + 3);
        g2.drawLine(x + 10, y + 5, x + 12, y + 3);
        g2.dispose();
    }
    @Override public int getIconWidth() { return 16; }
    @Override public int getIconHeight() { return 16; }
}

/**
 * 100% VECTOR-DRAWN REVENUE / BAR CHART ICON
 */
class RevenueIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(c.getForeground());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(x + 2, y + 13, x + 14, y + 13); // Axis
        g2.fillRect(x + 3, y + 9, 2, 4);
        g2.fillRect(x + 7, y + 6, 2, 7);
        g2.fillRect(x + 11, y + 3, 2, 10);
        g2.dispose();
    }
    @Override public int getIconWidth() { return 16; }
    @Override public int getIconHeight() { return 16; }
}

/**
 * 100% VECTOR-DRAWN LOGOUT / EXIT ICON
 */
class ExitIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(c.getForeground());
        g2.setStroke(new BasicStroke(1.5f));
        // Door bracket
        g2.drawLine(x + 9, y + 2, x + 3, y + 2);
        g2.drawLine(x + 3, y + 2, x + 3, y + 14);
        g2.drawLine(x + 3, y + 14, x + 9, y + 14);
        // Shaft
        g2.drawLine(x + 6, y + 8, x + 13, y + 8);
        // Tip
        g2.drawLine(x + 10, y + 5, x + 13, y + 8);
        g2.drawLine(x + 10, y + 11, x + 13, y + 8);
        g2.dispose();
    }
    @Override public int getIconWidth() { return 16; }
    @Override public int getIconHeight() { return 16; }
}
