package ru.alexey_ovcharov.webserver.common.util;

import java.util.Enumeration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * @author Alexey
 */
public class LoggerFactory {

    public static Logger createConsoleLogger(String name) {
        Logger rootLogger = Logger.getRootLogger();
        Enumeration<Appender> allAppenders = rootLogger.getAllAppenders();
        boolean exists = false;
        while (allAppenders.hasMoreElements()) {
            Appender appender = allAppenders.nextElement();
            if (StringUtils.equals(appender.getName(), name)) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            PatternLayout layout = new PatternLayout("%-5p %d %m%n");
            ConsoleAppender appender = new ConsoleAppender(layout);
            appender.setName(name);
            appender.activateOptions();
            rootLogger.addAppender(appender);
        }
        Logger logger = Logger.getLogger(name);
        logger.setLevel(Level.DEBUG);
        logger.setAdditivity(true);
        return logger;
    }
}
