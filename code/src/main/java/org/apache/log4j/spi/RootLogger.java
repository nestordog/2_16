//
package org.apache.log4j.spi;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;

// Contibutors: Mathias Bogaert

/**
   RootLogger sits at the top of the logger hierachy. It is a
   regular logger except that it provides several guarantees.

   <p>First, it cannot be assigned a <code>null</code>
   level. Second, since root logger cannot have a parent, the
   {@link #getChainedLevel} method always returns the value of the
   level field without walking the hierarchy.

   @author Ceki G&uuml;lc&uuml;

 */
public final class RootLogger extends Logger {
    /**
       The root logger names itself as "root". However, the root
       logger cannot be retrieved by name.
    */
    public RootLogger(Level level) {
        super("root");
        setLevel(level);
    }

    /**
       Return the assigned level value without walking the logger
       hierarchy.
    */
    public final Level getChainedLevel() {
        return this.level;
    }

    /**
       Setting a null value to the level of the root logger may have catastrophic
       results. We prevent this here.

       @since 0.8.3 */
    public final void setLevel(Level level) {
        String commandLineLevel = System.getProperty("logLevel");
        if (commandLineLevel != null) {
            level = Level.toLevel(commandLineLevel);
        }
        if (level == null) {
            LogLog.error("You have tried to set a null level to root.", new Throwable());
        } else {
            this.level = level;
        }
    }

}
