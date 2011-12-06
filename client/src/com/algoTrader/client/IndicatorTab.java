package com.algoTrader.client;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;

import org.jfree.beans.LegendPosition;

import com.algoTrader.service.ManagementService;
import com.algoTrader.vo.DiagramVO;
import com.algoTrader.vo.ParameterVO;

public class IndicatorTab extends javax.swing.JPanel {

    private static final long serialVersionUID = 2673291828098110545L;

    private MBeanServerConnection mbsc = null;
    private Collection<DiagramVO> diagrams = null;
    private TimeSeriesChart timeSeriesChart = null;
    private SelectionPanel selectionPanel = null;

    public IndicatorTab() {

        setLayout(new BorderLayout(10, 10));

        this.timeSeriesChart = new TimeSeriesChart();
        this.timeSeriesChart.setSource("");
        this.timeSeriesChart.setSubtitle("");
        this.timeSeriesChart.setTitle("Indicators");
        this.timeSeriesChart.setLegendPosition(LegendPosition.RIGHT);
        this.add(this.timeSeriesChart, BorderLayout.CENTER);

        this.selectionPanel = new SelectionPanel();
        this.add(IndicatorTab.this.selectionPanel, BorderLayout.WEST);
    }

    void setMBeanServerConnection(MBeanServerConnection mBeanServerConnection) {

        this.mbsc = mBeanServerConnection;
    }

    // Return a new SwingWorker for UI update
    public SwingWorker<JMXResult, Object> newSwingWorker() {

        return new Worker();
    }

    public class Worker extends SwingWorker<JMXResult, Object> {

        @Override
        protected void done() {

            try {
                JMXResult result = get();

                // init the selectionPanel if we got back diagrams
                if (result.getDiagrams() != null) {
                    IndicatorTab.this.selectionPanel.initSelectionPanel(result.getDiagrams());
                }

                // update the timeSeriesChart if we got back events
                if (result.getEvents() != null) {
                    IndicatorTab.this.timeSeriesChart.updateEvents(result.getDiagram(), result.getEvents());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public JMXResult doInBackground() throws Exception {

            JMXResult result = new JMXResult();

            ObjectName mbeanName = new ObjectName("com.algoTrader.service:name=managementService");

            // see if the service is available
            try {
                IndicatorTab.this.mbsc.getObjectInstance(mbeanName);
            } catch (InstanceNotFoundException e) {
                return result;
            } catch (IOException e) {
                return result;
            }

            // get the managementService
            ManagementService managementService = JMX.newMBeanProxy(IndicatorTab.this.mbsc, mbeanName, ManagementService.class);

            // retrieve the diagrams if necessary
            if (IndicatorTab.this.diagrams == null) {
                List<DiagramVO> diagrams = managementService.getIndicatorDiagrams(true);
                IndicatorTab.this.diagrams = diagrams;

                // return the diagrams, so the SelectionPanel can be initialized
                result.setDiagrams(diagrams);
            }

            // retrieve events for the selected diagram
            if (IndicatorTab.this.diagrams != null) {
                for (DiagramVO diagram : IndicatorTab.this.diagrams) {
                    if (diagram.isSelected()) {
                        result.setDiagram(diagram);

                        List<Object> events = managementService.getAllEvents(diagram.getStatementName());
                        result.setEvents(events);
                    }
                }
            }

            return result;
        }
    }

    public class SelectionPanel extends JPanel {

        private static final long serialVersionUID = -4151191979796774273L;

        public void initSelectionPanel(List<DiagramVO> diagrams) {

            this.setLayout(new GridBagLayout());

            ButtonGroup group = new ButtonGroup();
            int y = 0;

            for (DiagramVO diagram : diagrams) {

                // create the radio button for the diagrams
                JRadioButton diagramButton = new JRadioButton();

                ButtonModel diagramButtonModel = new DiagramButtonModel(diagram);
                diagramButtonModel.setSelected(diagram.isSelected());
                diagramButton.setModel(diagramButtonModel);

                diagramButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        newSwingWorker().execute();
                    }
                });

                group.add(diagramButton);

                GridBagConstraints diagramButtonConstraint = new GridBagConstraints();
                diagramButtonConstraint.gridx = 0;
                diagramButtonConstraint.gridy = y;
                diagramButtonConstraint.anchor = GridBagConstraints.WEST;

                this.add(diagramButton, diagramButtonConstraint);

                // create the labels for the diagrams
                JLabel diagramLabel = new JLabel();
                diagramLabel.setText(diagram.getLabel());

                GridBagConstraints diagramLabelConstraint = new GridBagConstraints();
                diagramLabelConstraint.gridx = 1;
                diagramLabelConstraint.gridy = y;
                diagramLabelConstraint.anchor = GridBagConstraints.WEST;

                this.add(diagramLabel, diagramLabelConstraint);

                y++;

                Collection<ParameterVO> parameters = diagram.getParameters();
                for (final ParameterVO parameter : parameters) {

                    // create the checkbox for the parameters
                    JCheckBox parameterButton = new JCheckBox();

                    ButtonModel parameterButtonModel = new ParameterButtonModel(parameter);
                    parameterButtonModel.setSelected(parameter.isSelected());
                    parameterButton.setModel(parameterButtonModel);

                    parameterButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            newSwingWorker().execute();
                        }
                    });

                    GridBagConstraints parameterButtonConstraint = new GridBagConstraints();
                    parameterButtonConstraint.gridx = 1;
                    parameterButtonConstraint.gridy = y;
                    parameterButtonConstraint.anchor = GridBagConstraints.WEST;

                    this.add(parameterButton, parameterButtonConstraint);

                    // create the labels for the diagrams
                    JLabel parameterLabel = new JLabel();
                    parameterLabel.setText(parameter.getLabel());

                    GridBagConstraints parameterLabelConstraint = new GridBagConstraints();
                    parameterLabelConstraint.gridx = 2;
                    parameterLabelConstraint.gridy = y;
                    parameterLabelConstraint.anchor = GridBagConstraints.WEST;

                    this.add(parameterLabel, parameterLabelConstraint);

                    y++;
                }
            }
        }
    }

    @SuppressWarnings("serial")
    public class DiagramButtonModel extends JToggleButton.ToggleButtonModel {

        private DiagramVO diagram;

        public DiagramButtonModel(DiagramVO diagram) {
            this.diagram = diagram;
        }

        @Override
        public void setSelected(boolean b) {
            this.diagram.setSelected(b);
            super.setSelected(b);
        }
    }

    @SuppressWarnings("serial")
    public class ParameterButtonModel extends JToggleButton.ToggleButtonModel {

        private ParameterVO parameter;

        public ParameterButtonModel(ParameterVO parameter) {
            this.parameter = parameter;
        }

        @Override
        public void setSelected(boolean b) {
            this.parameter.setSelected(b);
            super.setSelected(b);
        }
    }

    public class JMXResult {

        private List<DiagramVO> diagrams;
        private DiagramVO diagram;
        private List<Object> events;

        public List<DiagramVO> getDiagrams() {
            return this.diagrams;
        }

        public void setDiagrams(List<DiagramVO> diagrams) {
            this.diagrams = diagrams;
        }

        public DiagramVO getDiagram() {
            return this.diagram;
        }

        public void setDiagram(DiagramVO diagram) {
            this.diagram = diagram;
        }

        public List<Object> getEvents() {
            return this.events;
        }

        public void setEvents(List<Object> events) {
            this.events = events;
        }
    }
}
