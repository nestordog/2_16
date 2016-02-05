package ch.algotrader.esper;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

public class ExponentialMovingAverageEsperTest {

    private EPServiceProvider epService;
    private EPRuntime epRuntime;

    @Before
    public void setupEsper() throws Exception {

        Configuration config = new Configuration();
        config.configure("/META-INF/esper-common.cfg.xml");
        config.addEventType(A.class);

        this.epService = EPServiceProviderManager.getDefaultProvider(config);
        this.epRuntime = this.epService.getEPRuntime();
    }

    @After
    public void cleanUpEsper() {
        if (this.epService != null) {
            this.epService.destroy();
        }
    }

    @Test
    public void testEMA() {

        final Queue<Double> emaQueue = new ConcurrentLinkedQueue<>();
        EPStatement stmt1 = this.epService.getEPAdministrator().createEPL("select ema(value, 10) from A");

        stmt1.setSubscriber(new Object() {
            public void update(Double value) {
                emaQueue.add(value);
            }
        });

        Assert.assertNull(emaQueue.poll());

        this.epRuntime.sendEvent(new A(27.620001));
        this.epRuntime.sendEvent(new A(27.25));
        this.epRuntime.sendEvent(new A(26.74));
        this.epRuntime.sendEvent(new A(26.690001));
        this.epRuntime.sendEvent(new A(26.549999));
        this.epRuntime.sendEvent(new A(26.700001));
        this.epRuntime.sendEvent(new A(26.459999));
        this.epRuntime.sendEvent(new A(26.83));
        this.epRuntime.sendEvent(new A(26.889999));
        this.epRuntime.sendEvent(new A(27.209999));
        this.epRuntime.sendEvent(new A(27.040001));
        this.epRuntime.sendEvent(new A(27.25));
        this.epRuntime.sendEvent(new A(27.25));
        this.epRuntime.sendEvent(new A(27.15));
        this.epRuntime.sendEvent(new A(27.610001));
        this.epRuntime.sendEvent(new A(27.629999));
        this.epRuntime.sendEvent(new A(27.879999));
        this.epRuntime.sendEvent(new A(27.91));
        this.epRuntime.sendEvent(new A(28.01));
        this.epRuntime.sendEvent(new A(27.85));
        this.epRuntime.sendEvent(new A(27.450001));
        this.epRuntime.sendEvent(new A(27.93));
        this.epRuntime.sendEvent(new A(27.440001));
        this.epRuntime.sendEvent(new A(27.5));
        this.epRuntime.sendEvent(new A(27.34));
        this.epRuntime.sendEvent(new A(27.280001));
        this.epRuntime.sendEvent(new A(27.549999));
        this.epRuntime.sendEvent(new A(27.860001));
        this.epRuntime.sendEvent(new A(27.879999));

        Assert.assertEquals(27.620001, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.4350005, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.2033336666667, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.0750005, emaQueue.poll(), 0.000001);
        Assert.assertEquals(26.9700002, emaQueue.poll(), 0.000001);
        Assert.assertEquals(26.9250003333333, emaQueue.poll(), 0.000001);
        Assert.assertEquals(26.8585715714286, emaQueue.poll(), 0.000001);
        Assert.assertEquals(26.855000125, emaQueue.poll(), 0.000001);
        Assert.assertEquals(26.8588888888889, emaQueue.poll(), 0.000001);
        Assert.assertEquals(26.8939999, emaQueue.poll(), 0.000001);
        Assert.assertEquals(26.9205455545455, emaQueue.poll(), 0.000001);
        Assert.assertEquals(26.9804463628099, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.0294561150263, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.0513731850215, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.152941878654, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.2396795370805, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.3561012576113, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.4568101198638, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.5573900980704, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.6105918984212, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.5813935532537, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.6447765435712, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.6075446265583, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.5879910580931, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.5429017748035, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.4951016339301, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.5050829732155, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.56961352354, emaQueue.poll(), 0.000001);
        Assert.assertEquals(27.6260472465327, emaQueue.poll(), 0.000001);

        Assert.assertNull(emaQueue.poll());

    }

    public static class A {

        private final double value;

        public A(double value) {
            super();
            this.value = value;
        }

        public double getValue() {
            return this.value;
        }
    }
}
