package de.crafttogether.common.mysql;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

public class LogFilter extends AbstractFilter
{
    private static LogFilter registeredFilter;
    public static synchronized boolean registerFilter()
    {
        if (registeredFilter != null)
        {
            return true;
        }

        org.apache.logging.log4j.Logger logger = LogManager.getRootLogger();

        if (!(logger instanceof org.apache.logging.log4j.core.Logger coreLogger))
        {
            return false;
        }

        registeredFilter = new LogFilter();
        coreLogger.addFilter(registeredFilter);
        return true;
    }

    public static synchronized void unregisterFilter()
    {
        if (registeredFilter == null)
        {
            return;
        }

        org.apache.logging.log4j.Logger logger = LogManager.getRootLogger();

        if (logger instanceof org.apache.logging.log4j.core.Logger coreLogger)
        {
            var loggerConfig = coreLogger.get();

            if (loggerConfig != null)
            {
                loggerConfig.removeFilter(registeredFilter);

                var context = coreLogger.getContext();
                context.updateLoggers();
            }
        }

        registeredFilter = null;
    }

    @Override
    public Result filter(LogEvent event)
    {
        if(event == null)
        {
            return Result.NEUTRAL;
        }
        if(event.getLoggerName().contains("com.zaxxer.hikari"))
        {
            return Result.DENY;
        }
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t)
    {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params)
    {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t)
    {
        return Result.NEUTRAL;
    }
}
