/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 */
public class CsvExporter {

    public void exportTable(JTable table, File file) {

        try {
            TableModel model = table.getModel();
            FileWriter out = new FileWriter(file);
            for (int i = 0; i < model.getColumnCount(); i++) {
                out.write(model.getColumnName(i) + ";");
            }
            out.write("\n");

            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    out.write(model.getValueAt(i, j).toString() + ";");
                }
                out.write("\n");
            }

            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
