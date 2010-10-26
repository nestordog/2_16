package com.algoTrader.sabr;

import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

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

    public SABRCalibrationParams calibrate(Double[] strikes, Double[] volatilities, double ATMvol, double forward, double years, double beta) throws MWException {

        Object[] input = new Object[6];

        input[0] = strikes;
        input[1] = volatilities;
        input[2] = ATMvol;
        input[3] = forward;
        input[4] = years;
        input[5] = beta;

        Object[] y = this.sabr.calibration(3, input);

        SABRCalibrationParams params = new SABRCalibrationParams();

        params.setR(((MWNumericArray) y[0]).getDouble());
        params.setV(((MWNumericArray) y[1]).getDouble());
        params.setA(((MWNumericArray) y[2]).getDouble());

        return params;
    }

    public double findAlpha(double forward, double strike, double atmVola, double years, double b, double r, double v) throws MWException {

        Object[] input = new Object[7];

        input[0] = forward;
        input[1] = strike;
        input[2] = years;
        input[3] = atmVola;
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
