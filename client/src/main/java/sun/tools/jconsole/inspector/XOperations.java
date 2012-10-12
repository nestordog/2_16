// AlgoTrader:
// line 84 - 99: sort operations alphabetically & get the number of non-getter operations
// line 111 - 113: do not display getters as operations
// line 185 - 201: format error message
/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.tools.jconsole.inspector;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import sun.tools.jconsole.JConsole;
import sun.tools.jconsole.MBeansTab;
import sun.tools.jconsole.Resources;

public abstract class XOperations extends JPanel implements ActionListener {

    public final static String OPERATION_INVOCATION_EVENT =
            "jam.xoperations.invoke.result";
    private java.util.List<NotificationListener> notificationListenersList;

    private Hashtable<JButton, OperationEntry> operationEntryTable;

    private XMBean mbean;
    private MBeanInfo mbeanInfo;
    private MBeansTab mbeansTab;

    public XOperations(MBeansTab mbeansTab) {
        super(new GridLayout(1,1));
        this.mbeansTab = mbeansTab;
        this.operationEntryTable = new Hashtable<JButton, OperationEntry>();
        ArrayList<NotificationListener> l =
                new ArrayList<NotificationListener>(1);
        this.notificationListenersList =
                Collections.synchronizedList(l);
    }

    // Call on EDT
    public void removeOperations() {
        removeAll();
    }

    // Call on EDT
    public void loadOperations(XMBean mbean,MBeanInfo mbeanInfo) {
        this.mbean = mbean;
        this.mbeanInfo = mbeanInfo;
        // add operations information
        MBeanOperationInfo operations[] = mbeanInfo.getOperations();
        invalidate();

        // remove listeners, if any
        Component listeners[] = getComponents();
        for (Component listener : listeners)
            if (listener instanceof JButton)
                ((JButton)listener).removeActionListener(this);

        removeAll();
        setLayout(new BorderLayout());

        // sort operations alphabetically
        Arrays.sort(operations, 0, operations.length, new Comparator<MBeanOperationInfo>() {
            @Override
            public int compare(MBeanOperationInfo arg0, MBeanOperationInfo arg1) {
                return arg0.getName().compareTo(arg1.getName());
            }
        });

        // get the number of non-getter operations
        int operationsLength = 0;
        for (MBeanOperationInfo operation : operations) {
            Object role = operation.getDescriptor().getFieldValue("role");
            if (role == null || !"getter".equals(role)) {
                operationsLength++;
            }
        }

        JButton methodButton;
        JLabel methodLabel;
        JPanel innerPanelLeft,innerPanelRight;
        JPanel outerPanelLeft,outerPanelRight;
        outerPanelLeft = new JPanel(new GridLayout(operationsLength, 1));
        outerPanelRight = new JPanel(new GridLayout(operationsLength, 1));

        for (MBeanOperationInfo operation : operations) {

            // only display non-getter operations
            Object role = operation.getDescriptor().getFieldValue("role");
            if (role == null || !"getter".equals(role)) {

                innerPanelLeft  = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                innerPanelRight = new JPanel(new FlowLayout(FlowLayout.LEFT));
                String returnType = operation.getReturnType();
                if (returnType == null) {
                    methodLabel = new JLabel("null", JLabel.RIGHT);
                    if (JConsole.isDebug()) {
                        System.err.println(
                                "WARNING: The operation's return type " +
                                "shouldn't be \"null\". Check how the " +
                                "MBeanOperationInfo for the \"" +
                                operation.getName() + "\" operation has " +
                                "been defined in the MBean's implementation code.");
                    }
                } else {
                    methodLabel = new JLabel(
                            Utils.getReadableClassName(returnType), JLabel.RIGHT);
                }
                innerPanelLeft.add(methodLabel);
                if (methodLabel.getText().length()>20) {
                    methodLabel.setText(methodLabel.getText().
                            substring(methodLabel.getText().
                            lastIndexOf(".")+1,
                            methodLabel.getText().length()));
                }

                methodButton = new JButton(operation.getName());
                methodButton.setToolTipText(operation.getDescription());
                boolean callable = isCallable(operation.getSignature());
                if(callable)
                    methodButton.addActionListener(this);
                else
                    methodButton.setEnabled(false);

                MBeanParameterInfo[] signature = operation.getSignature();
                OperationEntry paramEntry = new OperationEntry(operation,
                        callable,
                        methodButton,
                        this);
                this.operationEntryTable.put(methodButton, paramEntry);
                innerPanelRight.add(methodButton);
                if(signature.length==0)
                    innerPanelRight.add(new JLabel("( )",JLabel.CENTER));
                else
                    innerPanelRight.add(paramEntry);

                outerPanelLeft.add(innerPanelLeft,BorderLayout.WEST);
                outerPanelRight.add(innerPanelRight,BorderLayout.CENTER);
            }
        }
        add(outerPanelLeft,BorderLayout.WEST);
        add(outerPanelRight,BorderLayout.CENTER);
        validate();
    }

    private boolean isCallable(MBeanParameterInfo[] signature) {
        for(int i = 0; i < signature.length; i++) {
            if(!Utils.isEditableType(signature[i].getType()))
                return false;
        }
        return true;
    }

    // Call on EDT
    @Override
    public void actionPerformed(final ActionEvent e) {
        performInvokeRequest((JButton)e.getSource());
    }

    void performInvokeRequest(final JButton button) {
        final OperationEntry entryIf = this.operationEntryTable.get(button);
        new SwingWorker<Object, Void>() {
            @Override
            public Object doInBackground() throws Exception {
                return XOperations.this.mbean.invoke(button.getText(),
                        entryIf.getParameters(), entryIf.getSignature());
            }
            @Override
            protected void done() {
                try {
                    Object result = get();
                    // sends result notification to upper level if
                    // there is a return value
                    if (entryIf.getReturnType() != null &&
                            !entryIf.getReturnType().equals(Void.TYPE.getName()) &&
                            !entryIf.getReturnType().equals(Void.class.getName()))
                        fireChangedNotification(OPERATION_INVOCATION_EVENT, button, result);
                    else
                        new ThreadDialog(
                                button,
                                Resources.getText("Method successfully invoked"),
                                Resources.getText("Info"),
                                JOptionPane.INFORMATION_MESSAGE).run();
                } catch (Throwable t) {
                    t = Utils.getActualException(t);
                    if (JConsole.isDebug()) {
                        t.printStackTrace();
                    }

                    // get the "last" cause
                    Throwable cause = t;
                    while (cause.getCause() != null) {
                        cause = cause.getCause();
                    }

                    // print the header (multiline)
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(t.toString().replace("-->", "\n-->"));
                    buffer.append("\n\n");

                    // print the stacktrace
                    StackTraceElement[] trace = cause.getStackTrace();
                    for (int i = 0; i < Math.min(20, trace.length); i++) {
                        buffer.append("\tat " + trace[i] + "\n");
                    }

                    new ThreadDialog(
                            button,
                            Resources.getText("Problem invoking") + " " +
                            button.getText() + " : " + buffer.toString(),
                            Resources.getText("Error"),
                            JOptionPane.ERROR_MESSAGE).run();
                }
            }
        }.execute();
    }

    public void addOperationsListener(NotificationListener nl) {
        this.notificationListenersList.add(nl);
    }

    public void removeOperationsListener(NotificationListener nl) {
        this.notificationListenersList.remove(nl);
    }

    // Call on EDT
    private void fireChangedNotification(
            String type, Object source, Object handback) {
        Notification n = new Notification(type, source, 0);
        for(NotificationListener nl : this.notificationListenersList)
            nl.handleNotification(n, handback);
    }

    protected abstract MBeanOperationInfo[]
            updateOperations(MBeanOperationInfo[] operations);
}
