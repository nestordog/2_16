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
package ch.algotrader.esper.aggregation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import org.apache.commons.collections15.buffer.CircularFifoBuffer;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import com.espertech.esper.client.hook.AggregationFunctionFactory;
import com.espertech.esper.epl.agg.aggregator.AggregationMethod;
import com.espertech.esper.epl.agg.service.AggregationValidationContext;
import com.espertech.esper.epl.expression.core.ExprEvaluator;
import com.tictactec.ta.lib.CoreAnnotated;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.meta.annotation.InputParameterInfo;
import com.tictactec.ta.lib.meta.annotation.InputParameterType;
import com.tictactec.ta.lib.meta.annotation.OptInputParameterInfo;
import com.tictactec.ta.lib.meta.annotation.OptInputParameterType;
import com.tictactec.ta.lib.meta.annotation.OutputParameterInfo;
import com.tictactec.ta.lib.meta.annotation.OutputParameterType;

/**
 * Factory class needed for {@link GenericTALibFunction}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class GenericTALibFunctionFactory implements AggregationFunctionFactory {

    private static CoreAnnotated core = new CoreAnnotated();

    private Method function;
    private Class<?> outputClass;

    private int inputParamCount = 0;
    private int lookbackPeriod;

    private List<CircularFifoBuffer<Number>> inputParams = new ArrayList<CircularFifoBuffer<Number>>();
    private List<Object> optInputParams = new ArrayList<Object>();
    private Map<String, Object> outputParams = new LinkedHashMap<String, Object>();

    @Override
    public void setFunctionName(String functionName) {
        // do nothing
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
    public void validate(AggregationValidationContext validationContext) {

        Class<?>[] paramTypes = validationContext.getParameterTypes();

        // get the functionname
        String talibFunctionName = (String) getConstant(validationContext, 0, String.class);

        // get the method by iterating over all core-methods
        // we have to do it this way, since we don't have the exact parameters
        for (Method method : core.getClass().getDeclaredMethods()) {
            if (method.getName().equals(talibFunctionName)) {
                this.function = method;
                break;
            }
        }

        // check that we have a function now
        if (this.function == null) {
            throw new IllegalArgumentException("function " + talibFunctionName + " was not found");
        }

        // get the parameters
        int paramCounter = 1;
        Map<String, Class<?>> outputParamTypes = new HashMap<String, Class<?>>();
        for (Annotation[] annotations : this.function.getParameterAnnotations()) {
            for (Annotation annotation : annotations) {

                // got through all inputParameters and count them
                if (annotation instanceof InputParameterInfo) {
                    InputParameterInfo inputParameterInfo = (InputParameterInfo) annotation;
                    if (inputParameterInfo.type().equals(InputParameterType.TA_Input_Real)) {
                        if (paramTypes[paramCounter].equals(double.class) || paramTypes[paramCounter].equals(Double.class)) {
                            this.inputParamCount++;
                            paramCounter++;
                        } else {
                            throw new IllegalArgumentException("param number " + paramCounter + " needs must be of type double");
                        }
                    } else if (inputParameterInfo.type().equals(InputParameterType.TA_Input_Integer)) {
                        if (paramTypes[paramCounter].equals(int.class) || paramTypes[paramCounter].equals(Integer.class)) {
                            this.inputParamCount++;
                            paramCounter++;
                        } else {
                            throw new IllegalArgumentException("param number " + paramCounter + " needs must be of type int");
                        }
                    } else if (inputParameterInfo.type().equals(InputParameterType.TA_Input_Price)) {

                        // the flags define the number of parameters in use by a bitwise or
                        int priceParamSize = numberOfSetBits(inputParameterInfo.flags());
                        for (int i = 0; i < priceParamSize; i++) {
                            if (paramTypes[paramCounter].equals(double.class) || paramTypes[paramCounter].equals(Double.class)) {
                                this.inputParamCount++;
                                paramCounter++;
                            } else {
                                throw new IllegalArgumentException("param number " + paramCounter + " needs must be of type double");
                            }
                        }
                    }

                    // got through all optInputParameters and store them for later
                } else if (annotation instanceof OptInputParameterInfo) {
                    OptInputParameterInfo optInputParameterInfo = (OptInputParameterInfo) annotation;
                    if (optInputParameterInfo.type().equals(OptInputParameterType.TA_OptInput_IntegerRange)) {
                        this.optInputParams.add(getConstant(validationContext, paramCounter, Integer.class));
                    } else if (optInputParameterInfo.type().equals(OptInputParameterType.TA_OptInput_RealRange)) {
                        this.optInputParams.add(getConstant(validationContext, paramCounter, Double.class));
                    } else if (optInputParameterInfo.type().equals(OptInputParameterType.TA_OptInput_IntegerList)) {
                        String value = (String) getConstant(validationContext, paramCounter, String.class);
                        MAType type = MAType.valueOf(value);
                        this.optInputParams.add(type);
                    }
                    paramCounter++;

                    // to through all outputParameters and store them
                } else if (annotation instanceof OutputParameterInfo) {
                    OutputParameterInfo outputParameterInfo = (OutputParameterInfo) annotation;
                    String paramName = outputParameterInfo.paramName();
                    if (outputParameterInfo.type().equals(OutputParameterType.TA_Output_Real)) {
                        this.outputParams.put(paramName, new double[1]);
                        outputParamTypes.put(paramName.toLowerCase().substring(3), double.class);
                    } else if (outputParameterInfo.type().equals(OutputParameterType.TA_Output_Integer)) {
                        this.outputParams.put(outputParameterInfo.paramName(), new int[1]);
                        outputParamTypes.put(paramName.toLowerCase().substring(3), int.class);
                    }
                }
            }
        }

        try {

            // get the dynamically created output class
            if (this.outputParams.size() > 1) {
                String className = StringUtils.capitalize(talibFunctionName);
                this.outputClass = getReturnClass(className, outputParamTypes);
            }

            // get the lookback size
            Object[] args = new Object[this.optInputParams.size()];
            Class<?>[] argTypes = new Class[this.optInputParams.size()];

            // supply all optInputParams
            int argCount = 0;
            for (Object object : this.optInputParams) {
                args[argCount] = object;
                Class<?> clazz = object.getClass();
                Class<?> primitiveClass = ClassUtils.wrapperToPrimitive(clazz);
                if (primitiveClass != null) {
                    argTypes[argCount] = primitiveClass;
                } else {
                    argTypes[argCount] = clazz;
                }
                argCount++;
            }

            // get and invoke the lookback method
            Method lookback = core.getClass().getMethod(talibFunctionName + "Lookback", argTypes);
            this.lookbackPeriod = (Integer) lookback.invoke(core, args) + 1;

            // create the fixed size Buffers
            for (int i = 0; i < this.inputParamCount; i++) {
                this.inputParams.add(new CircularFifoBuffer<Number>(this.lookbackPeriod));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AggregationMethod newAggregator() {

        return new GenericTALibFunction(this.function, this.inputParamCount, this.lookbackPeriod, this.optInputParams, this.outputParams, this.outputClass);
    }

    private Object getConstant(AggregationValidationContext validationContext, int index, Class<?> clazz) {

        if (index >= validationContext.getIsConstantValue().length) {
            throw new IllegalArgumentException("only " + validationContext.getIsConstantValue().length + " params have been specified, should be "
                    + (index + 1));
        }

        if (validationContext.getIsConstantValue()[index]) {
            if (validationContext.getParameterTypes()[index].equals(clazz)) {
                return validationContext.getConstantValues()[index];
            } else {
                throw new IllegalArgumentException("param " + index + " has to be a constant of type " + clazz);
            }
        } else {
            ExprEvaluator evaluator = (ExprEvaluator) validationContext.getExpressions()[index];
            Object obj = evaluator.evaluate(null, true, null);
            if (obj.getClass().equals(clazz)) {
                return obj;
            } else {
                throw new IllegalArgumentException("param " + index + " has to be a constant of type " + clazz);
            }
        }
    }

    private int numberOfSetBits(int i) {
        i = i - ((i >> 1) & 0x55555555);
        i = (i & 0x33333333) + ((i >> 2) & 0x33333333);
        return ((i + (i >> 4) & 0xF0F0F0F) * 0x1010101) >> 24;
    }

    private Class<?> getReturnClass(String className, Map<String, Class<?>> fields) throws CannotCompileException, NotFoundException {

        String fqClassName = this.getClass().getPackage().getName() + "." + className;

        try {
            // see if the class already exists
            return Class.forName(fqClassName);

        } catch (ClassNotFoundException e) {

            // otherwise create the class
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.makeClass(fqClassName);

            for (Map.Entry<String, Class<?>> entry : fields.entrySet()) {

                // generate a public field (we don't need a setter)
                String fieldName = entry.getKey();
                CtClass valueClass = pool.get(entry.getValue().getName());
                CtField ctField = new CtField(valueClass, fieldName, ctClass);
                ctField.setModifiers(Modifier.PUBLIC);
                ctClass.addField(ctField);

                // generate the getter method
                String methodName = "get" + StringUtils.capitalize(fieldName);
                CtMethod ctMethod = CtNewMethod.make(valueClass, methodName, new CtClass[] {}, new CtClass[] {}, "{ return this." + fieldName + ";}", ctClass);
                ctClass.addMethod(ctMethod);
            }
            return ctClass.toClass();
        }
    }
}
