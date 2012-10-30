// AlgoTrader:
// 85 - 108 refresh MBeans
// 387 - 413 add SplitPane to Attributes
/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.tools.jconsole.inspector;

import static sun.tools.jconsole.Resources.getMnemonicInt;
import static sun.tools.jconsole.Resources.getText;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ReflectionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import sun.tools.jconsole.JConsole;
import sun.tools.jconsole.MBeansTab;
import sun.tools.jconsole.Resources;
import sun.tools.jconsole.inspector.XNodeInfo.Type;

@SuppressWarnings("serial")
public class XSheet extends JPanel
        implements ActionListener, NotificationListener {

    private JPanel mainPanel;
    private JPanel southPanel;

    // Node being currently displayed
    private volatile DefaultMutableTreeNode currentNode;

    // MBean being currently displayed
    private volatile XMBean mbean;

    // XMBeanAttributes container
    private XMBeanAttributes mbeanAttributes;

    // XMBeanOperations container
    private XMBeanOperations mbeanOperations;

    // XMBeanNotifications container
    private XMBeanNotifications mbeanNotifications;

    // XMBeanInfo container
    private XMBeanInfo mbeanInfo;

    // Refresh JButton (mbean attributes case)
    private JButton refreshButton;

    // Subscribe/Unsubscribe/Clear JButton (mbean notifications case)
    private JButton clearButton, subscribeButton, unsubscribeButton;

    // Reference to MBeans tab
    private MBeansTab mbeansTab;

    public XSheet(MBeansTab mbeansTab) {
        this.mbeansTab = mbeansTab;
        setupScreen();
        setupRefresh();
    }

    private void setupRefresh() {

        TimerTask timerTask = new TimerTask() {
            public void run() {
                XSheet.this.mbeansTab.workerAdd(new Runnable() {
                    public void run() {
                        try {
                            XSheet.this.mbeanAttributes.refreshAttributes();
                        } catch (Exception ex) {
                            // Should have a trace logged with proper
                            // trace mecchanism
                        }
                    }
                });
            }
        };

        String timerName = "MBean-Timer";
        Timer timer = new Timer(timerName, true);
        timer.schedule(timerTask, 0, this.mbeansTab.getUpdateInterval());
    }

    public void dispose() {
        clear();
        XDataViewer.dispose(this.mbeansTab);
        this.mbeanNotifications.dispose();
    }

    private void setupScreen() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        // add main panel to XSheet
        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new BorderLayout());
        add(this.mainPanel, BorderLayout.CENTER);
        // add south panel to XSheet
        this.southPanel = new JPanel();
        add(this.southPanel, BorderLayout.SOUTH);
        // create the refresh button
        String refreshButtonKey = "MBeansTab.refreshAttributesButton";
        this.refreshButton = new JButton(getText(refreshButtonKey));
        this.refreshButton.setMnemonic(getMnemonicInt(refreshButtonKey));
        this.refreshButton.setToolTipText(getText(refreshButtonKey + ".toolTip"));
        this.refreshButton.addActionListener(this);
        // create the clear button
        String clearButtonKey = "MBeansTab.clearNotificationsButton";
        this.clearButton = new JButton(getText(clearButtonKey));
        this.clearButton.setMnemonic(getMnemonicInt(clearButtonKey));
        this.clearButton.setToolTipText(getText(clearButtonKey + ".toolTip"));
        this.clearButton.addActionListener(this);
        // create the subscribe button
        String subscribeButtonKey = "MBeansTab.subscribeNotificationsButton";
        this.subscribeButton = new JButton(getText(subscribeButtonKey));
        this.subscribeButton.setMnemonic(getMnemonicInt(subscribeButtonKey));
        this.subscribeButton.setToolTipText(getText(subscribeButtonKey + ".toolTip"));
        this.subscribeButton.addActionListener(this);
        // create the unsubscribe button
        String unsubscribeButtonKey = "MBeansTab.unsubscribeNotificationsButton";
        this.unsubscribeButton = new JButton(getText(unsubscribeButtonKey));
        this.unsubscribeButton.setMnemonic(getMnemonicInt(unsubscribeButtonKey));
        this.unsubscribeButton.setToolTipText(getText(unsubscribeButtonKey + ".toolTip"));
        this.unsubscribeButton.addActionListener(this);
        // create XMBeanAttributes container
        this.mbeanAttributes = new XMBeanAttributes(this.mbeansTab);
        // create XMBeanOperations container
        this.mbeanOperations = new XMBeanOperations(this.mbeansTab);
        this.mbeanOperations.addOperationsListener(this);
        // create XMBeanNotifications container
        this.mbeanNotifications = new XMBeanNotifications();
        this.mbeanNotifications.addNotificationsListener(this);
        // create XMBeanInfo container
        this.mbeanInfo = new XMBeanInfo();
    }

    private boolean isSelectedNode(DefaultMutableTreeNode n, DefaultMutableTreeNode cn) {
        return (cn == n);
    }

    // Call on EDT
    private void showErrorDialog(Object message, String title) {
        new ThreadDialog(this, message, title, JOptionPane.ERROR_MESSAGE).run();
    }

    public boolean isMBeanNode(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof XNodeInfo) {
            XNodeInfo uo = (XNodeInfo) userObject;
            return uo.getType().equals(Type.MBEAN);
        }
        return false;
    }

    // Call on EDT
    public synchronized void displayNode(DefaultMutableTreeNode node) {
        clear();
        displayEmptyNode();
        if (node == null) {
            return;
        }
        this.currentNode = node;
        Object userObject = node.getUserObject();
        if (userObject instanceof XNodeInfo) {
            XNodeInfo uo = (XNodeInfo) userObject;
            switch (uo.getType()) {
                case MBEAN:
                    displayMBeanNode(node);
                    break;
                case NONMBEAN:
                    displayEmptyNode();
                    break;
                case ATTRIBUTES:
                    displayMBeanAttributesNode(node);
                    break;
                case OPERATIONS:
                    displayMBeanOperationsNode(node);
                    break;
                case NOTIFICATIONS:
                    displayMBeanNotificationsNode(node);
                    break;
                case ATTRIBUTE:
                case OPERATION:
                case NOTIFICATION:
                    displayMetadataNode(node);
                    break;
                default:
                    displayEmptyNode();
                    break;
            }
        } else {
            displayEmptyNode();
        }
    }

    // Call on EDT
    private void displayMBeanNode(final DefaultMutableTreeNode node) {
        final XNodeInfo uo = (XNodeInfo) node.getUserObject();
        if (!uo.getType().equals(Type.MBEAN)) {
            return;
        }
        this.mbean = (XMBean) uo.getData();
        SwingWorker<MBeanInfo,Void> sw = new SwingWorker<MBeanInfo,Void>() {
            @Override
            public MBeanInfo doInBackground() throws InstanceNotFoundException,
                    IntrospectionException, ReflectionException, IOException {
                return XSheet.this.mbean.getMBeanInfo();
            }
            @Override
            protected void done() {
                try {
                    MBeanInfo mbi = get();
                    if (mbi != null) {
                        if (!isSelectedNode(node, XSheet.this.currentNode)) return;
                        XSheet.this.mbeanInfo.addMBeanInfo(XSheet.this.mbean, mbi);
                        invalidate();
                        XSheet.this.mainPanel.removeAll();
                        XSheet.this.mainPanel.add(XSheet.this.mbeanInfo, BorderLayout.CENTER);
                        XSheet.this.southPanel.setVisible(false);
                        XSheet.this.southPanel.removeAll();
                        validate();
                        repaint();
                    }
                } catch (Exception e) {
                    Throwable t = Utils.getActualException(e);
                    if (JConsole.isDebug()) {
                        System.err.println("Couldn't get MBeanInfo for MBean [" +
                                XSheet.this.mbean.getObjectName() + "]");
                        t.printStackTrace();
                    }
                    showErrorDialog(t.toString(),
                            Resources.getText("Problem displaying MBean"));
                }
            }
        };
        sw.execute();
    }

    // Call on EDT
    private void displayMetadataNode(final DefaultMutableTreeNode node) {
        final XNodeInfo uo = (XNodeInfo) node.getUserObject();
        final XMBeanInfo mbi = this.mbeanInfo;
        switch (uo.getType()) {
            case ATTRIBUTE:
                SwingWorker<MBeanAttributeInfo,Void> sw =
                        new SwingWorker<MBeanAttributeInfo,Void>() {
                    @Override
                    public MBeanAttributeInfo doInBackground() {
                        Object attrData = uo.getData();
                        XSheet.this.mbean = (XMBean) ((Object[]) attrData)[0];
                        MBeanAttributeInfo mbai =
                                (MBeanAttributeInfo) ((Object[]) attrData)[1];
                        XSheet.this.mbeanAttributes.loadAttributes(XSheet.this.mbean, new MBeanInfo(
                                null, null, new MBeanAttributeInfo[] {mbai},
                                null, null, null));
                        return mbai;
                    }
                    @Override
                    protected void done() {
                        try {
                            MBeanAttributeInfo mbai = get();
                            if (!isSelectedNode(node, XSheet.this.currentNode)) return;
                            invalidate();
                            XSheet.this.mainPanel.removeAll();
                            JPanel attributePanel =
                                    new JPanel(new BorderLayout());
                            JPanel attributeBorderPanel =
                                    new JPanel(new BorderLayout());
                            attributeBorderPanel.setBorder(
                                    BorderFactory.createTitledBorder(
                                    Resources.getText("Attribute value")));
                            JPanel attributeValuePanel =
                                    new JPanel(new BorderLayout());
                            attributeValuePanel.setBorder(
                                    LineBorder.createGrayLineBorder());
                            attributeValuePanel.add(XSheet.this.mbeanAttributes.getTableHeader(),
                                    BorderLayout.PAGE_START);
                            attributeValuePanel.add(XSheet.this.mbeanAttributes,
                                    BorderLayout.CENTER);
                            attributeBorderPanel.add(attributeValuePanel,
                                    BorderLayout.CENTER);
                            JPanel refreshButtonPanel = new JPanel();
                            refreshButtonPanel.add(XSheet.this.refreshButton);
                            attributeBorderPanel.add(refreshButtonPanel,
                                    BorderLayout.SOUTH);
                            XSheet.this.refreshButton.setEnabled(true);
                            attributePanel.add(attributeBorderPanel,
                                    BorderLayout.NORTH);
                            mbi.addMBeanAttributeInfo(mbai);
                            attributePanel.add(mbi, BorderLayout.CENTER);
                            XSheet.this.mainPanel.add(attributePanel,
                                    BorderLayout.CENTER);
                            XSheet.this.southPanel.setVisible(false);
                            XSheet.this.southPanel.removeAll();
                            validate();
                            repaint();
                        } catch (Exception e) {
                            Throwable t = Utils.getActualException(e);
                            if (JConsole.isDebug()) {
                                System.err.println("Problem displaying MBean " +
                                        "attribute for MBean [" +
                                        XSheet.this.mbean.getObjectName() + "]");
                                t.printStackTrace();
                            }
                            showErrorDialog(t.toString(),
                                    Resources.getText("Problem displaying MBean"));
                        }
                    }
                };
                sw.execute();
                break;
            case OPERATION:
                Object operData = uo.getData();
                this.mbean = (XMBean) ((Object[]) operData)[0];
                MBeanOperationInfo mboi =
                        (MBeanOperationInfo) ((Object[]) operData)[1];
                this.mbeanOperations.loadOperations(this.mbean,
                        new MBeanInfo(null, null, null, null,
                        new MBeanOperationInfo[] {mboi}, null));
                invalidate();
                this.mainPanel.removeAll();
                JPanel operationPanel = new JPanel(new BorderLayout());
                JPanel operationBorderPanel = new JPanel(new BorderLayout());
                operationBorderPanel.setBorder(BorderFactory.createTitledBorder(
                        Resources.getText("Operation invocation")));
                operationBorderPanel.add(new JScrollPane(this.mbeanOperations));
                operationPanel.add(operationBorderPanel, BorderLayout.NORTH);
                mbi.addMBeanOperationInfo(mboi);
                operationPanel.add(mbi, BorderLayout.CENTER);
                this.mainPanel.add(operationPanel, BorderLayout.CENTER);
                this.southPanel.setVisible(false);
                this.southPanel.removeAll();
                validate();
                repaint();
                break;
            case NOTIFICATION:
                Object notifData = uo.getData();
                invalidate();
                this.mainPanel.removeAll();
                mbi.addMBeanNotificationInfo((MBeanNotificationInfo) notifData);
                this.mainPanel.add(mbi, BorderLayout.CENTER);
                this.southPanel.setVisible(false);
                this.southPanel.removeAll();
                validate();
                repaint();
                break;
        }
    }

    // Call on EDT
    private void displayMBeanAttributesNode(final DefaultMutableTreeNode node) {
        final XNodeInfo uo = (XNodeInfo) node.getUserObject();
        if (!uo.getType().equals(Type.ATTRIBUTES)) {
            return;
        }
        this.mbean = (XMBean) uo.getData();
        SwingWorker<MBeanInfo,Void> sw = new SwingWorker<MBeanInfo,Void>() {
            @Override
            public MBeanInfo doInBackground() throws InstanceNotFoundException,
                    IntrospectionException, ReflectionException, IOException {
                XSheet.this.mbeanAttributes.loadAttributes(XSheet.this.mbean, XSheet.this.mbean.getMBeanInfo());
                return XSheet.this.mbean.getMBeanInfo();
            }
            @Override
            protected void done() {
                try {
                    MBeanInfo mbi = get();
                    invalidate();
                    XSheet.this.mainPanel.removeAll();
                    JScrollPane attributeScrollPane = new JScrollPane(XSheet.this.mbeanAttributes);

                    if (mbi != null && mbi.getOperations().length > 0) {

                        XSheet.this.mbeanOperations.loadOperations(XSheet.this.mbean, mbi);
                        JScrollPane operationScrollPane = new JScrollPane(XSheet.this.mbeanOperations);

                        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, attributeScrollPane, operationScrollPane);
                        splitPane.setResizeWeight(0.5);

                        XSheet.this.mainPanel.add(splitPane, BorderLayout.CENTER);
                    } else {
                        XSheet.this.mainPanel.add(attributeScrollPane, BorderLayout.CENTER);
                    }

                    // add the refresh button to the south panel
                    XSheet.this.southPanel.removeAll();
                    XSheet.this.southPanel.add(XSheet.this.refreshButton, BorderLayout.SOUTH);
                    XSheet.this.southPanel.setVisible(true);
                    XSheet.this.refreshButton.setEnabled(true);
                    validate();
                    repaint();
                } catch (Exception e) {
                    Throwable t = Utils.getActualException(e);
                    if (JConsole.isDebug()) {
                        System.err.println("Problem displaying MBean " +
                                "attributes for MBean [" +
                                XSheet.this.mbean.getObjectName() + "]");
                        t.printStackTrace();
                    }
                    showErrorDialog(t.toString(),
                            Resources.getText("Problem displaying MBean"));
                }
            }
        };
        sw.execute();
    }

    // Call on EDT
    private void displayMBeanOperationsNode(final DefaultMutableTreeNode node) {
        final XNodeInfo uo = (XNodeInfo) node.getUserObject();
        if (!uo.getType().equals(Type.OPERATIONS)) {
            return;
        }
        this.mbean = (XMBean) uo.getData();
        SwingWorker<MBeanInfo,Void> sw = new SwingWorker<MBeanInfo,Void>() {
            @Override
            public MBeanInfo doInBackground() throws InstanceNotFoundException,
                    IntrospectionException, ReflectionException, IOException {
                return XSheet.this.mbean.getMBeanInfo();
            }
            @Override
            protected void done() {
                try {
                    MBeanInfo mbi = get();
                    if (mbi != null) {
                        if (!isSelectedNode(node, XSheet.this.currentNode)) return;
                        XSheet.this.mbeanOperations.loadOperations(XSheet.this.mbean, mbi);
                        invalidate();
                        XSheet.this.mainPanel.removeAll();
                        JPanel borderPanel = new JPanel(new BorderLayout());
                        borderPanel.setBorder(BorderFactory.createTitledBorder(
                                Resources.getText("Operation invocation")));
                        borderPanel.add(new JScrollPane(XSheet.this.mbeanOperations));
                        XSheet.this.mainPanel.add(borderPanel, BorderLayout.CENTER);
                        XSheet.this.southPanel.setVisible(false);
                        XSheet.this.southPanel.removeAll();
                        validate();
                        repaint();
                    }
                } catch (Exception e) {
                    Throwable t = Utils.getActualException(e);
                    if (JConsole.isDebug()) {
                        System.err.println("Problem displaying MBean " +
                                "operations for MBean [" +
                                XSheet.this.mbean.getObjectName() + "]");
                        t.printStackTrace();
                    }
                    showErrorDialog(t.toString(),
                            Resources.getText("Problem displaying MBean"));
                }
            }
        };
        sw.execute();
    }

    // Call on EDT
    private void displayMBeanNotificationsNode(DefaultMutableTreeNode node) {
        final XNodeInfo uo = (XNodeInfo) node.getUserObject();
        if (!uo.getType().equals(Type.NOTIFICATIONS)) {
            return;
        }
        this.mbean = (XMBean) uo.getData();
        this.mbeanNotifications.loadNotifications(this.mbean);
        updateNotifications();
        invalidate();
        this.mainPanel.removeAll();
        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.setBorder(BorderFactory.createTitledBorder(
                Resources.getText("Notification buffer")));
        borderPanel.add(new JScrollPane(this.mbeanNotifications));
        this.mainPanel.add(borderPanel, BorderLayout.CENTER);
        // add the subscribe/unsubscribe/clear buttons to the south panel
        this.southPanel.removeAll();
        this.southPanel.add(this.subscribeButton, BorderLayout.WEST);
        this.southPanel.add(this.unsubscribeButton, BorderLayout.CENTER);
        this.southPanel.add(this.clearButton, BorderLayout.EAST);
        this.southPanel.setVisible(true);
        this.subscribeButton.setEnabled(true);
        this.unsubscribeButton.setEnabled(true);
        this.clearButton.setEnabled(true);
        validate();
        repaint();
    }

    // Call on EDT
    private void displayEmptyNode() {
        invalidate();
        this.mainPanel.removeAll();
        this.southPanel.removeAll();
        validate();
        repaint();
    }

    /**
     * Subscribe button action.
     */
    private void registerListener() {
        new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground()
            throws InstanceNotFoundException, IOException {
                XSheet.this.mbeanNotifications.registerListener(XSheet.this.currentNode);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    updateNotifications();
                    validate();
                } catch (Exception e) {
                    Throwable t = Utils.getActualException(e);
                    if (JConsole.isDebug()) {
                        System.err.println("Problem adding listener");
                        t.printStackTrace();
                    }
                    showErrorDialog(t.getMessage(),
                            Resources.getText("Problem adding listener"));
                }
            }
        }.execute();
    }

    /**
     * Unsubscribe button action.
     */
    private void unregisterListener() {
        new SwingWorker<Boolean, Void>() {
            @Override
            public Boolean doInBackground() {
                return XSheet.this.mbeanNotifications.unregisterListener(XSheet.this.currentNode);
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        updateNotifications();
                        validate();
                    }
                } catch (Exception e) {
                    Throwable t = Utils.getActualException(e);
                    if (JConsole.isDebug()) {
                        System.err.println("Problem removing listener");
                        t.printStackTrace();
                    }
                    showErrorDialog(t.getMessage(),
                            Resources.getText("Problem removing listener"));
                }
            }
        }.execute();
    }

    /**
     * Refresh button action.
     */
    private void refreshAttributes() {
        this.mbeanAttributes.refreshAttributes();
    }

    // Call on EDT
    private void updateNotifications() {
        if (this.mbeanNotifications.isListenerRegistered(this.mbean)) {
            long received = this.mbeanNotifications.getReceivedNotifications(this.mbean);
            updateReceivedNotifications(this.currentNode, received, false);
        } else {
            clearNotifications();
        }
    }

    /**
     * Update notification node label in MBean tree: "Notifications[received]".
     */
    // Call on EDT
    private void updateReceivedNotifications(
            DefaultMutableTreeNode emitter, long received, boolean bold) {
        String text = Resources.getText("Notifications") + "[" + received + "]";
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)
        this.mbeansTab.getTree().getLastSelectedPathComponent();
        if (bold && emitter != selectedNode) {
            text = "<html><b>" + text + "</b></html>";
        }
        updateNotificationsNodeLabel(emitter, text);
    }

    /**
     * Update notification node label in MBean tree: "Notifications".
     */
    // Call on EDT
    private void clearNotifications() {
        updateNotificationsNodeLabel(this.currentNode,
                Resources.getText("Notifications"));
    }

    /**
     * Update notification node label in MBean tree: "Notifications[0]".
     */
    // Call on EDT
    private void clearNotifications0() {
        updateNotificationsNodeLabel(this.currentNode,
                Resources.getText("Notifications") + "[0]");
    }

    /**
     * Update the label of the supplied MBean tree node.
     */
    // Call on EDT
    private void updateNotificationsNodeLabel(
            DefaultMutableTreeNode node, String label) {
        synchronized (this.mbeansTab.getTree()) {
            invalidate();
            XNodeInfo oldUserObject = (XNodeInfo) node.getUserObject();
            XNodeInfo newUserObject = new XNodeInfo(
                    oldUserObject.getType(), oldUserObject.getData(),
                    label, oldUserObject.getToolTipText());
            node.setUserObject(newUserObject);
            DefaultTreeModel model =
                    (DefaultTreeModel) this.mbeansTab.getTree().getModel();
            model.nodeChanged(node);
            validate();
            repaint();
        }
    }

    /**
     * Clear button action.
     */
    // Call on EDT
    private void clearCurrentNotifications() {
        this.mbeanNotifications.clearCurrentNotifications();
        if (this.mbeanNotifications.isListenerRegistered(this.mbean)) {
            // Update notifs in MBean tree "Notifications[0]".
            //
            // Notification buffer has been cleared with a listener been
            // registered so add "[0]" at the end of the node label.
            //
            clearNotifications0();
        } else {
            // Update notifs in MBean tree "Notifications".
            //
            // Notification buffer has been cleared without a listener been
            // registered so don't add "[0]" at the end of the node label.
            //
            clearNotifications();
        }
    }

    // Call on EDT
    private void clear() {
        this.mbeanAttributes.stopCellEditing();
        this.mbeanAttributes.emptyTable();
        this.mbeanAttributes.removeAttributes();
        this.mbeanOperations.removeOperations();
        this.mbeanNotifications.stopCellEditing();
        this.mbeanNotifications.emptyTable();
        this.mbeanNotifications.disableNotifications();
        this.mbean = null;
        this.currentNode = null;
    }

    /**
     * Notification listener: handles asynchronous reception
     * of MBean operation results and MBean notifications.
     */
    // Call on EDT
    public void handleNotification(Notification e, Object handback) {
        // Operation result
        if (e.getType().equals(XOperations.OPERATION_INVOCATION_EVENT)) {
            final Object message;
            if (handback == null) {
                JTextArea textArea = new JTextArea("null");
                textArea.setEditable(false);
                textArea.setEnabled(true);
                textArea.setRows(textArea.getLineCount());
                message = textArea;
            } else {
                Component comp = this.mbeansTab.getDataViewer().
                        createOperationViewer(handback, this.mbean);
                if (comp == null) {
                    JTextArea textArea = new JTextArea(handback.toString());
                    textArea.setEditable(false);
                    textArea.setEnabled(true);
                    textArea.setRows(textArea.getLineCount());
                    JScrollPane scrollPane = new JScrollPane(textArea);
                    Dimension d = scrollPane.getPreferredSize();
                    if (d.getWidth() > 400 || d.getHeight() > 250) {
                        scrollPane.setPreferredSize(new Dimension(400, 250));
                    }
                    message = scrollPane;
                } else {
                    if (!(comp instanceof JScrollPane)) {
                        comp = new JScrollPane(comp);
                    }
                    Dimension d = comp.getPreferredSize();
                    if (d.getWidth() > 400 || d.getHeight() > 250) {
                        comp.setPreferredSize(new Dimension(400, 250));
                    }
                    message = comp;
                }
            }
            new ThreadDialog(
                    (Component) e.getSource(),
                    message,
                    Resources.getText("Operation return value"),
                    JOptionPane.INFORMATION_MESSAGE).run();
        }
        // Got notification
        else if (e.getType().equals(
                XMBeanNotifications.NOTIFICATION_RECEIVED_EVENT)) {
            DefaultMutableTreeNode emitter = (DefaultMutableTreeNode) handback;
            Long received = (Long) e.getUserData();
            updateReceivedNotifications(emitter, received.longValue(), true);
        }
    }

    /**
     * Action listener: handles actions in panel buttons
     */
    // Call on EDT
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JButton) {
            JButton button = (JButton) e.getSource();
            // Refresh button
            if (button == this.refreshButton) {
                new SwingWorker<Void, Void>() {
                    @Override
                    public Void doInBackground() {
                        refreshAttributes();
                        return null;
                    }
                }.execute();
                return;
            }
            // Clear button
            if (button == this.clearButton) {
                clearCurrentNotifications();
                return;
            }
            // Subscribe button
            if (button == this.subscribeButton) {
                registerListener();
                return;
            }
            // Unsubscribe button
            if (button == this.unsubscribeButton) {
                unregisterListener();
                return;
            }
        }
    }
}
