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
package ch.algotrader.entity.trade.algo;

import java.time.LocalTime;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import ch.algotrader.entity.trade.Fill;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class VWAPOrderStateVO extends AlgoOrderStateVO {

    private static final long serialVersionUID = 1L;

    private final double participation;

    private final TreeMap<LocalTime, Long> buckets;

    private final List<Fill> fills = new CopyOnWriteArrayList<>();

    public VWAPOrderStateVO(double participation, TreeMap<LocalTime, Long> buckets) {
        this.participation = participation;
        this.buckets = buckets;
    }

    public double getParticipation() {
        return this.participation;
    }

    public long getBucketVolume(LocalTime time) {
        return this.buckets.floorEntry(time).getValue();
    }

    public void storeFill(Fill fill) {
        this.fills.add(fill);
    }

    public List<Fill> getFills() {
        return this.fills;
    }

    @Override
    public String toString() {
        return "participationRate=" + this.participation;
    }

}
