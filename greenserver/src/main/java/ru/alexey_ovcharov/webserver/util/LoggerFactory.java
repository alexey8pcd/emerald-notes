package ru.alexey_ovcharov.webserver.util;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * @author Alexey
 */
public class LoggerFactory {

    public static Logger createConsoleLogger(String name) {
        Logger rootLogger = Logger.getRootLogger();
        PatternLayout layout = new PatternLayout("%-5p %d %m%n");
        ConsoleAppender appender = new ConsoleAppender(layout);
        appender.setName(name);
        appender.activateOptions();
        rootLogger.addAppender(appender);
        return rootLogger;
    }
}
