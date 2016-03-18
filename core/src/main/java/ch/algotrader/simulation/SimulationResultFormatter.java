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
package ch.algotrader.simulation;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.vo.performance.MaxDrawDownVO;
import ch.algotrader.vo.performance.PerformanceKeysVO;
import ch.algotrader.vo.performance.PeriodPerformanceVO;
import ch.algotrader.vo.performance.SimulationResultVO;
import ch.algotrader.vo.performance.TradesVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public final class SimulationResultFormatter {

    private static final DecimalFormat twoDigitFormat = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter monthFormat = DateTimeFormatter.ofPattern(" MMM-yy ", Locale.ROOT);
    private static final DateTimeFormatter yearFormat = DateTimeFormatter.ofPattern("   yyyy ", Locale.ROOT);

    public void formatShort(final Appendable buffer, final SimulationResultVO resultVO) throws IOException {

        PerformanceKeysVO performanceKeys = resultVO.getPerformanceKeys();
        MaxDrawDownVO maxDrawDownVO = resultVO.getMaxDrawDown();

        if (resultVO.getAllTrades().getCount() == 0) {
            buffer.append("no trades took place!");
            return;
        }

        Collection<PeriodPerformanceVO> periodPerformanceVOs = resultVO.getMonthlyPerformances();
        double maxDrawDownM = 0d;
        double bestMonthlyPerformance = Double.NEGATIVE_INFINITY;
        if ((periodPerformanceVOs != null)) {
            for (PeriodPerformanceVO PeriodPerformanceVO : periodPerformanceVOs) {
                maxDrawDownM = Math.min(maxDrawDownM, PeriodPerformanceVO.getValue());
                bestMonthlyPerformance = Math.max(bestMonthlyPerformance, PeriodPerformanceVO.getValue());
            }
        }

        if (performanceKeys != null) {
            buffer.append(" avgY=" + twoDigitFormat.format(performanceKeys.getAvgY() * 100.0) + "%");
            buffer.append(" stdY=" + twoDigitFormat.format(performanceKeys.getStdY() * 100) + "%");
            buffer.append(" sharpe=" + twoDigitFormat.format(performanceKeys.getSharpeRatio()));
        }
        buffer.append(" maxDDM=" + twoDigitFormat.format(-maxDrawDownM * 100) + "%");
        buffer.append(" bestMP=" + twoDigitFormat.format(bestMonthlyPerformance * 100) + "%");
        buffer.append(" maxDD=" + twoDigitFormat.format(maxDrawDownVO.getAmount() * 100.0) + "%");
        buffer.append(" maxDDPer=" + twoDigitFormat.format(maxDrawDownVO.getPeriod() / 86400000));
        buffer.append(" winTrds=" + resultVO.getWinningTrades().getCount());
        buffer.append(" winTrdsPct=" + twoDigitFormat.format(100.0 * resultVO.getWinningTrades().getCount() / resultVO.getAllTrades().getCount()) + "%");
        buffer.append(" avgPPctWin=" + twoDigitFormat.format(resultVO.getWinningTrades().getAvgProfitPct() * 100.0) + "%");
        buffer.append(" loosTrds=" + resultVO.getLoosingTrades().getCount());
        buffer.append(" loosTrdsPct=" + twoDigitFormat.format(100.0 * resultVO.getLoosingTrades().getCount() / resultVO.getAllTrades().getCount()) + "%");
        buffer.append(" avgPPctLoos=" + twoDigitFormat.format(resultVO.getLoosingTrades().getAvgProfitPct() * 100.0) + "%");
        buffer.append(" totalTrds=" + resultVO.getAllTrades().getCount());

        for (Map.Entry<String, Object> entry : resultVO.getStrategyResults().entrySet()) {
            buffer.append(" " + entry.getKey() + "=" + entry.getValue());
        }

    }

    public void formatLong(final Appendable buffer, final SimulationResultVO resultVO, final CommonConfig commonConfig) throws IOException {

        buffer.append("execution time (min): " + (new DecimalFormat("0.00")).format(resultVO.getMins()) + "\r\n");

        if (resultVO.getAllTrades().getCount() == 0) {
            buffer.append("no trades took place! \r\n");
            return;
        }

        buffer.append("dataSet: " + commonConfig.getDataSet() + "\r\n");

        double netLiqValue = resultVO.getNetLiqValue();
        buffer.append("netLiqValue=" + twoDigitFormat.format(netLiqValue) + "\r\n");

        // monthlyPerformances
        Collection<PeriodPerformanceVO> monthlyPerformances = resultVO.getMonthlyPerformances();
        double maxDrawDownM = 0d;
        double bestMonthlyPerformance = Double.NEGATIVE_INFINITY;
        int positiveMonths = 0;
        int negativeMonths = 0;
        if ((monthlyPerformances != null)) {
            StringBuilder dateBuffer = new StringBuilder("month-year:         ");
            StringBuilder performanceBuffer = new StringBuilder("monthlyPerformance: ");
            for (PeriodPerformanceVO monthlyPerformance : monthlyPerformances) {
                maxDrawDownM = Math.min(maxDrawDownM, monthlyPerformance.getValue());
                bestMonthlyPerformance = Math.max(bestMonthlyPerformance, monthlyPerformance.getValue());
                monthFormat.formatTo(DateTimeLegacy.toGMTDate(monthlyPerformance.getDate()), dateBuffer);
                performanceBuffer.append(StringUtils.leftPad(twoDigitFormat.format(monthlyPerformance.getValue() * 100), 6) + "% ");
                if (monthlyPerformance.getValue() > 0) {
                    positiveMonths++;
                } else {
                    negativeMonths++;
                }
            }
            buffer.append(dateBuffer.toString() + "\r\n");
            buffer.append(performanceBuffer.toString() + "\r\n");
        }

        // yearlyPerformances
        int positiveYears = 0;
        int negativeYears = 0;
        Collection<PeriodPerformanceVO> yearlyPerformances = resultVO.getYearlyPerformances();
        if ((yearlyPerformances != null)) {
            StringBuilder dateBuffer = new StringBuilder("year:               ");
            StringBuilder performanceBuffer = new StringBuilder("yearlyPerformance:  ");
            for (PeriodPerformanceVO yearlyPerformance : yearlyPerformances) {
                yearFormat.formatTo(DateTimeLegacy.toGMTDate(yearlyPerformance.getDate()), dateBuffer);
                performanceBuffer.append(StringUtils.leftPad(twoDigitFormat.format(yearlyPerformance.getValue() * 100), 6) + "% ");
                if (yearlyPerformance.getValue() > 0) {
                    positiveYears++;
                } else {
                    negativeYears++;
                }
            }
            buffer.append(dateBuffer.toString() + "\r\n");
            buffer.append(performanceBuffer.toString() + "\r\n");
        }

        if ((monthlyPerformances != null)) {
            buffer.append("posMonths=" + positiveMonths + " negMonths=" + negativeMonths);
            if ((yearlyPerformances != null)) {
                buffer.append(" posYears=" + positiveYears + " negYears=" + negativeYears);
            }
            buffer.append("\r\n");
        }

        PerformanceKeysVO performanceKeys = resultVO.getPerformanceKeys();
        MaxDrawDownVO maxDrawDownVO = resultVO.getMaxDrawDown();
        if (performanceKeys != null && maxDrawDownVO != null) {
            buffer.append("avgM=" + twoDigitFormat.format(performanceKeys.getAvgM() * 100) + "%");
            buffer.append(" stdM=" + twoDigitFormat.format(performanceKeys.getStdM() * 100) + "%");
            buffer.append(" avgY=" + twoDigitFormat.format(performanceKeys.getAvgY() * 100) + "%");
            buffer.append(" stdY=" + twoDigitFormat.format(performanceKeys.getStdY() * 100) + "% ");
            buffer.append(" sharpeRatio=" + twoDigitFormat.format(performanceKeys.getSharpeRatio()) + "\r\n");

            buffer.append("maxMonthlyDrawDown=" + twoDigitFormat.format(-maxDrawDownM * 100) + "%");
            buffer.append(" bestMonthlyPerformance=" + twoDigitFormat.format(bestMonthlyPerformance * 100) + "%");
            buffer.append(" maxDrawDown=" + twoDigitFormat.format(maxDrawDownVO.getAmount() * 100) + "%");
            buffer.append(" maxDrawDownPeriod=" + twoDigitFormat.format(maxDrawDownVO.getPeriod() / 86400000) + "days");
            buffer.append(" colmarRatio=" + twoDigitFormat.format(performanceKeys.getAvgY() / maxDrawDownVO.getAmount()));

            buffer.append("\r\n");
        }

        buffer.append("WinningTrades:");
        printTrades(buffer, resultVO.getWinningTrades(), resultVO.getAllTrades().getCount());

        buffer.append("LoosingTrades:");
        printTrades(buffer, resultVO.getLoosingTrades(), resultVO.getAllTrades().getCount());

        buffer.append("AllTrades:");
        printTrades(buffer, resultVO.getAllTrades(), resultVO.getAllTrades().getCount());

        for (Map.Entry<String, Object> entry : resultVO.getStrategyResults().entrySet()) {
            buffer.append(entry.getKey() + "=" + entry.getValue() + " ");
        }
    }

    private void printTrades(final Appendable buffer, final TradesVO tradesVO, final long totalTrades) throws IOException {

        buffer.append(" count=" + tradesVO.getCount());
        if (tradesVO.getCount() != totalTrades) {
            buffer.append("(" + twoDigitFormat.format(100.0 * tradesVO.getCount() / totalTrades) + "%)");
        }
        buffer.append(" totalProfit=" + twoDigitFormat.format(tradesVO.getTotalProfit()));
        buffer.append(" avgProfit=" + twoDigitFormat.format(tradesVO.getAvgProfit()));
        buffer.append(" avgProfitPct=" + twoDigitFormat.format(tradesVO.getAvgProfitPct() * 100) + "%");
        buffer.append("\r\n");
    }


}
