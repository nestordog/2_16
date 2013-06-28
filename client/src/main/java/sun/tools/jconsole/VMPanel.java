// Algotrader
// line 92 - 97: change tab order
// line 577 - 597: add timeout
/*
 * Copyright (c) 2004, 2008, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.tools.jconsole;

import static com.sun.tools.jconsole.JConsoleContext.CONNECTION_STATE_PROPERTY;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.plaf.TabbedPaneUI;

import ch.algotrader.client.WarningProducer;

import com.sun.tools.jconsole.JConsoleContext;
import com.sun.tools.jconsole.JConsoleContext.ConnectionState;
import com.sun.tools.jconsole.JConsolePlugin;

@SuppressWarnings("serial")
public class VMPanel extends JTabbedPane implements PropertyChangeListener {

    private ProxyClient proxyClient;
    private Timer timer;
    private int updateInterval;
    private String hostName;
    private int port;
    private int vmid;
    private String userName;
    private String password;
    private String url;
    private VMInternalFrame vmIF = null;
    private static final String windowsLaF =
            "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
    private static ArrayList<TabInfo> tabInfos = new ArrayList<TabInfo>();
    private boolean wasConnected = false;

    // The everConnected flag keeps track of whether the window can be
    // closed if the user clicks Cancel after a failed connection attempt.
    //
    private boolean everConnected = false;

    // The initialUpdate flag is used to enable/disable tabs each time
    // a connect or reconnect takes place. This flag avoids having to
    // enable/disable tabs on each update call.
    //
    private boolean initialUpdate = true;

    // Each VMPanel has its own instance of the JConsolePlugin
    // A map of JConsolePlugin to the previous SwingWorker
    private Map<JConsolePlugin, SwingWorker<?, ?>> plugins = null;
    private boolean pluginTabsAdded = false;

    // Update these only on the EDT
    private JOptionPane optionPane;
    private JProgressBar progressBar;
    private long time0;

    static {
        if (System.getProperty("expertMode") != null) {
            tabInfos.add(new TabInfo(OverviewTab.class, OverviewTab.getTabName(), true));
            tabInfos.add(new TabInfo(MemoryTab.class, MemoryTab.getTabName(), true));
            tabInfos.add(new TabInfo(SummaryTab.class, SummaryTab.getTabName(), true));
            tabInfos.add(new TabInfo(ThreadTab.class, ThreadTab.getTabName(), true));
            tabInfos.add(new TabInfo(ClassTab.class, ClassTab.getTabName(), true));
        }
        tabInfos.add(new TabInfo(MBeansTab.class, MBeansTab.getTabName(), true));
    }

    public static TabInfo[] getTabInfos() {
        return tabInfos.toArray(new TabInfo[tabInfos.size()]);
    }

    VMPanel(ProxyClient proxyClient, int updateInterval) {
        this.proxyClient = proxyClient;
        this.updateInterval = updateInterval;
        this.hostName = proxyClient.getHostName();
        this.port = proxyClient.getPort();
        this.vmid = proxyClient.getVmid();
        this.userName = proxyClient.getUserName();
        this.password = proxyClient.getPassword();
        this.url = proxyClient.getUrl();

        for (TabInfo tabInfo : tabInfos) {
            if (tabInfo.tabVisible) {
                addTab(tabInfo);
            }
        }

        this.plugins = new LinkedHashMap<JConsolePlugin, SwingWorker<?, ?>>();
        for (JConsolePlugin p : JConsole.getPlugins()) {
            p.setContext(proxyClient);
            this.plugins.put(p, null);
        }

        Utilities.updateTransparency(this);

        ToolTipManager.sharedInstance().registerComponent(this);

        // Start listening to connection state events
        //
        proxyClient.addPropertyChangeListener(this);

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (VMPanel.this.connectedIconBounds != null && (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0 && VMPanel.this.connectedIconBounds.contains(e.getPoint())) {

                    if (isConnected()) {
                        disconnect();
                        VMPanel.this.wasConnected = false;
                    } else {
                        connect();
                    }
                    repaint();
                }
            }
        });

    }
    private static Icon connectedIcon16 =
            new ImageIcon(VMPanel.class.getResource("resources/connected16.png"));
    private static Icon connectedIcon24 =
            new ImageIcon(VMPanel.class.getResource("resources/connected24.png"));
    private static Icon disconnectedIcon16 =
            new ImageIcon(VMPanel.class.getResource("resources/disconnected16.png"));
    private static Icon disconnectedIcon24 =
            new ImageIcon(VMPanel.class.getResource("resources/disconnected24.png"));
    private Rectangle connectedIconBounds;

    // Override to increase right inset for tab area,
    // in order to reserve space for the connect toggle.
    @Override
    public void setUI(TabbedPaneUI ui) {
        Insets insets = (Insets) UIManager.getLookAndFeelDefaults().get("TabbedPane.tabAreaInsets");
        insets = (Insets) insets.clone();
        insets.right += connectedIcon24.getIconWidth() + 8;
        UIManager.put("TabbedPane.tabAreaInsets", insets);
        super.setUI(ui);
    }

    // Override to paint the connect toggle
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Icon icon;
        Component c0 = getComponent(0);
        if (c0 != null && c0.getY() > 24) {
            icon = isConnected() ? connectedIcon24 : disconnectedIcon24;
        } else {
            icon = isConnected() ? connectedIcon16 : disconnectedIcon16;
        }
        Insets insets = getInsets();
        int x = getWidth() - insets.right - icon.getIconWidth() - 4;
        int y = insets.top;
        if (c0 != null) {
            y = (c0.getY() - icon.getIconHeight()) / 2;
        }
        icon.paintIcon(this, g, x, y);
        this.connectedIconBounds = new Rectangle(x, y, icon.getIconWidth(), icon.getIconHeight());
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (this.connectedIconBounds.contains(event.getPoint())) {
            if (isConnected()) {
                return getText("Connected. Click to disconnect.");
            } else {
                return getText("Disconnected. Click to connect.");
            }
        } else {
            return super.getToolTipText(event);
        }
    }

    private synchronized void addTab(TabInfo tabInfo) {
        Tab tab = instantiate(tabInfo);
        if (tab != null) {
            addTab(tabInfo.name, tab);
        } else {
            tabInfo.tabVisible = false;
        }
    }

    private synchronized void insertTab(TabInfo tabInfo, int index) {
        Tab tab = instantiate(tabInfo);
        if (tab != null) {
            insertTab(tabInfo.name, null, tab, null, index);
        } else {
            tabInfo.tabVisible = false;
        }
    }

    @Override
    public synchronized void removeTabAt(int index) {
        super.removeTabAt(index);
    }

    private Tab instantiate(TabInfo tabInfo) {
        try {
            Constructor con = tabInfo.tabClass.getConstructor(VMPanel.class);
            return (Tab) con.newInstance(this);
        } catch (Exception ex) {
            System.err.println(ex);
            return null;
        }
    }

    boolean isConnected() {
        return this.proxyClient.isConnected();
    }

    public int getUpdateInterval() {
        return this.updateInterval;
    }

    /**
     * WARNING NEVER CALL THIS METHOD TO MAKE JMX REQUEST
     * IF  assertThread == false.
     * DISPATCHER THREAD IS NOT ASSERTED.
     * IT IS USED TO MAKE SOME LOCAL MANIPULATIONS.
     */
    ProxyClient getProxyClient(boolean assertThread) {
        if (assertThread) {
            return getProxyClient();
        } else {
            return this.proxyClient;
        }
    }

    public ProxyClient getProxyClient() {
        String threadClass = Thread.currentThread().getClass().getName();
        if (threadClass.equals("java.awt.EventDispatchThread")) {
            String msg = "Calling VMPanel.getProxyClient() from the Event Dispatch Thread!";
            new RuntimeException(msg).printStackTrace();
            System.exit(1);
        }
        return this.proxyClient;
    }

    public void cleanUp() {
        //proxyClient.disconnect();
        for (Tab tab : getTabs()) {
            tab.dispose();
        }
        for (JConsolePlugin p : this.plugins.keySet()) {
            p.dispose();
        }
        // Cancel pending update tasks
        //
        if (this.timer != null) {
            this.timer.cancel();
        }
        // Stop listening to connection state events
        //
        this.proxyClient.removePropertyChangeListener(this);
    }

    // Call on EDT
    public void connect() {
        if (isConnected()) {
            // create plugin tabs if not done
            createPluginTabs();
            // Notify tabs
            fireConnectedChange(true);
            // Enable/disable tabs on initial update
            this.initialUpdate = true;
            // Start/Restart update timer on connect/reconnect
            startUpdateTimer();
        } else {
            new Thread("VMPanel.connect") {

                @Override
                public void run() {
                    VMPanel.this.proxyClient.connect();
                }
            }.start();
        }
    }

    // Call on EDT
    public void disconnect() {
        this.proxyClient.disconnect();
        updateFrameTitle();
    }

    // Called on EDT
    @Override
    public void propertyChange(PropertyChangeEvent ev) {
        String prop = ev.getPropertyName();

        if (prop == CONNECTION_STATE_PROPERTY) {
            ConnectionState oldState = (ConnectionState) ev.getOldValue();
            ConnectionState newState = (ConnectionState) ev.getNewValue();
            switch (newState) {
                case CONNECTING:
                    onConnecting();
                    break;

                case CONNECTED:
                    if (this.progressBar != null) {
                        this.progressBar.setIndeterminate(false);
                        this.progressBar.setValue(100);
                    }
                    closeOptionPane();
                    updateFrameTitle();
                    // create tabs if not done
                    createPluginTabs();
                    repaint();
                    // Notify tabs
                    fireConnectedChange(true);
                    // Enable/disable tabs on initial update
                    this.initialUpdate = true;
                    // Start/Restart update timer on connect/reconnect
                    startUpdateTimer();
                    break;

                case DISCONNECTED:
                    if (this.progressBar != null) {
                        this.progressBar.setIndeterminate(false);
                        this.progressBar.setValue(0);
                        closeOptionPane();
                    }
                    vmPanelDied();
                    if (oldState == ConnectionState.CONNECTED) {
                        // Notify tabs
                        fireConnectedChange(false);
                    }
                    break;
            }
        }
    }

    // Called on EDT
    private void onConnecting() {
        this.time0 = System.currentTimeMillis();

        final JConsole jc = (JConsole) SwingUtilities.getWindowAncestor(this);

        String connectionName = getConnectionName();
        this.progressBar = new JProgressBar();
        this.progressBar.setIndeterminate(true);
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        progressPanel.add(this.progressBar);

        Object[] message = {
            "<html><h3>" + getText("connectingTo1", connectionName) + "</h3></html>",
            progressPanel,
            "<html><b>" + getText("connectingTo2", connectionName) + "</b></html>"
        };

        this.optionPane =
                SheetDialog.showOptionDialog(this,
                message,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null,
                new String[]{getText("Cancel")},
                0);


    }

    // Called on EDT
    private void closeOptionPane() {
        if (this.optionPane != null) {
            new Thread("VMPanel.sleeper") {
                @Override
                public void run() {
                    long elapsed = System.currentTimeMillis() - VMPanel.this.time0;
                    if (elapsed < 2000) {
                        try {
                            sleep(2000 - elapsed);
                        } catch (InterruptedException ex) {
                        // Ignore
                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            VMPanel.this.optionPane.setVisible(false);
                            VMPanel.this.progressBar = null;
                        }
                    });
                }
            }.start();
        }
    }

    void updateFrameTitle() {
        VMInternalFrame vmIF = getFrame();
        if (vmIF != null) {
            String displayName = getDisplayName();
            if (!this.proxyClient.isConnected()) {
                displayName = getText("ConnectionName (disconnected)", displayName);
            }
            vmIF.setTitle(displayName);
        }
    }

    private VMInternalFrame getFrame() {
        if (this.vmIF == null) {
            this.vmIF = (VMInternalFrame) SwingUtilities.getAncestorOfClass(VMInternalFrame.class,
                    this);
        }
        return this.vmIF;
    }

    // TODO: this method is not needed when all JConsole tabs
    // are migrated to use the new JConsolePlugin API.
    //
    // A thread safe clone of all JConsole tabs
    synchronized List<Tab> getTabs() {
        ArrayList<Tab> list = new ArrayList<Tab>();
        int n = getTabCount();
        for (int i = 0; i < n; i++) {
            Component c = getComponentAt(i);
            if (c instanceof Tab) {
                list.add((Tab) c);
            }
        }
        return list;
    }

    private void startUpdateTimer() {
        if (this.timer != null) {
            this.timer.cancel();
        }
        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                update();
            }
        };
        String timerName = "Timer-" + getConnectionName();
        this.timer = new Timer(timerName, true);
        this.timer.schedule(timerTask, 0, this.updateInterval);
    }

    // Call on EDT
    private void vmPanelDied() {
        disconnect();

        final JConsole jc = (JConsole) SwingUtilities.getWindowAncestor(this);

        JOptionPane optionPane;

        final String connectStr = getText("Connect");
        final String reconnectStr = getText("Reconnect");
        final String cancelStr = getText("Cancel");

        String msgTitle, msgExplanation, buttonStr;

        if (this.wasConnected) {
            this.wasConnected = false;
            msgTitle = getText("connectionLost1");
            msgExplanation = getText("connectionLost2", getConnectionName());
            buttonStr = reconnectStr;
        } else {
            msgTitle = getText("connectionFailed1");
            msgExplanation = getText("connectionFailed2", getConnectionName());
            buttonStr = connectStr;
        }

        optionPane =
                SheetDialog.showOptionDialog(this,
                "<html><h3>" + msgTitle + "</h3>" +
                "<b>" + msgExplanation + "</b>",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null,
                new String[]{buttonStr, cancelStr},
                0);

        optionPane.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) {
                    Object value = event.getNewValue();

                    if (value == reconnectStr || value == connectStr) {
                        connect();
                    } else if (!VMPanel.this.everConnected) {
                        try {
                            getFrame().setClosed(true);
                        } catch (PropertyVetoException ex) {
                        // Should not happen, but can be ignored.
                        }
                    }
                }
            }
        });
    }

    // Note: This method is called on a TimerTask thread. Any GUI manipulation
    // must be performed with invokeLater() or invokeAndWait().
    private Object lockObject = new Object();

    private void update() {
        synchronized (this.lockObject) {
            if (!isConnected()) {
                if (this.wasConnected) {
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            vmPanelDied();
                        }
                    });
                }
                this.wasConnected = false;
                return;
            } else {
                this.wasConnected = true;
                this.everConnected = true;
            }
            this.proxyClient.flush();
            List<Tab> tabs = getTabs();
            final int n = tabs.size();
            for (int i = 0; i < n; i++) {
                final int index = i;
                try {
                    if (!this.proxyClient.isDead()) {
                        // Update tab
                        //
                        tabs.get(index).update();
                        // Enable tab on initial update
                        //
                        if (this.initialUpdate) {
                            EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    setEnabledAt(index, true);
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    // Disable tab on initial update
                    //
                    if (this.initialUpdate) {
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                setEnabledAt(index, false);
                            }
                        });
                    }
                }
            }

            // plugin GUI update
            for (JConsolePlugin p : this.plugins.keySet()) {
                SwingWorker<?, ?> sw = p.newSwingWorker();
                SwingWorker<?, ?> prevSW = this.plugins.get(p);

                // cancel previous SwingWorker if it is not done yet
                if (prevSW != null && !prevSW.isDone()) {
                    if (prevSW.cancel(true)) {
                        System.out.println("cancelled existing SwingWorker");
                    }
                }

                if (sw == null || sw.getState() == SwingWorker.StateValue.PENDING) {
                    this.plugins.put(p, sw);
                    if (sw != null) {
                        sw.execute();
                        try {
                            sw.get(this.updateInterval - 1000, TimeUnit.MILLISECONDS); // updateInterval minus 1 second
                        } catch (TimeoutException e) {
                            System.out.println("timeout occured after " + (this.updateInterval - 1000) / 1000 + " seconds");
                            WarningProducer.produceWarning("server timeout");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // Set the first enabled tab in the tab's list
            // as the selected tab on initial update
            //
            if (this.initialUpdate) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        // Select first enabled tab if current tab isn't.
                        int index = getSelectedIndex();
                        if (index < 0 || !isEnabledAt(index)) {
                            for (int i = 0; i < n; i++) {
                                if (isEnabledAt(i)) {
                                    setSelectedIndex(i);
                                    break;
                                }
                            }
                        }
                    }
                });
                this.initialUpdate = false;
            }
        }
    }

    public String getHostName() {
        return this.hostName;
    }

    public int getPort() {
        return this.port;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getUrl() {
        return this.url;
    }

    public String getPassword() {
        return this.password;
    }

    public String getConnectionName() {
        return this.proxyClient.connectionName();
    }

    public String getDisplayName() {
        return this.proxyClient.getDisplayName();
    }

    static class TabInfo {

        Class<? extends Tab> tabClass;
        String name;
        boolean tabVisible;

        TabInfo(Class<? extends Tab> tabClass, String name, boolean tabVisible) {
            this.tabClass = tabClass;
            this.name = name;
            this.tabVisible = tabVisible;
        }
    }

    // Convenience methods
    private static String getText(String key, Object... args) {
        return Resources.getText(key, args);
    }

    private void createPluginTabs() {
        // add plugin tabs if not done
        if (!this.pluginTabsAdded) {
            for (JConsolePlugin p : this.plugins.keySet()) {
                Map<String, JPanel> tabs = p.getTabs();
                for (Map.Entry<String, JPanel> e : tabs.entrySet()) {
                    addTab(e.getKey(), e.getValue());
                }
            }
            this.pluginTabsAdded = true;
        }
    }

    private void fireConnectedChange(boolean connected) {
        for (Tab tab : getTabs()) {
            tab.firePropertyChange(JConsoleContext.CONNECTION_STATE_PROPERTY, !connected, connected);
        }
    }
}
