package com.algoTrader.starter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import com.algoTrader.ServiceLocator;

public class SimulationStarter {

    public static void main(String[] args) {

        List securities = new ArrayList();
        CollectionUtils.addAll(securities, ArrayUtils.subarray(args, 1, args.length -1));

        long startTime = Long.parseLong(args[0]);

        ServiceLocator.instance().getCepService().simulate(startTime, securities);
    }
}
