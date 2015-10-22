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
package ch.algotrader.util.mail;

/**
 * POJO representing an Email Fragment with its binary data and {@code fileName}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class EmailFragment {

    private final byte[] data;
    private final String filename;

    public EmailFragment(String filename, byte[] data) {
        this.filename = filename;
        this.data = data;
    }

    public byte[] getData() {
        return this.data;
    }

    public String getFilename() {
        return this.filename;
    }

    @Override
    public String toString() {
        return this.filename;
    }
}
