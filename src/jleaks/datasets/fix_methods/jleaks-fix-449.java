private static void initInternal(File configFile, String outputPath, boolean stdOut) throws IOException{
    List<Appender> appenders = new ArrayList<Appender>();
    // Retrieve all existing appenders
    Enumeration apps = LogManager.getRootLogger().getAllAppenders();
    while (apps.hasMoreElements()) {
        Appender appender = (Appender) apps.nextElement();
        if (!(appender instanceof ConsoleAppender || appender instanceof FileAppender))
            appenders.add(appender);
    }
    Properties lprops = new Properties();
    try (FileInputStream in = new FileInputStream(configFile)) {
        ;
        lprops.load(in);
    }
    LogManager.resetConfiguration();
    if (lprops.getProperty("log4j.appender.orslogfile.File") == null)
        lprops.put("log4j.appender.orslogfile.File", outputPath);
    PropertyConfigurator.configure(lprops);
    if (!stdOut) {
        apps = org.apache.log4j.Logger.getRootLogger().getAllAppenders();
        while (apps.hasMoreElements()) {
            Appender appender = (Appender) apps.nextElement();
            if (appender instanceof org.apache.log4j.ConsoleAppender) {
                org.apache.log4j.Logger.getRootLogger().removeAppender(appender);
            }
        }
    }
    for (Appender appender : appenders) LogManager.getRootLogger().addAppender(appender);
}