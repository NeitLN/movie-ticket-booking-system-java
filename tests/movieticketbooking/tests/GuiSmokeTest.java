package movieticketbooking.tests;

import movieticketbooking.model.Booking;
import movieticketbooking.model.Seat;
import movieticketbooking.ui.BookingHistoryPanel;
import movieticketbooking.ui.MainFrame;
import movieticketbooking.ui.SeatLayoutPanel;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Constructs the full GUI and verifies the two Phase 8 screens, including the
 * read-only state used when a cancelled booking is selected.
 */
public final class GuiSmokeTest {
    private GuiSmokeTest() {}

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setSize(1366, 768);
                frame.setVisible(true);

                JPanel content = getField(frame, "contentPanel", JPanel.class);
                CardLayout cards = (CardLayout) content.getLayout();

                verifySeatBookingScreen(frame, content, cards);
                verifyBookingHistoryStates(frame, content, cards);

                frame.dispose();
            } catch (ReflectiveOperationException ex) {
                throw new AssertionError("Unable to inspect MainFrame integration.", ex);
            }
        });
        System.out.println("PASS: Booking and booking-history screens rendered with valid states.");
    }

    private static void verifySeatBookingScreen(MainFrame frame,
                                                JPanel content,
                                                CardLayout cards) {
        cards.show(content, "Bookings (Seat)");
        layoutFrame(frame, content);

        SeatLayoutPanel bookingPanel = findComponent(content, SeatLayoutPanel.class);
        if (bookingPanel == null || !bookingPanel.isVisible()) {
            throw new AssertionError("Booking panel was not displayed.");
        }
        layoutRecursively(bookingPanel);
        assertValidBounds(bookingPanel);
    }

    private static void verifyBookingHistoryStates(MainFrame frame,
                                                   JPanel content,
                                                   CardLayout cards)
            throws ReflectiveOperationException {
        // Verify the normal, non-maximised submission window size rather than
        // relying only on a maximised desktop.
        frame.setExtendedState(javax.swing.JFrame.NORMAL);
        frame.setSize(1280, 760);
        cards.show(content, "Booking History");
        layoutFrame(frame, content);

        BookingHistoryPanel historyPanel = findComponent(content, BookingHistoryPanel.class);
        if (historyPanel == null || !historyPanel.isVisible()) {
            throw new AssertionError("Booking History panel was not displayed.");
        }
        layoutRecursively(historyPanel);
        assertValidBounds(historyPanel);

        JScrollPane detailsScroll = getField(
            historyPanel, "detailScrollPane", JScrollPane.class);
        JScrollPane tableScroll = getField(
            historyPanel, "tableScrollPane", JScrollPane.class);
        JSplitPane split = getField(
            historyPanel, "bodySplitPane", JSplitPane.class);
        JPanel detailsPanel = getField(
            historyPanel, "detailPanel", JPanel.class);

        check(detailsScroll.getHorizontalScrollBarPolicy()
                == ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
            "Booking details must not create a horizontal scrollbar.");
        check(tableScroll.getHorizontalScrollBarPolicy()
                == ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
            "Booking table must resize to the available width.");
        check(detailsPanel.getWidth() <= detailsScroll.getViewport().getExtentSize().width + 1,
            "Booking details content must track the viewport width.");
        check(split.getRightComponent().getX() + split.getRightComponent().getWidth()
                <= split.getWidth() + 1,
            "Booking details pane must remain inside the split-pane bounds.");

        Method populate = BookingHistoryPanel.class.getDeclaredMethod(
            "populateDetailPanel", Booking.class);
        populate.setAccessible(true);

        JTextField name = getField(historyPanel, "txtEditName", JTextField.class);
        JTextField phone = getField(historyPanel, "txtEditPhone", JTextField.class);
        JButton update = getField(historyPanel, "btnUpdate", JButton.class);
        JButton cancel = getField(historyPanel, "btnCancel", JButton.class);

        Booking cancelledBooking = new Booking(
            "BKG998", "SCR001", "Cancelled User", "0901234567",
            List.of(new Seat("A1", true)), 90000.0, "CANCELLED");
        populate.invoke(historyPanel, cancelledBooking);

        check(!name.isEditable() && !phone.isEditable(),
            "Cancelled booking contact fields must be read-only.");
        check(!update.isEnabled() && !cancel.isEnabled(),
            "Cancelled booking actions must be disabled.");
        check(cancel.getCursor().getType() == Cursor.DEFAULT_CURSOR,
            "Disabled booking action must not use a hand cursor.");

        Booking confirmedBooking = new Booking(
            "BKG997", "SCR001", "Confirmed User", "0912345678",
            List.of(new Seat("A2", true)), 90000.0, "CONFIRMED");
        populate.invoke(historyPanel, confirmedBooking);

        check(name.isEditable() && phone.isEditable(),
            "Confirmed booking contact fields must remain editable.");
        check(update.isEnabled() && cancel.isEnabled(),
            "Confirmed booking actions must remain enabled.");
        check("Save Changes".equals(update.getText()) && "Cancel Booking".equals(cancel.getText()),
            "Action labels must use supported text without missing-glyph boxes.");
    }

    private static void layoutFrame(MainFrame frame, JPanel content) {
        frame.validate();
        frame.doLayout();
        content.doLayout();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void layoutRecursively(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container) {
                layoutRecursively((Container) child);
            }
        }
    }

    private static void assertValidBounds(Component component) {
        if (component.isVisible() && (component.getWidth() < 0 || component.getHeight() < 0)) {
            throw new AssertionError(
                "Invalid bounds for " + component.getClass().getSimpleName() + ": " + component.getBounds());
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                assertValidBounds(child);
            }
        }
    }

    private static <T extends Component> T findComponent(Container root, Class<T> type) {
        for (Component child : root.getComponents()) {
            if (type.isInstance(child)) return type.cast(child);
            if (child instanceof Container) {
                T nested = findComponent((Container) child, type);
                if (nested != null) return nested;
            }
        }
        return null;
    }

    private static <T> T getField(Object target, String name, Class<T> type)
            throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }
}
