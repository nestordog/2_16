/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client.soda;

import java.util.ArrayList;
import java.io.StringWriter;

/**
 * A stream of events that is generated by pattern matches.
 * <p>
 * Patterns matches are events that match pattern expressions. Pattern expressions are built using
 * {@link Patterns}.
 */
public class PatternStream extends ProjectedStream
{
    private PatternExpr expression;
    private static final long serialVersionUID = -8321367637970657123L;

    /**
     * Ctor.
     */
    public PatternStream() {
    }

    /**

     * Creates a pattern stream from a pattern expression.
     * @param expression pattern expression
     * @return stream
     */
    public static PatternStream create(PatternExpr expression)
    {
        return new PatternStream(expression);
    }

    /**
     * Creates a named pattern stream from a pattern expression.
     * @param expression pattern expression
     * @param optStreamName is the pattern stream name (as-name)
     * @return stream
     */
    public static PatternStream create(PatternExpr expression, String optStreamName)
    {
        return new PatternStream(expression, optStreamName);
    }

    /**
     * Ctor.
     * @param expression pattern expression
     */
    public PatternStream(PatternExpr expression)
    {
        this(expression, null);
    }

    /**
     * Ctor.
     * @param expression pattern expression
     * @param optStreamName is the pattern stream name (as-name)
     */
    public PatternStream(PatternExpr expression, String optStreamName)
    {
        super(new ArrayList<View>(), optStreamName);
        this.expression = expression;
    }

    /**
     * Returns the pattern expression providing events to the stream.
     * @return pattern expression
     */
    public PatternExpr getExpression()
    {
        return expression;
    }

    /**
     * Sets the pattern expression providing events to the stream.
     * @param expression is the pattern expression
     */
    public void setExpression(PatternExpr expression)
    {
        this.expression = expression;
    }

    public void toEPLProjectedStream(StringWriter writer)
    {
        writer.write("pattern [");
        if (expression != null) {
            expression.toEPL(writer, PatternExprPrecedenceEnum.MINIMUM);
        }
        writer.write(']');
    }

    public void toEPLProjectedStreamType(StringWriter writer)
    {
        writer.write("pattern");
    }

    public void toEPLStreamOptions(StringWriter writer)
    {
    }
}
