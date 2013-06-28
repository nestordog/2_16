// AlgoTrader: 55 MBeans -> Services
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

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerDelegate;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import sun.tools.jconsole.ProxyClient.SnapshotMBeanServerConnection;
import sun.tools.jconsole.inspector.Utils;
import sun.tools.jconsole.inspector.XDataViewer;
import sun.tools.jconsole.inspector.XSheet;
import sun.tools.jconsole.inspector.XTree;
import sun.tools.jconsole.inspector.XTreeRenderer;

import com.sun.tools.jconsole.JConsoleContext;

@SuppressWarnings("serial")
public class MBeansTab extends Tab implements
        NotificationListener, PropertyChangeListener,
        TreeSelectionListener, TreeWillExpandListener {

    private XTree tree;
    private XSheet sheet;
    private XDataViewer viewer;

    public static String getTabName() {
        return "Services";
    }

    public MBeansTab(final VMPanel vmPanel) {
        super(vmPanel, getTabName());
        addPropertyChangeListener(this);
        setupTab();
    }

    public XDataViewer getDataViewer() {
        return this.viewer;
    }

    public XTree getTree() {
        return this.tree;
    }

    public XSheet getSheet() {
        return this.sheet;
    }

    @Override
    public void dispose() {
        super.dispose();
        this.sheet.dispose();
    }

    public int getUpdateInterval() {
        return this.vmPanel.getUpdateInterval();
    }

    private void buildMBeanServerView() {
        new SwingWorker<Set<ObjectName>, Void>() {
            @Override
            public Set<ObjectName> doInBackground() {
                // Register listener for MBean registration/unregistration
                //
                try {
                    getMBeanServerConnection().addNotificationListener(
                            MBeanServerDelegate.DELEGATE_NAME,
                            MBeansTab.this,
                            null,
                            null);
                } catch (InstanceNotFoundException e) {
                    // Should never happen because the MBeanServerDelegate
                    // is always present in any standard MBeanServer
                    //
                    if (JConsole.isDebug()) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    if (JConsole.isDebug()) {
                        e.printStackTrace();
                    }
                    MBeansTab.this.vmPanel.getProxyClient().markAsDead();
                    return null;
                }
                // Retrieve MBeans from MBeanServer
                //
                Set<ObjectName> mbeans = null;
                try {
                    mbeans = getMBeanServerConnection().queryNames(null, null);
                } catch (IOException e) {
                    if (JConsole.isDebug()) {
                        e.printStackTrace();
                    }
                    MBeansTab.this.vmPanel.getProxyClient().markAsDead();
                    return null;
                }
                return mbeans;
            }
            @Override
            protected void done() {
                try {
                    // Wait for mbsc.queryNames() result
                    Set<ObjectName> mbeans = get();
                    // Do not display anything until the new tree has been built
                    //
                    MBeansTab.this.tree.setVisible(false);
                    // Cleanup current tree
                    //
                    MBeansTab.this.tree.removeAll();
                    // Add MBeans to tree
                    //
                    MBeansTab.this.tree.addMBeansToView(mbeans);
                    // Display the new tree
                    //
                    MBeansTab.this.tree.setVisible(true);
                } catch (Exception e) {
                    Throwable t = Utils.getActualException(e);
                    if (JConsole.isDebug()) {
                        System.err.println("Problem at MBean tree construction");
                        t.printStackTrace();
                    }
                }
            }
        }.execute();
    }

    public MBeanServerConnection getMBeanServerConnection() {
        return this.vmPanel.getProxyClient().getMBeanServerConnection();
    }

    public SnapshotMBeanServerConnection getSnapshotMBeanServerConnection() {
        return this.vmPanel.getProxyClient().getSnapshotMBeanServerConnection();
    }

    @Override
    public void update() {
        // Ping the connection to see if it is still alive. At
        // some point the ProxyClient class should centralize
        // the connection aliveness monitoring and no longer
        // rely on the custom tabs to ping the connections.
        //
        try {
            getMBeanServerConnection().getDefaultDomain();
        } catch (IOException ex) {
            this.vmPanel.getProxyClient().markAsDead();
        }
    }

    private void setupTab() {
        // set up the split pane with the MBean tree and MBean sheet panels
        setLayout(new BorderLayout());
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(160);
        mainSplit.setBorder(BorderFactory.createEmptyBorder());

        // set up the MBean tree panel (left pane)
        this.tree = new XTree(this);
        this.tree.setCellRenderer(new XTreeRenderer());
        this.tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.tree.addTreeSelectionListener(this);
        this.tree.addTreeWillExpandListener(this);
        this.tree.addMouseListener(this.ml);
        JScrollPane theScrollPane = new JScrollPane(
                this.tree,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(theScrollPane, BorderLayout.CENTER);
        mainSplit.add(treePanel, JSplitPane.LEFT, 0);

        // set up the MBean sheet panel (right pane)
        this.viewer = new XDataViewer(this);
        this.sheet = new XSheet(this);
        mainSplit.add(this.sheet, JSplitPane.RIGHT, 0);

        add(mainSplit);
    }

    /* notification listener:  handleNotification */
    @Override
    public void handleNotification(
            final Notification notification, Object handback) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (notification instanceof MBeanServerNotification) {
                    ObjectName mbean =
                            ((MBeanServerNotification) notification).getMBeanName();
                    if (notification.getType().equals(
                            MBeanServerNotification.REGISTRATION_NOTIFICATION)) {
                        MBeansTab.this.tree.addMBeanToView(mbean);
                    } else if (notification.getType().equals(
                            MBeanServerNotification.UNREGISTRATION_NOTIFICATION)) {
                        MBeansTab.this.tree.removeMBeanFromView(mbean);
                    }
                }
            }
        });
    }

    /* property change listener:  propertyChange */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (JConsoleContext.CONNECTION_STATE_PROPERTY.equals(evt.getPropertyName())) {
            boolean connected = (Boolean) evt.getNewValue();
            if (connected) {
                buildMBeanServerView();
            } else {
                this.sheet.dispose();
            }
        }
    }

    /* tree selection listener: valueChanged */
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
        this.sheet.displayNode(node);
    }
    /* tree mouse listener: mousePressed */
    private MouseListener ml = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getClickCount() == 1) {
                int selRow = MBeansTab.this.tree.getRowForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    TreePath selPath =
                            MBeansTab.this.tree.getPathForLocation(e.getX(), e.getY());
                    DefaultMutableTreeNode node =
                            (DefaultMutableTreeNode) selPath.getLastPathComponent();
                    if (MBeansTab.this.sheet.isMBeanNode(node)) {
                        MBeansTab.this.tree.expandPath(selPath);
                    }
                }
            }
        }
    };

    /* tree will expand listener: treeWillExpand */
    @Override
    public void treeWillExpand(TreeExpansionEvent e)
            throws ExpandVetoException {
        TreePath path = e.getPath();
        if (!this.tree.hasBeenExpanded(path)) {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) path.getLastPathComponent();
            if (this.sheet.isMBeanNode(node) && !this.tree.hasMetadataNodes(node)) {
                this.tree.addMetadataNodes(node);
            }
        }
    }

    /* tree will expand listener: treeWillCollapse */
    @Override
    public void treeWillCollapse(TreeExpansionEvent e)
            throws ExpandVetoException {
    }
}
