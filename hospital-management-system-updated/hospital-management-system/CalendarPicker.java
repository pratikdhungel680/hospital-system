import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A month-view calendar for picking a date, styled to match the app theme.
 *
 * - Click a day (or use the arrow keys / Page Up / Page Down) to select it.
 * - Today is ringed, the selected day is filled, past days are muted.
 * - Days that already have appointments get a dot and a tooltip with the count
 *   (feed it with {@link #setAppointmentCounts(Map)}).
 * - The first day of the week follows the system locale.
 */
public class CalendarPicker extends JPanel {
    private static final int WEEKDAY_ROW_HEIGHT = 26;
    private static final int CELL_WIDTH = 40;
    private static final int CELL_HEIGHT = 32;

    private static final Color HOVER_BG = new Color(0xE6F1EF);
    private static final Color OUTSIDE_MONTH = new Color(0xB8C4C7);
    private static final Color PAST_DAY = new Color(0x8FA0A6);

    private final DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
    private final List<Consumer<LocalDate>> listeners = new ArrayList<>();
    private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
    private final DayGrid grid = new DayGrid();

    private Map<LocalDate, Integer> appointmentCounts = new HashMap<>();
    private LocalDate selected;
    private YearMonth shown;

    public CalendarPicker() {
        this(LocalDate.now());
    }

    public CalendarPicker(LocalDate initial) {
        this.selected = initial;
        this.shown = YearMonth.from(initial);

        setOpaque(false);
        setLayout(new BorderLayout(0, 6));

        monthLabel.setFont(UITheme.subheading());
        monthLabel.setForeground(UITheme.TEXT_PRIMARY);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(navButton("\u00AB", "Previous year", -12));
        left.add(navButton("\u2039", "Previous month", -1));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(navButton("\u203A", "Next month", 1));
        right.add(navButton("\u00BB", "Next year", 12));

        JPanel nav = new JPanel(new BorderLayout());
        nav.setOpaque(false);
        nav.add(left, BorderLayout.WEST);
        nav.add(monthLabel, BorderLayout.CENTER);
        nav.add(right, BorderLayout.EAST);

        LinkButton todayBtn = new LinkButton("Jump to today", UITheme.ACCENT, UITheme.ACCENT_HOVER);
        todayBtn.setFont(UITheme.small().deriveFont(Font.BOLD));
        todayBtn.addActionListener(e -> select(LocalDate.now()));

        String dot = String.format("#%06X", UITheme.ACCENT.getRGB() & 0xFFFFFF);
        JLabel legend = new JLabel("<html><font color='" + dot + "'>\u25CF</font> has appointments</html>");
        legend.setFont(UITheme.small());
        legend.setForeground(UITheme.TEXT_SECONDARY);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(todayBtn, BorderLayout.WEST);
        footer.add(legend, BorderLayout.EAST);

        add(nav, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        updateMonthLabel();
    }

    // ---------------------------------------------------------------- public API

    public LocalDate getSelectedDate() {
        return selected;
    }

    /** Selects a date, jumping to its month, and notifies listeners if it changed. */
    public void setSelectedDate(LocalDate date) {
        select(date);
    }

    /** Dates with at least one appointment get a dot; the count is shown as a tooltip. */
    public void setAppointmentCounts(Map<LocalDate, Integer> counts) {
        this.appointmentCounts = new HashMap<>(counts);
        grid.repaint();
    }

    public void addDateChangeListener(Consumer<LocalDate> listener) {
        listeners.add(listener);
    }

    // ------------------------------------------------------------------ internals

    private LinkButton navButton(String glyph, String tooltip, int months) {
        LinkButton b = new LinkButton(glyph, UITheme.PRIMARY, UITheme.ACCENT);
        b.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 18));
        b.setPreferredSize(new Dimension(30, 28));
        b.setToolTipText(tooltip);
        b.setFocusable(false); // keyboard users navigate months with Page Up / Page Down on the grid
        b.addActionListener(e -> {
            shown = shown.plusMonths(months);
            updateMonthLabel();
            grid.repaint();
        });
        return b;
    }

    private void select(LocalDate date) {
        boolean changed = !date.equals(selected);
        selected = date;
        shown = YearMonth.from(date);
        updateMonthLabel();
        grid.repaint();
        if (changed) {
            for (Consumer<LocalDate> l : new ArrayList<>(listeners)) l.accept(date);
        }
    }

    private void updateMonthLabel() {
        monthLabel.setText(shown.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + shown.getYear());
    }

    /** The date shown in the top-left cell of the 6x7 grid. */
    private LocalDate gridStart() {
        LocalDate first = shown.atDay(1);
        int offset = (first.getDayOfWeek().getValue() - firstDayOfWeek.getValue() + 7) % 7;
        return first.minusDays(offset);
    }

    private void bind(JComponent c, int keyCode, int modifiers, String name, Runnable action) {
        c.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(keyCode, modifiers), name);
        c.getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    /** The clickable 6-week grid, including the weekday header row. */
    private class DayGrid extends JPanel {
        private int hoverIndex = -1;

        DayGrid() {
            setOpaque(false);
            setFocusable(true);
            getAccessibleContext().setAccessibleName("Calendar days");
            ToolTipManager.sharedInstance().registerComponent(this);

            bind(this, KeyEvent.VK_LEFT, 0, "prevDay", () -> select(selected.minusDays(1)));
            bind(this, KeyEvent.VK_RIGHT, 0, "nextDay", () -> select(selected.plusDays(1)));
            bind(this, KeyEvent.VK_UP, 0, "prevWeek", () -> select(selected.minusWeeks(1)));
            bind(this, KeyEvent.VK_DOWN, 0, "nextWeek", () -> select(selected.plusWeeks(1)));
            bind(this, KeyEvent.VK_PAGE_UP, 0, "prevMonth", () -> select(selected.minusMonths(1)));
            bind(this, KeyEvent.VK_PAGE_DOWN, 0, "nextMonth", () -> select(selected.plusMonths(1)));
            bind(this, KeyEvent.VK_PAGE_UP, InputEvent.CTRL_DOWN_MASK, "prevYear", () -> select(selected.minusYears(1)));
            bind(this, KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_DOWN_MASK, "nextYear", () -> select(selected.plusYears(1)));
            bind(this, KeyEvent.VK_HOME, 0, "today", () -> select(LocalDate.now()));

            addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                    int idx = indexAt(e.getPoint());
                    if (idx >= 0) select(gridStart().plusDays(idx));
                }
                @Override public void mouseExited(MouseEvent e) { setHover(-1); }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) { setHover(indexAt(e.getPoint())); }
            });
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e) { repaint(); }
            });
        }

        private void setHover(int index) {
            if (index != hoverIndex) {
                hoverIndex = index;
                repaint();
            }
        }

        /** Cell index (0-41) under a point, or -1 if the point is outside the day cells. */
        private int indexAt(Point p) {
            if (p.y < WEEKDAY_ROW_HEIGHT || getWidth() <= 0 || getHeight() <= WEEKDAY_ROW_HEIGHT) return -1;
            float cw = getWidth() / 7f;
            float ch = (getHeight() - WEEKDAY_ROW_HEIGHT) / 6f;
            int col = (int) (p.x / cw);
            int row = (int) ((p.y - WEEKDAY_ROW_HEIGHT) / ch);
            if (col < 0 || col > 6 || row < 0 || row > 5) return -1;
            return row * 7 + col;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(7 * CELL_WIDTH, WEEKDAY_ROW_HEIGHT + 6 * CELL_HEIGHT);
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            int idx = indexAt(e.getPoint());
            if (idx < 0) return null;
            Integer n = appointmentCounts.get(gridStart().plusDays(idx));
            if (n == null || n <= 0) return null;
            return n + (n == 1 ? " appointment" : " appointments");
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            float cw = getWidth() / 7f;
            float ch = (getHeight() - WEEKDAY_ROW_HEIGHT) / 6f;

            // Weekday header
            g2.setFont(UITheme.small().deriveFont(Font.BOLD));
            g2.setColor(UITheme.TEXT_SECONDARY);
            for (int c = 0; c < 7; c++) {
                String name = firstDayOfWeek.plus(c).getDisplayName(TextStyle.SHORT, Locale.getDefault());
                drawCentered(g2, name, c * cw, 0, cw, WEEKDAY_ROW_HEIGHT - 2);
            }
            g2.setColor(UITheme.BORDER);
            g2.drawLine(0, WEEKDAY_ROW_HEIGHT - 2, getWidth(), WEEKDAY_ROW_HEIGHT - 2);

            // Day cells
            LocalDate start = gridStart();
            LocalDate today = LocalDate.now();
            float diameter = Math.min(cw, ch) - 4;

            for (int i = 0; i < 42; i++) {
                int row = i / 7, col = i % 7;
                LocalDate d = start.plusDays(i);
                boolean inMonth = YearMonth.from(d).equals(shown);
                boolean isSelected = d.equals(selected);
                boolean isToday = d.equals(today);
                boolean isPast = d.isBefore(today);

                float cx = col * cw + cw / 2f;
                float cy = WEEKDAY_ROW_HEIGHT + row * ch + ch / 2f;
                Ellipse2D circle = new Ellipse2D.Float(cx - diameter / 2f, cy - diameter / 2f, diameter, diameter);

                if (isSelected) {
                    g2.setColor(UITheme.ACCENT);
                    g2.fill(circle);
                    if (isFocusOwner()) {
                        g2.setColor(UITheme.PRIMARY);
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.draw(new Ellipse2D.Float(cx - diameter / 2f - 2.5f, cy - diameter / 2f - 2.5f, diameter + 5, diameter + 5));
                    }
                } else if (i == hoverIndex) {
                    g2.setColor(HOVER_BG);
                    g2.fill(circle);
                }
                if (isToday && !isSelected) {
                    g2.setColor(UITheme.PRIMARY);
                    g2.setStroke(new BasicStroke(1.6f));
                    g2.draw(circle);
                }

                Color fg;
                if (isSelected) fg = Color.WHITE;
                else if (!inMonth) fg = OUTSIDE_MONTH;
                else if (isPast) fg = PAST_DAY;
                else fg = UITheme.TEXT_PRIMARY;
                g2.setColor(fg);
                g2.setFont(isSelected || isToday ? UITheme.bodyBold() : UITheme.body());
                boolean hasAppointments = appointmentCounts.getOrDefault(d, 0) > 0;
                float textShift = hasAppointments ? -3f : 0f;
                drawCentered(g2, String.valueOf(d.getDayOfMonth()), cx - cw / 2f, cy - ch / 2f + textShift, cw, ch);

                if (hasAppointments) {
                    g2.setColor(isSelected ? Color.WHITE : UITheme.ACCENT);
                    g2.fill(new Ellipse2D.Float(cx - 2f, cy + 8f, 4f, 4f));
                }
            }
            g2.dispose();
        }

        private void drawCentered(Graphics2D g2, String text, float x, float y, float w, float h) {
            FontMetrics fm = g2.getFontMetrics();
            float tx = x + (w - fm.stringWidth(text)) / 2f;
            float ty = y + (h - fm.getHeight()) / 2f + fm.getAscent();
            g2.drawString(text, tx, ty);
        }
    }
}
