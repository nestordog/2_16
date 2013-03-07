/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.sabr;

import com.algoTrader.vo.SABRSmileVO;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SABRCalibration {

    private SABR sabr;
    private static SABRCalibration instance;

    public static SABRCalibration getInstance() throws MWException {

        if (instance == null) {
            instance = new SABRCalibration();
        }
        return instance;
    }

    public SABRCalibration() {

        try {
            this.sabr = new SABR();
        } catch (MWException e) {
            e.printStackTrace();
        }
    }

    public SABRSmileVO calibrate(Double[] strikes, Double[] volatilities, double atmVol, double forward, double years, double beta) throws MWException {

        Object[] input = new Object[6];

        input[0] = strikes;
        input[1] = volatilities;
        input[2] = atmVol;
        input[3] = forward;
        input[4] = years;
        input[5] = beta;

        Object[] y = this.sabr.calibration(3, input);

        SABRSmileVO params = new SABRSmileVO();

        params.setYears(years);
        params.setRho(((MWNumericArray) y[0]).getDouble());
        params.setVolVol(((MWNumericArray) y[1]).getDouble());
        params.setAlpha(((MWNumericArray) y[2]).getDouble());
        params.setAtmVol(atmVol);

        return params;
    }

    public double findAlpha(double forward, double strike, double atmVol, double years, double b, double r, double v) throws MWException {

        Object[] input = new Object[7];

        input[0] = forward;
        input[1] = strike;
        input[2] = years;
        input[3] = atmVol;
        input[4] = b;
        input[5] = r;
        input[6] = v;

        Object[] y = this.sabr.findAlpha(1, input);

        return ((MWNumericArray) y[0]).getDouble();
    }

    public double vol(double forward, double strike, double years, double a, double b, double r, double v) throws MWException {

        Object[] input2 = new Object[7];

        input2[0] = a;
        input2[1] = b;
        input2[2] = r;
        input2[3] = v;
        input2[4] = forward;
        input2[5] = strike;
        input2[6] = years;

        Object[] z = this.sabr.vol(1, input2);

        return ((MWNumericArray) z[0]).getDouble();
    }

    public double volByAtmVol(double forward, double strike, double atmVol, double years, double b, double r, double v) throws MWException {

        double alpha = findAlpha(forward, strike, atmVol, years, b, r, v);
        double result = vol(forward, strike, years, alpha, b, r, v);
        return result;
    }

    public void dispose() {
        this.sabr.dispose();
    }
}
