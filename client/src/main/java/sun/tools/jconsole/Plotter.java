/*
 * @(#)Plotter.java    1.37 07/05/30
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.tools.jconsole;

import static sun.tools.jconsole.Formatter.formatBytes;
import static sun.tools.jconsole.Formatter.formatClockTime;
import static sun.tools.jconsole.Formatter.formatDate;
import static sun.tools.jconsole.Formatter.timeDF;
import static sun.tools.jconsole.Formatter.toExcelTime;
import static sun.tools.jconsole.Resources.getMnemonicInt;
import static sun.tools.jconsole.Resources.getText;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sun.tools.jconsole.JConsoleContext;
import com.sun.tools.jconsole.JConsoleContext.ConnectionState;

@SuppressWarnings("serial")
public class Plotter extends JComponent implements Accessible, ActionListener, PropertyChangeListener {

    public static enum Unit {
        NONE, BYTES, PERCENT
    }

    static final String[] rangeNames = { Resources.getText(" 1 min"), Resources.getText(" 5 min"), Resources.getText("10 min"), Resources.getText("30 min"), Resources.getText(" 1 hour"),
            Resources.getText(" 2 hours"), Resources.getText(" 3 hours"), Resources.getText(" 6 hours"), Resources.getText("12 hours"), Resources.getText(" 1 day"), Resources.getText(" 7 days"),
            Resources.getText(" 1 month"), Resources.getText(" 3 months"), Resources.getText(" 6 months"), Resources.getText(" 1 year"), Resources.getText("All") };

    static final int[] rangeValues = { 1, 5, 10, 30, 1 * 60, 2 * 60, 3 * 60, 6 * 60, 12 * 60, 1 * 24 * 60, 7 * 24 * 60, 1 * 31 * 24 * 60, 3 * 31 * 24 * 60, 6 * 31 * 24 * 60, 366 * 24 * 60, -1 };

    final static long SECOND = 1000;
    final static long MINUTE = 60 * SECOND;
    final static long HOUR = 60 * MINUTE;
    final static long DAY = 24 * HOUR;

    final static Color bgColor = new Color(250, 250, 250);
    final static Color defaultColor = Color.blue.darker();

    final static int ARRAY_SIZE_INCREMENT = 4000;

    private static Stroke dashedStroke;

    private TimeStamps times = new TimeStamps();
    private ArrayList<Sequence> seqs = new ArrayList<Sequence>();
    private JPopupMenu popupMenu;
    private JMenu timeRangeMenu;
    private JRadioButtonMenuItem[] menuRBs;
    private JMenuItem saveAsMI;
    private JFileChooser saveFC;

    private int viewRange = -1; // Minutes (value <= 0 means full range)
    private Unit unit;
    private int decimals;
    private double decimalsMultiplier;
    private Border border = null;
    private Rectangle r = new Rectangle(1, 1, 1, 1);
    private Font smallFont = null;

    // Initial margins, may be recalculated as needed
    private int topMargin = 10;
    private int bottomMargin = 45;
    private int leftMargin = 65;
    private int rightMargin = 70;

    public Plotter() {
        this(Unit.NONE, 0);
    }

    public Plotter(Unit unit) {
        this(unit, 0);
    }

    // Note: If decimals > 0 then values must be decimally shifted left
    // that many places, i.e. multiplied by Math.pow(10.0, decimals).
    public Plotter(Unit unit, int decimals) {
        setUnit(unit);
        setDecimals(decimals);

        enableEvents(AWTEvent.MOUSE_EVENT_MASK);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (getParent() instanceof PlotterPanel) {
                    getParent().requestFocusInWindow();
                }
            }
        });

    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
        this.decimalsMultiplier = Math.pow(10.0, decimals);
    }

    public void createSequence(String key, String name, Color color, boolean isPlotted) {
        Sequence seq = getSequence(key);
        if (seq == null) {
            seq = new Sequence(key);
        }
        seq.name = name;
        seq.color = (color != null) ? color : defaultColor;
        seq.isPlotted = isPlotted;

        this.seqs.add(seq);
    }

    public void setUseDashedTransitions(String key, boolean b) {
        Sequence seq = getSequence(key);
        if (seq != null) {
            seq.transitionStroke = b ? getDashedStroke() : null;
        }
    }

    public void setIsPlotted(String key, boolean isPlotted) {
        Sequence seq = getSequence(key);
        if (seq != null) {
            seq.isPlotted = isPlotted;
        }
    }

    // Note: If decimals > 0 then values must be decimally shifted left
    // that many places, i.e. multiplied by Math.pow(10.0, decimals).
    public synchronized void addValues(long time, long... values) {
        assert (values.length == this.seqs.size());
        this.times.add(time);
        for (int i = 0; i < values.length; i++) {
            this.seqs.get(i).add(values[i]);
        }
        repaint();
    }

    private Sequence getSequence(String key) {
        for (Sequence seq : this.seqs) {
            if (seq.key.equals(key)) {
                return seq;
            }
        }
        return null;
    }

    /**
     * @return the displayed time range in minutes, or -1 for all data
     */
    public int getViewRange() {
        return this.viewRange;
    }

    /**
     * @param minutes
     *            the displayed time range in minutes, or -1 to diaplay all data
     */
    public void setViewRange(int minutes) {
        if (minutes != this.viewRange) {
            int oldValue = this.viewRange;
            this.viewRange = minutes;
            /* Do not i18n this string */
            firePropertyChange("viewRange", oldValue, this.viewRange);
            if (this.popupMenu != null) {
                for (int i = 0; i < this.menuRBs.length; i++) {
                    if (rangeValues[i] == this.viewRange) {
                        this.menuRBs[i].setSelected(true);
                        break;
                    }
                }
            }
            repaint();
        }
    }

    public JPopupMenu getComponentPopupMenu() {
        if (this.popupMenu == null) {
            this.popupMenu = new JPopupMenu(Resources.getText("Chart:"));
            this.timeRangeMenu = new JMenu(Resources.getText("Plotter.timeRangeMenu"));
            this.timeRangeMenu.setMnemonic(getMnemonicInt("Plotter.timeRangeMenu"));
            this.popupMenu.add(this.timeRangeMenu);
            this.menuRBs = new JRadioButtonMenuItem[rangeNames.length];
            ButtonGroup rbGroup = new ButtonGroup();
            for (int i = 0; i < rangeNames.length; i++) {
                this.menuRBs[i] = new JRadioButtonMenuItem(rangeNames[i]);
                rbGroup.add(this.menuRBs[i]);
                this.menuRBs[i].addActionListener(this);
                if (this.viewRange == rangeValues[i]) {
                    this.menuRBs[i].setSelected(true);
                }
                this.timeRangeMenu.add(this.menuRBs[i]);
            }

            this.popupMenu.addSeparator();

            this.saveAsMI = new JMenuItem(getText("Plotter.saveAsMenuItem"));
            this.saveAsMI.setMnemonic(getMnemonicInt("Plotter.saveAsMenuItem"));
            this.saveAsMI.addActionListener(this);
            this.popupMenu.add(this.saveAsMI);
        }
        return this.popupMenu;
    }

    public void actionPerformed(ActionEvent ev) {
        JComponent src = (JComponent) ev.getSource();
        if (src == this.saveAsMI) {
            saveAs();
        } else {
            int index = this.timeRangeMenu.getPopupMenu().getComponentIndex(src);
            setViewRange(rangeValues[index]);
        }
    }

    private void saveAs() {
        if (this.saveFC == null) {
            this.saveFC = new SaveDataFileChooser();
        }
        int ret = this.saveFC.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            saveDataToFile(this.saveFC.getSelectedFile());
        }
    }

    private void saveDataToFile(File file) {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(file));

            // Print header line
            out.print("Time");
            for (Sequence seq : this.seqs) {
                out.print("," + seq.name);
            }
            out.println();

            // Print data lines
            if (this.seqs.size() > 0 && this.seqs.get(0).size > 0) {
                for (int i = 0; i < this.seqs.get(0).size; i++) {
                    double excelTime = toExcelTime(this.times.time(i));
                    out.print(String.format(Locale.ENGLISH, "%.6f", excelTime));
                    for (Sequence seq : this.seqs) {
                        out.print("," + getFormattedValue(seq.value(i), false));
                    }
                    out.println();
                }
            }

            out.close();
            JOptionPane.showMessageDialog(this, getText("FileChooser.savedFile", file.getAbsolutePath(), file.length()));
        } catch (IOException ex) {
            String msg = ex.getLocalizedMessage();
            String path = file.getAbsolutePath();
            if (msg.startsWith(path)) {
                msg = msg.substring(path.length()).trim();
            }
            JOptionPane.showMessageDialog(this, getText("FileChooser.saveFailed.message", path, msg), getText("FileChooser.saveFailed.title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Color oldColor = g.getColor();
        Font oldFont = g.getFont();
        Color fg = getForeground();
        Color bg = getBackground();
        boolean bgIsLight = (bg.getRed() > 200 && bg.getGreen() > 200 && bg.getBlue() > 200);

        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (this.smallFont == null) {
            this.smallFont = oldFont.deriveFont(9.0F);
        }

        this.r.x = this.leftMargin - 5;
        this.r.y = this.topMargin - 8;
        this.r.width = getWidth() - this.leftMargin - this.rightMargin;
        this.r.height = getHeight() - this.topMargin - this.bottomMargin + 16;

        if (this.border == null) {
            // By setting colors here, we avoid recalculating them
            // over and over.
            this.border = new BevelBorder(BevelBorder.LOWERED, getBackground().brighter().brighter(), getBackground().brighter(), getBackground().darker().darker(), getBackground().darker());
        }

        this.border.paintBorder(this, g, this.r.x, this.r.y, this.r.width, this.r.height);

        // Fill background color
        g.setColor(bgColor);
        g.fillRect(this.r.x + 2, this.r.y + 2, this.r.width - 4, this.r.height - 4);
        g.setColor(oldColor);

        long tMin = Long.MAX_VALUE;
        long tMax = Long.MIN_VALUE;
        long vMin = Long.MAX_VALUE;
        long vMax = 1;

        int w = getWidth() - this.rightMargin - this.leftMargin - 10;
        int h = getHeight() - this.topMargin - this.bottomMargin;

        if (this.times.size > 1) {
            tMin = Math.min(tMin, this.times.time(0));
            tMax = Math.max(tMax, this.times.time(this.times.size - 1));
        }
        long viewRangeMS;
        if (this.viewRange > 0) {
            viewRangeMS = this.viewRange * MINUTE;
        } else {
            // Display full time range, but no less than a minute
            viewRangeMS = Math.max(tMax - tMin, 1 * MINUTE);
        }

        // Calculate min/max values
        for (Sequence seq : this.seqs) {
            if (seq.size > 0) {
                for (int i = 0; i < seq.size; i++) {
                    if (seq.size == 1 || this.times.time(i) >= tMax - viewRangeMS) {
                        long val = seq.value(i);
                        if (val > Long.MIN_VALUE) {
                            vMax = Math.max(vMax, val);
                            vMin = Math.min(vMin, val);
                        }
                    }
                }
            } else {
                vMin = 0L;
            }
            if (this.unit == Unit.BYTES || !seq.isPlotted) {
                // We'll scale only to the first (main) value set.
                break;
            }
        }

        // Normalize scale
        vMax = normalizeMax(vMax);
        if (vMin > 0) {
            if (vMax / vMin > 4) {
                vMin = 0;
            } else {
                vMin = normalizeMin(vMin);
            }
        }

        g.setColor(fg);

        // Axes
        // Draw vertical axis
        int x = this.leftMargin - 18;
        int y = this.topMargin;
        FontMetrics fm = g.getFontMetrics();

        g.drawLine(x, y, x, y + h);

        int n = 5;
        if (("" + vMax).startsWith("2")) {
            n = 4;
        } else if (("" + vMax).startsWith("3")) {
            n = 6;
        } else if (("" + vMax).startsWith("4")) {
            n = 4;
        } else if (("" + vMax).startsWith("6")) {
            n = 6;
        } else if (("" + vMax).startsWith("7")) {
            n = 7;
        } else if (("" + vMax).startsWith("8")) {
            n = 8;
        } else if (("" + vMax).startsWith("9")) {
            n = 3;
        }

        // Ticks
        ArrayList<Long> tickValues = new ArrayList<Long>();
        tickValues.add(vMin);
        for (int i = 0; i < n; i++) {
            long v = i * vMax / n;
            if (v > vMin) {
                tickValues.add(v);
            }
        }
        tickValues.add(vMax);
        n = tickValues.size();

        String[] tickStrings = new String[n];
        for (int i = 0; i < n; i++) {
            long v = tickValues.get(i);
            tickStrings[i] = getSizeString(v, vMax);
        }

        // Trim trailing decimal zeroes.
        if (this.decimals > 0) {
            boolean trimLast = true;
            boolean removedDecimalPoint = false;
            do {
                for (String str : tickStrings) {
                    if (!(str.endsWith("0") || str.endsWith("."))) {
                        trimLast = false;
                        break;
                    }
                }
                if (trimLast) {
                    if (tickStrings[0].endsWith(".")) {
                        removedDecimalPoint = true;
                    }
                    for (int i = 0; i < n; i++) {
                        String str = tickStrings[i];
                        tickStrings[i] = str.substring(0, str.length() - 1);
                    }
                }
            } while (trimLast && !removedDecimalPoint);
        }

        // Draw ticks
        int lastY = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            long v = tickValues.get(i);
            y = this.topMargin + h - (int) (h * (v - vMin) / (vMax - vMin));
            g.drawLine(x - 2, y, x + 2, y);
            String s = tickStrings[i];
            if (this.unit == Unit.PERCENT) {
                s += "%";
            }
            int sx = x - 6 - fm.stringWidth(s);
            if (y < lastY - 13) {
                if (checkLeftMargin(sx)) {
                    // Wait for next repaint
                    return;
                }
                g.drawString(s, sx, y + 4);
            }
            // Draw horizontal grid line
            g.setColor(Color.lightGray);
            g.drawLine(this.r.x + 4, y, this.r.x + this.r.width - 4, y);
            g.setColor(fg);
            lastY = y;
        }

        // Draw horizontal axis
        x = this.leftMargin;
        y = this.topMargin + h + 15;
        g.drawLine(x, y, x + w, y);

        long t1 = tMax;
        if (t1 <= 0L) {
            // No data yet, so draw current time
            t1 = System.currentTimeMillis();
        }
        long tz = timeDF.getTimeZone().getOffset(t1);
        long tickInterval = calculateTickInterval(w, 40, viewRangeMS);
        if (tickInterval > 3 * HOUR) {
            tickInterval = calculateTickInterval(w, 80, viewRangeMS);
        }
        long t0 = tickInterval - (t1 - viewRangeMS + tz) % tickInterval;
        while (t0 < viewRangeMS) {
            x = this.leftMargin + (int) (w * t0 / viewRangeMS);
            g.drawLine(x, y - 2, x, y + 2);

            long t = t1 - viewRangeMS + t0;
            String str = formatClockTime(t);
            g.drawString(str, x, y + 16);
            // if (tickInterval > (1 * HOUR) && t % (1 * DAY) == 0) {
            if ((t + tz) % (1 * DAY) == 0) {
                str = formatDate(t);
                g.drawString(str, x, y + 27);
            }
            // Draw vertical grid line
            g.setColor(Color.lightGray);
            g.drawLine(x, this.topMargin, x, this.topMargin + h);
            g.setColor(fg);
            t0 += tickInterval;
        }

        // Plot values
        int start = 0;
        int nValues = 0;
        int nLists = this.seqs.size();
        if (nLists > 0) {
            nValues = this.seqs.get(0).size;
        }
        if (nValues == 0) {
            g.setColor(oldColor);
            return;
        } else {
            Sequence seq = this.seqs.get(0);
            // Find starting point
            for (int p = 0; p < seq.size; p++) {
                if (this.times.time(p) >= tMax - viewRangeMS) {
                    start = p;
                    break;
                }
            }
        }

        // Optimization: collapse plot of more than four values per pixel
        int pointsPerPixel = (nValues - start) / w;
        if (pointsPerPixel < 4) {
            pointsPerPixel = 1;
        }

        // Draw graphs
        // Loop backwards over sequences because the first needs to be painted
        // on top
        for (int i = nLists - 1; i >= 0; i--) {
            int x0 = this.leftMargin;
            int y0 = this.topMargin + h + 1;

            Sequence seq = this.seqs.get(i);
            if (seq.isPlotted && seq.size > 0) {
                // Paint twice, with white and with color
                for (int pass = 0; pass < 2; pass++) {
                    g.setColor((pass == 0) ? Color.white : seq.color);
                    int x1 = -1;
                    long v1 = -1;
                    for (int p = start; p < nValues; p += pointsPerPixel) {
                        // Make sure we get the last value
                        if (pointsPerPixel > 1 && p >= nValues - pointsPerPixel) {
                            p = nValues - 1;
                        }
                        int x2 = (int) (w * (this.times.time(p) - (t1 - viewRangeMS)) / viewRangeMS);
                        long v2 = seq.value(p);
                        if (v2 >= vMin && v2 <= vMax) {
                            int y2 = (int) (h * (v2 - vMin) / (vMax - vMin));
                            if (x1 >= 0 && v1 >= vMin && v1 <= vMax) {
                                int y1 = (int) (h * (v1 - vMin) / (vMax - vMin));

                                if (y1 == y2) {
                                    // fillrect is much faster
                                    g.fillRect(x0 + x1, y0 - y1 - pass, x2 - x1, 1);
                                } else {
                                    Graphics2D g2d = (Graphics2D) g;
                                    Stroke oldStroke = null;
                                    if (seq.transitionStroke != null) {
                                        oldStroke = g2d.getStroke();
                                        g2d.setStroke(seq.transitionStroke);
                                    }
                                    g.drawLine(x0 + x1, y0 - y1 - pass, x0 + x2, y0 - y2 - pass);
                                    if (oldStroke != null) {
                                        g2d.setStroke(oldStroke);
                                    }
                                }
                            }
                        }
                        x1 = x2;
                        v1 = v2;
                    }
                }

                // Current value
                long v = seq.value(seq.size - 1);
                if (v >= vMin && v <= vMax) {
                    if (bgIsLight) {
                        g.setColor(seq.color);
                    } else {
                        g.setColor(fg);
                    }
                    x = this.r.x + this.r.width + 2;
                    y = this.topMargin + h - (int) (h * (v - vMin) / (vMax - vMin));
                    // a small triangle/arrow
                    g.fillPolygon(new int[] { x + 2, x + 6, x + 6 }, new int[] { y, y + 3, y - 3 }, 3);
                }
                g.setColor(fg);
            }
        }

        int[] valueStringSlots = new int[nLists];
        for (int i = 0; i < nLists; i++)
            valueStringSlots[i] = -1;
        for (int i = 0; i < nLists; i++) {
            Sequence seq = this.seqs.get(i);
            if (seq.isPlotted && seq.size > 0) {
                // Draw current value

                long v = seq.value(seq.size - 1);
                if (v >= vMin && v <= vMax) {
                    x = this.r.x + this.r.width + 2;
                    y = this.topMargin + h - (int) (h * (v - vMin) / (vMax - vMin));
                    int y2 = getValueStringSlot(valueStringSlots, y, 2 * 10, i);
                    g.setFont(this.smallFont);
                    if (bgIsLight) {
                        g.setColor(seq.color);
                    } else {
                        g.setColor(fg);
                    }
                    String curValue = getFormattedValue(v, true);
                    if (this.unit == Unit.PERCENT) {
                        curValue += "%";
                    }
                    int valWidth = fm.stringWidth(curValue);
                    String legend = seq.name;
                    int legendWidth = fm.stringWidth(legend);
                    if (checkRightMargin(valWidth) || checkRightMargin(legendWidth)) {
                        // Wait for next repaint
                        return;
                    }
                    g.drawString(legend, x + 17, Math.min(this.topMargin + h, y2 + 3 - 10));
                    g.drawString(curValue, x + 17, Math.min(this.topMargin + h + 10, y2 + 3));

                    // Maybe draw a short line to value
                    if (y2 > y + 3) {
                        g.drawLine(x + 9, y + 2, x + 14, y2);
                    } else if (y2 < y - 3) {
                        g.drawLine(x + 9, y - 2, x + 14, y2);
                    }
                }
                g.setFont(oldFont);
                g.setColor(fg);

            }
        }
        g.setColor(oldColor);
    }

    private boolean checkLeftMargin(int x) {
        // Make sure leftMargin has at least 2 pixels over
        if (x < 2) {
            this.leftMargin += (2 - x);
            // Repaint from top (above any cell renderers)
            SwingUtilities.getWindowAncestor(this).repaint();
            return true;
        }
        return false;
    }

    private boolean checkRightMargin(int w) {
        // Make sure rightMargin has at least 2 pixels over
        if (w + 2 > this.rightMargin) {
            this.rightMargin = (w + 2);
            // Repaint from top (above any cell renderers)
            SwingUtilities.getWindowAncestor(this).repaint();
            return true;
        }
        return false;
    }

    private int getValueStringSlot(int[] slots, int y, int h, int i) {
        for (int slot : slots) {
            if (slot >= y && slot < y + h) {
                // collide below us
                if (slot > h) {
                    return getValueStringSlot(slots, slot - h, h, i);
                } else {
                    return getValueStringSlot(slots, slot + h, h, i);
                }
            } else if (y >= h && slot > y - h && slot < y) {
                // collide above us
                return getValueStringSlot(slots, slot + h, h, i);
            }
        }
        slots[i] = y;
        return y;
    }

    private long calculateTickInterval(int w, int hGap, long viewRangeMS) {
        long tickInterval = viewRangeMS * hGap / w;
        if (tickInterval < 1 * MINUTE) {
            tickInterval = 1 * MINUTE;
        } else if (tickInterval < 5 * MINUTE) {
            tickInterval = 5 * MINUTE;
        } else if (tickInterval < 10 * MINUTE) {
            tickInterval = 10 * MINUTE;
        } else if (tickInterval < 30 * MINUTE) {
            tickInterval = 30 * MINUTE;
        } else if (tickInterval < 1 * HOUR) {
            tickInterval = 1 * HOUR;
        } else if (tickInterval < 3 * HOUR) {
            tickInterval = 3 * HOUR;
        } else if (tickInterval < 6 * HOUR) {
            tickInterval = 6 * HOUR;
        } else if (tickInterval < 12 * HOUR) {
            tickInterval = 12 * HOUR;
        } else if (tickInterval < 1 * DAY) {
            tickInterval = 1 * DAY;
        } else {
            tickInterval = normalizeMax(tickInterval / DAY) * DAY;
        }
        return tickInterval;
    }

    private long normalizeMin(long l) {
        int exp = (int) Math.log10(l);
        long multiple = (long) Math.pow(10.0, exp);
        int i = (int) (l / multiple);
        if (multiple * i > 10) {
            multiple = multiple * i / 10;
            i = (int) (l / multiple);
        }
        long min = i * multiple;
        return min;
    }

    private long normalizeMax(long l) {
        int exp = (int) Math.log10(l);
        long multiple = (long) Math.pow(10.0, exp);
        int i = (int) (l / multiple);
        if (multiple * i > 10) {
            multiple = multiple * i / 10;
            i = (int) (l / multiple);
        }
        long max = (i + 1) * multiple;
        return max;
    }

    private String getFormattedValue(long v, boolean groupDigits) {
        String str;
        String fmt = "%";
        if (groupDigits) {
            fmt += ",";
        }
        if (this.decimals > 0) {
            fmt += "." + this.decimals + "f";
            str = String.format(fmt, v / this.decimalsMultiplier);
        } else {
            fmt += "d";
            str = String.format(fmt, v);
        }
        return str;
    }

    private String getSizeString(long v, long vMax) {
        String s;

        if (this.unit == Unit.BYTES && this.decimals == 0) {
            s = formatBytes(v, vMax);
        } else {
            s = getFormattedValue(v, true);
        }
        return s;
    }

    private static synchronized Stroke getDashedStroke() {
        if (dashedStroke == null) {
            dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 2.0f, 3.0f }, 0.0f);
        }
        return dashedStroke;
    }

    private static Object extendArray(Object a1) {
        int n = Array.getLength(a1);
        Object a2 = Array.newInstance(a1.getClass().getComponentType(), n + ARRAY_SIZE_INCREMENT);
        System.arraycopy(a1, 0, a2, 0, n);
        return a2;
    }

    private static class TimeStamps {
        // Time stamps (long) are split into offsets (long) and a
        // series of times from the offsets (int). A new offset is
        // stored when the the time value doesn't fit in an int
        // (approx every 24 days). An array of indices is used to
        // define the starting point for each offset in the times
        // array.
        long[] offsets = new long[0];
        int[] indices = new int[0];
        int[] rtimes = new int[ARRAY_SIZE_INCREMENT];

        // Number of stored timestamps
        int size = 0;

        /**
         * Returns the time stamp for index i
         */
        public long time(int i) {
            long offset = 0;
            for (int j = this.indices.length - 1; j >= 0; j--) {
                if (i >= this.indices[j]) {
                    offset = this.offsets[j];
                    break;
                }
            }
            return offset + this.rtimes[i];
        }

        public void add(long time) {
            // May need to store a new time offset
            int n = this.offsets.length;
            if (n == 0 || time - this.offsets[n - 1] > Integer.MAX_VALUE) {
                // Grow offset and indices arrays and store new offset
                this.offsets = Arrays.copyOf(this.offsets, n + 1);
                this.offsets[n] = time;
                this.indices = Arrays.copyOf(this.indices, n + 1);
                this.indices[n] = this.size;
            }

            // May need to extend the array size
            if (this.rtimes.length == this.size) {
                this.rtimes = (int[]) extendArray(this.rtimes);
            }

            // Store the time
            this.rtimes[this.size] = (int) (time - this.offsets[this.offsets.length - 1]);
            this.size++;
        }
    }

    private static class Sequence {
        String key;
        String name;
        Color color;
        boolean isPlotted;
        Stroke transitionStroke = null;

        // Values are stored in an int[] if all values will fit,
        // otherwise in a long[]. An int can represent up to 2 GB.
        // Use a random start size, so all arrays won't need to
        // be grown during the same update interval
        Object values = new byte[ARRAY_SIZE_INCREMENT + (int) (Math.random() * 100)];

        // Number of stored values
        int size = 0;

        public Sequence(String key) {
            this.key = key;
        }

        /**
         * Returns the value at index i
         */
        public long value(int i) {
            return Array.getLong(this.values, i);
        }

        public void add(long value) {
            // May need to switch to a larger array type
            if ((this.values instanceof byte[] || this.values instanceof short[] || this.values instanceof int[]) && value > Integer.MAX_VALUE) {
                long[] la = new long[Array.getLength(this.values)];
                for (int i = 0; i < this.size; i++) {
                    la[i] = Array.getLong(this.values, i);
                }
                this.values = la;
            } else if ((this.values instanceof byte[] || this.values instanceof short[]) && value > Short.MAX_VALUE) {
                int[] ia = new int[Array.getLength(this.values)];
                for (int i = 0; i < this.size; i++) {
                    ia[i] = Array.getInt(this.values, i);
                }
                this.values = ia;
            } else if (this.values instanceof byte[] && value > Byte.MAX_VALUE) {
                short[] sa = new short[Array.getLength(this.values)];
                for (int i = 0; i < this.size; i++) {
                    sa[i] = Array.getShort(this.values, i);
                }
                this.values = sa;
            }

            // May need to extend the array size
            if (Array.getLength(this.values) == this.size) {
                this.values = extendArray(this.values);
            }

            // Store the value
            if (this.values instanceof long[]) {
                ((long[]) this.values)[this.size] = value;
            } else if (this.values instanceof int[]) {
                ((int[]) this.values)[this.size] = (int) value;
            } else if (this.values instanceof short[]) {
                ((short[]) this.values)[this.size] = (short) value;
            } else {
                ((byte[]) this.values)[this.size] = (byte) value;
            }
            this.size++;
        }
    }

    // Can be overridden by subclasses
    long getValue() {
        return 0;
    }

    long getLastTimeStamp() {
        return this.times.time(this.times.size - 1);
    }

    long getLastValue(String key) {
        Sequence seq = getSequence(key);
        return (seq != null && seq.size > 0) ? seq.value(seq.size - 1) : 0L;
    }

    // Called on EDT
    public void propertyChange(PropertyChangeEvent ev) {
        String prop = ev.getPropertyName();

        if (prop == JConsoleContext.CONNECTION_STATE_PROPERTY) {
            ConnectionState newState = (ConnectionState) ev.getNewValue();

            switch (newState) {
            case DISCONNECTED:
                synchronized (this) {
                    long time = System.currentTimeMillis();
                    this.times.add(time);
                    for (Sequence seq : this.seqs) {
                        seq.add(Long.MIN_VALUE);
                    }
                }
                break;
            }
        }
    }

    private static class SaveDataFileChooser extends JFileChooser {
        SaveDataFileChooser() {
            setFileFilter(new FileNameExtensionFilter("CSV file", "csv"));
        }

        public void approveSelection() {
            File file = getSelectedFile();
            if (file != null) {
                FileFilter filter = getFileFilter();
                if (filter != null && filter instanceof FileNameExtensionFilter) {
                    String[] extensions = ((FileNameExtensionFilter) filter).getExtensions();

                    boolean goodExt = false;
                    for (String ext : extensions) {
                        if (file.getName().toLowerCase().endsWith("." + ext.toLowerCase())) {
                            goodExt = true;
                            break;
                        }
                    }
                    if (!goodExt) {
                        file = new File(file.getParent(), file.getName() + "." + extensions[0]);
                    }
                }

                if (file.exists()) {
                    String okStr = getText("FileChooser.fileExists.okOption");
                    String cancelStr = getText("FileChooser.fileExists.cancelOption");
                    int ret = JOptionPane.showOptionDialog(this, getText("FileChooser.fileExists.message", file.getName()), getText("FileChooser.fileExists.title"), JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE, null, new Object[] { okStr, cancelStr }, okStr);
                    if (ret != JOptionPane.OK_OPTION) {
                        return;
                    }
                }
                setSelectedFile(file);
            }
            super.approveSelection();
        }
    }

    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessiblePlotter();
        }
        return this.accessibleContext;
    }

    protected class AccessiblePlotter extends AccessibleJComponent {
        protected AccessiblePlotter() {
            setAccessibleName(getText("Plotter.accessibleName"));
        }

        public String getAccessibleName() {
            String name = super.getAccessibleName();

            if (Plotter.this.seqs.size() > 0 && Plotter.this.seqs.get(0).size > 0) {
                String keyValueList = "";
                for (Sequence seq : Plotter.this.seqs) {
                    if (seq.isPlotted) {
                        String value = "null";
                        if (seq.size > 0) {
                            if (Plotter.this.unit == Unit.BYTES) {
                                value = getText("Size Bytes", seq.value(seq.size - 1));
                            } else {
                                value = getFormattedValue(seq.value(seq.size - 1), false) + ((Plotter.this.unit == Unit.PERCENT) ? "%" : "");
                            }
                        }
                        // Assume format string ends with newline
                        keyValueList += getText("Plotter.accessibleName.keyAndValue", seq.key, value);
                    }
                }
                name += "\n" + keyValueList + ".";
            } else {
                name += "\n" + getText("Plotter.accessibleName.noData");
            }
            return name;
        }

        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.CANVAS;
        }
    }
}
