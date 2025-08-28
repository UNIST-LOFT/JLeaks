private static void configureLog4J(Properties properties, PigContext pigContext) {
    // TODO Add a file appender for the logs
    // TODO Need to create a property in the properties file for it.
    // sgroschupf, 25Feb2008: this method will be obsolete with PIG-115.
     
    String log4jconf = properties.getProperty(LOG4J_CONF);
    String trueString = "true";
    boolean brief = trueString.equalsIgnoreCase(properties.getProperty(BRIEF));
    Level logLevel = Level.INFO;
    String logLevelString = properties.getProperty(DEBUG);
    if (logLevelString != null){
        logLevel = Level.toLevel(logLevelString, Level.INFO);
    }

    Properties props = new Properties();
    FileReader propertyReader = null;
    if (log4jconf != null) {
        try {
            propertyReader = new FileReader(log4jconf);
            props.load(propertyReader);
        }
        catch (IOException e)
        {
            System.err.println("Warn: Cannot open log4j properties file, use default");
        }
        finally
        {
            if (propertyReader != null) try {propertyReader.close();} catch(Exception e) {}
        }
    }
    if (props.size() == 0) {
        props.setProperty("log4j.rootLogger", "INFO, PIGCONSOLE");
        props.setProperty("log4j.logger.org.apache.pig", logLevel.toString());
        props.setProperty("log4j.appender.PIGCONSOLE",
                "org.apache.log4j.ConsoleAppender");
        props.setProperty("log4j.appender.PIGCONSOLE.layout",
                "org.apache.log4j.PatternLayout");
        props.setProperty("log4j.appender.PIGCONSOLE.target", "System.err");
        if (!brief) {
            // non-brief logging - timestamps
            props.setProperty(
                    "log4j.appender.PIGCONSOLE.layout.ConversionPattern",
                    "%d [%t] %-5p %c - %m%n");
        } else {
            // brief logging - no timestamps
            props.setProperty(
                    "log4j.appender.PIGCONSOLE.layout.ConversionPattern",
                    "%m%n");
        }
    }
    PropertyConfigurator.configure(props);
    logLevel = Logger.getLogger("org.apache.pig").getLevel();
    Properties backendProps = pigContext.getLog4jProperties();
    backendProps.setProperty("log4j.logger.org.apache.pig.level", logLevel.toString());
    pigContext.setLog4jProperties(backendProps);
    pigContext.setDefaultLogLevel(logLevel);
}