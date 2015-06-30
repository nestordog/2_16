package ch.algotrader.util;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.SecurityFamily;

public class RoundingTest {

    @Test
    public void testGetDigits() {

        assertEquals(3, RoundUtil.getDigits(new BigDecimal("0.001")));
        assertEquals(2, RoundUtil.getDigits(new BigDecimal("0.01")));
        assertEquals(1, RoundUtil.getDigits(new BigDecimal("0.1")));
        assertEquals(0, RoundUtil.getDigits(new BigDecimal("1.000")));
        assertEquals(0, RoundUtil.getDigits(new BigDecimal("1.00")));
        assertEquals(0, RoundUtil.getDigits(new BigDecimal("1.0")));
        assertEquals(0, RoundUtil.getDigits(new BigDecimal("1")));
        assertEquals(0, RoundUtil.getDigits(new BigDecimal("10")));
        assertEquals(1, RoundUtil.getDigits(new BigDecimal("10.1")));
        assertEquals(2, RoundUtil.getDigits(new BigDecimal("10.01")));
        assertEquals(2, RoundUtil.getDigits(new BigDecimal("10.25")));
        assertEquals(3, RoundUtil.getDigits(new BigDecimal("10.137")));
        assertEquals(4, RoundUtil.getDigits(new BigDecimal("10.8888")));
        assertEquals(5, RoundUtil.getDigits(new BigDecimal("10.11231")));
    }

    @Test
    public void testRoundPrice() {

        SecurityFamily securityFamily = SecurityFamily.Factory.newInstance();

        Future security = Future.Factory.newInstance();
        security.setSecurityFamily(securityFamily);

        securityFamily.setScale(0);
        securityFamily.setTickSizePattern("0<5");
        assertEquals(new BigDecimal("2055"), securityFamily.roundDown(null, new BigDecimal("2056.8")));
        assertEquals(new BigDecimal("2060"), securityFamily.roundUp(null, new BigDecimal("2056.8")));

        securityFamily.setScale(0);
        securityFamily.setTickSizePattern("0<1");
        assertEquals(new BigDecimal("2056"), securityFamily.roundDown(null, new BigDecimal("2056.8")));
        assertEquals(new BigDecimal("2057"), securityFamily.roundUp(null, new BigDecimal("2056.8")));

        securityFamily.setScale(1);
        securityFamily.setTickSizePattern("0<0.5");
        assertEquals(new BigDecimal("2056.5"), securityFamily.roundDown(null, new BigDecimal("2056.8")));
        assertEquals(new BigDecimal("2057.0"), securityFamily.roundUp(null, new BigDecimal("2056.8")));

        securityFamily.setScale(2);
        securityFamily.setTickSizePattern("0<0.25");
        assertEquals(new BigDecimal("2056.75"), securityFamily.roundDown(null, new BigDecimal("2056.8")));
        assertEquals(new BigDecimal("2057.00"), securityFamily.roundUp(null, new BigDecimal("2056.8")));
    }

}
