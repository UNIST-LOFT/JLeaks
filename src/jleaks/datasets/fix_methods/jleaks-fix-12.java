public static void main(String[] args) throws Exception 
{
    SparkIntegrationChecker checker = new SparkIntegrationChecker();
    JCommander jCommander = new JCommander(checker, args);
    jCommander.setProgramName("SparkIntegrationChecker");
    // Creates a file to save user-facing messages
    try (PrintWriter reportWriter = CheckerUtils.initReportFile()) {
        // Starts the Java Spark Context
        SparkConf conf = new SparkConf().setAppName(SparkIntegrationChecker.class.getName());
        JavaSparkContext sc = new JavaSparkContext(conf);
        checker.printConfigInfo(conf, reportWriter);
        Status resultStatus = checker.run(sc, reportWriter);
        checker.printResultInfo(resultStatus, reportWriter);
        reportWriter.flush();
        System.exit(resultStatus.equals(Status.SUCCESS) ? 0 : 1);
    }
}