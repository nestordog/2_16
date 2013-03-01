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
package com.algoTrader.esper.aggregation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.buffer.CircularFifoBuffer;
import org.apache.commons.lang.ArrayUtils;

import com.espertech.esper.epl.agg.aggregator.AggregationMethod;
import com.tictactec.ta.lib.CoreAnnotated;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

/**
 *
 * Generic AggregateFunction to support all TA-Lib operations
 * <p/>
 * To use the AggregateFunction add the following to the esper configuration
 *
 * <pre>
 * &lt;plugin-aggregation-function name="talib" factory-class="com.algoTrader.util.GenericTALibFunctionFactory"/&gt;
 * </pre>
 *
 * The AggregationFunction can be used in an esper statement like this:
 * <pre>
 * insert into StochF
 * select talib("stochF", high, low, close, 3, 2, "Sma") as values
 * from OHLCBar;
 *
 * select values.fastk, values.fastd
 * from StochF(values != null);
 * </pre>
 * The following parameters from the com.tictactec.ta.lib.Core methods will be needed:
 * <ul>
 * <li>in...(i.e. inHigh, inLow, inClose)</li>
 * <li>optIn..(i.e. optInFastK_Period, optInFastD_Period, optInFastD_MAType)</li>
 * <li>startIdx, endIdx, outBegIdx & outNBElement can be ignored</li>
 * </ul>
 * If the TA-Lib Function returns just one value, the value is directly exposed by the AggregationFunction.
 * </p>
 * If the TA-Lib Function returns multiple-values, a dynamic class will be generated on the fly, which gives
 * access to properly typed return-values. all return value names are lower-case!
 * </p>
 * Example: the TA-Lib function stochF has return values: outFastK and outFastD. The returned dynamic class
 * will have double typed properties by the name of: fastk and fastd (all lowercase)
 * </p>
 * The AggregationFunction needs the following libraries: </p>
 * <ul>
 * <li><a href="http://commons.apache.org/lang/">Apache Commons Lang</a></li>
 * <li><a href="http://larvalabs.com/collections/">Commons Generics</a></li>
 * <li><a href="http://ta-lib.org/">TA-Lib</a></li> </p>
 * <li><a href="http://www.javassist.org/">Javaassist</a></li> </p>
 * </ul>
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class GenericTALibFunction implements AggregationMethod {

    private static CoreAnnotated core = new CoreAnnotated();

    private Method function;
    private Class<?> outputClass;

    private List<CircularFifoBuffer<Number>> inputParams;
    private List<Object> optInputParams;
    private Map<String, Object> outputParams;

    public GenericTALibFunction(Method function, int inputParamCount, int lookbackPeriod, List<Object> optInputParams, Map<String, Object> outputParams, Class<?> outputClass) {

        super();

        this.function = function;
        this.outputClass = outputClass;

        this.optInputParams = optInputParams;
        this.outputParams = outputParams;

        this.inputParams = new ArrayList<CircularFifoBuffer<Number>>();

        for (int i = 0; i < inputParamCount; i++) {
            this.inputParams.add(new CircularFifoBuffer<Number>(lookbackPeriod));
        }
    }

    @Override
    public void enter(Object obj) {

        Object[] params = (Object[]) obj;

        // add all inputs to the correct buffers
        int paramCount = 1;
        for (CircularFifoBuffer<Number> buffer : this.inputParams) {
            Number value = (Number) params[paramCount];
            buffer.add(value);
            paramCount++;
        }
    }

    @Override
    public void leave(Object obj) {

        // Remove the last element of each buffer
        for (CircularFifoBuffer<Number> buffer : this.inputParams) {
            if (buffer.contains(obj)) {
                buffer.remove(obj);
            }
        }
    }

    @Override
    public Class<?> getValueType() {

        // if we only have one outPutParam return that value
        // otherwise return the dynamically generated class
        if (this.outputParams.size() == 1) {
            Class<?> clazz = this.outputParams.values().iterator().next().getClass();
            if (clazz.isArray()) {
                return clazz.getComponentType();
            } else {
                return clazz;
            }
        } else {
            return this.outputClass;
        }
    }

    @Override
    public Object getValue() {

        try {
            // get the total number of parameters
            int numberOfArgs = 2 + this.inputParams.size() + this.optInputParams.size() + 2 + this.outputParams.size();
            Object[] args = new Object[numberOfArgs];

            // get the size of the first input buffer
            int elements = this.inputParams.iterator().next().size();

            args[0] = elements - 1; // startIdx
            args[1] = elements - 1; // endIdx

            // inputParams
            int argCount = 2;
            for (CircularFifoBuffer<Number> buffer : this.inputParams) {

                // look at the first element of the buffer to determine the type
                Object firstElement = buffer.iterator().next();
                if (firstElement instanceof Double) {
                    args[argCount] = ArrayUtils.toPrimitive(buffer.toArray(new Double[0]));
                } else if (firstElement instanceof Integer) {
                    args[argCount] = ArrayUtils.toPrimitive(buffer.toArray(new Integer[0]));
                } else {
                    throw new IllegalArgumentException("unsupported type " + firstElement.getClass());
                }
                argCount++;
            }

            // optInputParams
            for (Object object : this.optInputParams) {
                args[argCount] = object;
                argCount++;
            }

            // begin
            MInteger begin = new MInteger();
            args[argCount] = begin;
            argCount++;

            // length
            MInteger length = new MInteger();
            args[argCount] = length;
            argCount++;

            // OutputParams
            for (Map.Entry<String, Object> entry : this.outputParams.entrySet()) {
                args[argCount++] = entry.getValue();
            }

            // invoke the function
            RetCode retCode = (RetCode) this.function.invoke(core, args);

            if (retCode == RetCode.Success) {
                if (length.value == 0) {
                    return null;
                }

                // if we only have one outPutParam return that value
                // otherwise return a Map
                if (this.outputParams.size() == 1) {
                    Object value = this.outputParams.values().iterator().next();
                    return getNumberFromNumberArray(value);
                } else {
                    Object returnObject = this.outputClass.newInstance();
                    for (Map.Entry<String, Object> entry : this.outputParams.entrySet()) {
                        Number value = getNumberFromNumberArray(entry.getValue());
                        String name = entry.getKey().toLowerCase().substring(3);

                        Field field = this.outputClass.getField(name);
                        field.set(returnObject, value);
                    }
                    return returnObject;
                }
            } else {
                throw new RuntimeException(retCode.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear() {

        // clear all elements from the buffers
        for (CircularFifoBuffer<Number> buffer : this.inputParams) {
            buffer.clear();
        }
    }

    private Number getNumberFromNumberArray(Object value) {

        if (value instanceof double[]) {
            return ((double[]) value)[0];
        } else if (value instanceof int[]) {
            return ((int[]) value)[0];
        } else {
            throw new IllegalArgumentException(value.getClass() + " not supported");
        }
    }
}
