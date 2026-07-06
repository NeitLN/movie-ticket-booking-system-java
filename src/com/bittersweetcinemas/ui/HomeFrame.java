package com.bittersweetcinemas.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class HomeFrame extends JFrame {
    public HomeFrame() {
        setTitle("Bittersweet Cinemas - UI Template");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 720));
        setSize(1366, 768);
        setLocationRelativeTo(null);
        setContentPane(buildRoot());
    }

    private JComponent buildRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);
        return root;
    }

    private JComponent buildHeader() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Theme.NAV);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.TOP_BAR);
        topBar.setPreferredSize(new Dimension(10, 30));
        topBar.setBorder(new EmptyBorder(0, 22, 0, 22));

        JLabel left = new JLabel("◆ Tin mới & Ưu đãi      ◆ Vé của tôi      ◆ Hệ thống rạp");
        left.setForeground(Theme.MUTED);
        left.setFont(Theme.FONT_NORMAL);
        JLabel right = new JLabel("Đăng nhập / Đăng ký");
        right.setForeground(Theme.CREAM);
        right.setFont(Theme.FONT_BOLD);
        topBar.add(left, BorderLayout.WEST);
        topBar.add(right, BorderLayout.EAST);

        JPanel nav = new JPanel(new BorderLayout());
        nav.setPreferredSize(new Dimension(10, 54));
        nav.setBackground(Theme.NAV);
        nav.setBorder(new EmptyBorder(0, 22, 0, 22));

        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        leftNav.setOpaque(false);
        leftNav.add(buildLogo());
        leftNav.add(navItem("PHIM"));
        leftNav.add(navItem("RẠP"));
        leftNav.add(navItem("THÀNH VIÊN"));
        leftNav.add(navItem("ƯU ĐÃI"));

        RoundedButton buyNow = new RoundedButton("MUA VÉ NGAY", Theme.RED, new Color(235, 48, 86), Color.WHITE, null);
        buyNow.setPreferredSize(new Dimension(130, 34));
        buyNow.addActionListener(e -> JOptionPane.showMessageDialog(this, "Mở màn hình đặt vé tại đây."));

        JPanel buttonWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        buttonWrap.setOpaque(false);
        buttonWrap.add(buyNow);

        nav.add(leftNav, BorderLayout.WEST);
        nav.add(buttonWrap, BorderLayout.EAST);

        DottedDivider divider = new DottedDivider();
        divider.setPreferredSize(new Dimension(10, 8));

        wrapper.add(topBar);
        wrapper.add(nav);
        wrapper.add(divider);
        return wrapper;
    }

    private JComponent buildLogo() {
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        logo.setOpaque(false);
        logo.setPreferredSize(new Dimension(190, 54));

        RoundedPanel mark = new RoundedPanel(8, Theme.RED);
        mark.setPreferredSize(new Dimension(30, 30));
        mark.setLayout(new GridBagLayout());
        JLabel m = new JLabel("▦");
        m.setForeground(Color.WHITE);
        m.setFont(new Font("Segoe UI", Font.BOLD, 15));
        mark.add(m);

        JLabel text = new JLabel("<html><div style='line-height:10px'>BITTERSWEET<br><span style='color:#E9B838'>CINEMAS</span></div></html>");
        text.setForeground(Theme.CREAM);
        text.setFont(Theme.FONT_LOGO);
        logo.add(mark);
        logo.add(text);
        return logo;
    }

    private JLabel navItem(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.CREAM);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setBorder(new EmptyBorder(19, 0, 0, 0));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return label;
    }

    private JComponent buildBody() {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Theme.BG);
        body.add(buildHero());
        body.add(buildMoviesSection());
        body.add(Box.createVerticalGlue());

        scrollPane.setViewportView(body);
        return scrollPane;
    }

    private JComponent buildHero() {
        JPanel hero = new JPanel(new BorderLayout());
        hero.setOpaque(false);
        hero.setPreferredSize(new Dimension(10, 265));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 265));
        hero.setBorder(new EmptyBorder(28, 36, 18, 36));

        RoundedPanel banner = new RoundedPanel(22, Theme.BG_2, null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(45, 17, 22), getWidth(), getHeight(), new Color(16, 18, 23));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        banner.setLayout(new BorderLayout());
        banner.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel small = new JLabel("◆  ƯU ĐÃI ĐẶC BIỆT THÁNG 7");
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

    private JComponent buildMoviesSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);
        section.setBorder(new EmptyBorder(0, 20, 28, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel heading = new JLabel("PHIM ĐANG CHIẾU");
        heading.setForeground(Theme.CREAM);
        heading.setFont(Theme.FONT_HEADING);
        heading.setBorder(new EmptyBorder(0, 8, 0, 0));

        JTextField search = new JTextField("  Tìm phim theo tên hoặc thể loại");
        search.setPreferredSize(new Dimension(260, 32));
        search.setBackground(new Color(24, 21, 19));
        search.setForeground(Theme.SOFT);
        search.setCaretColor(Theme.GOLD);
        search.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        top.add(heading, BorderLayout.WEST);
        top.add(search, BorderLayout.EAST);
        section.add(top, BorderLayout.NORTH);

        JPanel cards = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 14));
        cards.setOpaque(false);
        List<Movie> movies = Arrays.asList(
            new Movie("Quỷ Ám Kẻ Vô Thần", "Kinh dị / Hồi hộp", 118, "T18", 7.8),
            new Movie("Vùng Đất Câm Lặng 3", "Kinh dị / Khoa học viễn tưởng", 104, "T16", 8.1),
            new Movie("Hành Trình Cuối Cùng", "Hành động / Phiêu lưu", 132, "T13", 7.5),
            new Movie("Mắt Bão", "Tâm lý / Chính kịch", 127, "T16", 8.4),
            new Movie("Thám Tử Không Tên", "Hành động / Bí ẩn", 115, "T13", 7.9),
            new Movie("Ánh Sáng Cuối Đường", "Tình cảm / Gia đình", 98, "P", 7.2)
        );
        for (Movie movie : movies) {
            cards.add(new MovieCard(movie));
        }
        section.add(cards, BorderLayout.CENTER);
        return section;
    }
}
