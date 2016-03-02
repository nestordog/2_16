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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.algotrader.entity.marketData.TickI;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.util.collection.Pair;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class SlicingOrderStateVO extends AlgoOrderStateVO {

    private static final long serialVersionUID = 1L;

    private int currentOffsetTicks = 1;

    private final List<Pair<LimitOrder, TickI>> pairs = new ArrayList<>();

    private final List<Fill> fills = new ArrayList<>();

    private final Map<String, List<Fill>> fillsByIntId = new HashMap<>();

    public int getCurrentOffsetTicks() {
        return this.currentOffsetTicks;
    }

    public void setCurrentOffsetTicks(int currentOffsetTicks) {
        this.currentOffsetTicks = currentOffsetTicks;
    }

    public void addPair(Pair<LimitOrder, TickI> pair) {
        this.pairs.add(pair);
    }

    public List<Pair<LimitOrder, TickI>> getPairs() {
        return this.pairs;
    }

    public void storeFill(Fill fill) {

        String intId = fill.getOrder().getIntId();
        List<Fill> fills = this.fillsByIntId.get(intId);
        if (fills == null) {
            fills = new ArrayList<Fill>();
            this.fillsByIntId.put(intId, fills);
        }
        fills.add(fill);
        this.fills.add(fill);
    }

    public List<Fill> getFills() {
        return this.fills;
    }

    public List<Fill> getFillsByIntOrderId(String intId) {
        return this.fillsByIntId.get(intId);
    }

    @Override
    public String toString() {
        return "currentOffsetTicks=" + this.currentOffsetTicks;
    }
}
