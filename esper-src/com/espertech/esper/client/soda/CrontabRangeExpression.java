package com.espertech.esper.client.soda;

import java.io.StringWriter;

/**
 * Parameter expression for use in crontab expressions and representing a range.
 */
public class CrontabRangeExpression extends ExpressionBase
{
    private static final long serialVersionUID = -6078090679603607493L;

    /**
     * Ctor.
     */
    public CrontabRangeExpression()
    {
    }

    /**
     * Ctor.
     * @param lowerBounds provides lower bounds
     * @param upperBounds provides upper bounds
     */
    public CrontabRangeExpression(Expression lowerBounds, Expression upperBounds)
    {
        this.getChildren().add(lowerBounds);
        this.getChildren().add(upperBounds);
    }

    public ExpressionPrecedenceEnum getPrecedence()
    {
        return ExpressionPrecedenceEnum.UNARY;
    }

    public void toPrecedenceFreeEPL(StringWriter writer)
    {
        this.getChildren().get(0).toEPL(writer, ExpressionPrecedenceEnum.MINIMUM);
        writer.append(":");
        this.getChildren().get(1).toEPL(writer, ExpressionPrecedenceEnum.MINIMUM);
    }
}
