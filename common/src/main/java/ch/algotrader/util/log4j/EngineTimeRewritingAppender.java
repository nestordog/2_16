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
package ch.algotrader.util.log4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.config.AppenderControl;

/**
 * Log4J log appender delegates to another appender after rewriting events using the given
 * {@link org.apache.logging.log4j.core.appender.rewrite.RewritePolicy}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public final class EngineTimeRewritingAppender extends AbstractAppender {

    private static final long serialVersionUID = 1L;

    private final AppenderControl appenderControl;
    private final RewritePolicy rewritePolicy;

    EngineTimeRewritingAppender(final Appender appender, final RewritePolicy rewritePolicy) {
        super(appender.getName(), null, null, true);
        Filter filter = appender instanceof AbstractAppender ? ((AbstractAppender) appender).getFilter() : null;
        this.appenderControl = new AppenderControl(appender, Level.ALL, filter);
        this.rewritePolicy = rewritePolicy;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void append(final LogEvent event) {
        this.appenderControl.callAppender(this.rewritePolicy.rewrite(event));
    }

}
