/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
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
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.adapter.ib;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.beanutils.ConvertUtilsBean;

import ch.algotrader.config.IBConfig;
import ch.algotrader.entity.trade.LimitOrderI;
import ch.algotrader.entity.trade.OrderProperty;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.StopOrderI;
import ch.algotrader.enumeration.OrderPropertyType;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.service.ib.IBNativeOrderServiceException;
import ch.algotrader.util.FieldUtil;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.TagValue;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultIBOrderMessageFactory implements IBOrderMessageFactory {

    private static DateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    private final IBConfig iBConfig;
    private final ConvertUtilsBean convertUtils;

    public DefaultIBOrderMessageFactory(IBConfig iBConfig) {
        this.iBConfig = iBConfig;
        this.convertUtils = new ConvertUtilsBean();
    }

    @Override
    public Order createOrderMessage(SimpleOrder order, Contract contract) {

        com.ib.client.Order ibOrder = new com.ib.client.Order();
        ibOrder.m_totalQuantity = (int) order.getQuantity();
        ibOrder.m_action = IBUtil.getIBSide(order.getSide());
        ibOrder.m_orderType = IBUtil.getIBOrderType(order);
        ibOrder.m_transmit = true;

        // handle a potentially defined account
        if (order.getAccount().getExtAccount() != null) {

            ibOrder.m_account = order.getAccount().getExtAccount();

            // handling for financial advisor account groups
        } else if (order.getAccount().getExtAccountGroup() != null) {

            ibOrder.m_faGroup = order.getAccount().getExtAccountGroup();
            ibOrder.m_faMethod = this.iBConfig.getFaMethod();

            //            long existingQuantity = 0;
            //            for (Position position : order.getSecurity().getPositions()) {
            //                existingQuantity += position.getQuantity();
            //            }
            //
            //            // evaluate weather the transaction is opening or closing
            //            boolean opening = false;
            //            if (existingQuantity > 0 && Side.SELL.equals(order.getSide())) {
            //                opening = false;
            //            } else if (existingQuantity <= 0 && Side.SELL.equals(order.getSide())) {
            //                opening = true;
            //            } else if (existingQuantity < 0 && Side.BUY.equals(order.getSide())) {
            //                opening = false;
            //            } else if (existingQuantity >= 0 && Side.BUY.equals(order.getSide())) {
            //                opening = true;
            //            }
            //
            //            ibOrder.m_faGroup = order.getAccount().getExtAccountGroup();
            //
            //            if (opening) {
            //
            //                // open by specifying the actual quantity
            //                ibOrder.m_faMethod = this.faOpenMethod;
            //
            //            } else {
            //
            //                // reduce by percentage
            //                ibOrder.m_faMethod = "PctChange";
            //                ibOrder.m_totalQuantity = 0; // bacause the order is percent based
            //                ibOrder.m_faPercentage = "-" + Math.abs(order.getQuantity() * 100 / (existingQuantity - order.getQuantity()));
            //            }

            // handling for financial advisor allocation profiles
        } else if (order.getAccount().getExtAllocationProfile() != null) {

            ibOrder.m_faProfile = order.getAccount().getExtAllocationProfile();
        }

        // add clearing information
        if (order.getAccount().getExtClearingAccount() != null) {
            ibOrder.m_clearingAccount = order.getAccount().getExtClearingAccount();
            ibOrder.m_clearingIntent = "Away";
        }

        //set the limit price if order is a limit order or stop limit order
        if (order instanceof LimitOrderI) {
            ibOrder.m_lmtPrice = ((LimitOrderI) order).getLimit().doubleValue();
        }

        //set the stop price if order is a stop order or stop limit order
        if (order instanceof StopOrderI) {
            ibOrder.m_auxPrice = ((StopOrderI) order).getStop().doubleValue();
        }

        // set Time-In-Force (ATC are set as order types LOC and MOC)
        if (order.getTif() != null && !TIF.ATC.equals(order.getTif())) {
            ibOrder.m_tif = order.getTif().name();

            // set the TIF-Date
            if (order.getTifDateTime() != null) {
                ibOrder.m_goodTillDate = format.format(order.getTifDateTime());
            }
        }

        // separate properties that correspond to IB Order fields from the rest
        Map<String, OrderProperty> propertiesMap = new HashMap<String, OrderProperty>(order.getOrderProperties());
        for (Field field : FieldUtil.getAllFields(ibOrder.getClass())) {
            String name = field.getName().substring(2);
            OrderProperty orderProperty = propertiesMap.get(name);
            if (orderProperty != null && OrderPropertyType.IB.equals(orderProperty.getType())) {
                try {
                    Object value = this.convertUtils.convert(orderProperty.getValue(), field.getType());
                    field.set(ibOrder, value);
                } catch (IllegalAccessException e) {
                    throw new IBNativeOrderServiceException(e.getMessage(), e);
                }
                propertiesMap.remove(name);
            }
        }

        // add remaining params as AlgoParams
        Vector<TagValue> params = new Vector<TagValue>();
        for (OrderProperty orderProperty : propertiesMap.values()) {
            if (OrderPropertyType.IB.equals(orderProperty.getType())) {
                params.add(new TagValue(orderProperty.getName(), orderProperty.getValue()));
            }
        }

        if (params.size() > 0) {
            ibOrder.m_algoParams = params;
        }

        return ibOrder;
    }

}
