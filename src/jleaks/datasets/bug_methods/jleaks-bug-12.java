  public static void main(String[] args) throws Exception {
    SparkIntegrationChecker checker = new SparkIntegrationChecker();
    JCommander jCommander = new JCommander(checker, args);
    jCommander.setProgramName("SparkIntegrationChecker");

    // Creates a file to save user-facing messages
    FileWriter fileWriter = new FileWriter("./SparkIntegrationReport.txt", true);
    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
    PrintWriter reportWriter = new PrintWriter(bufferedWriter);

    // Starts the Java Spark Context
    SparkConf conf = new SparkConf().setAppName(SparkIntegrationChecker.class.getName());
    JavaSparkContext sc = new JavaSparkContext(conf);

    checker.printConfigInfo(conf, reportWriter);
    Status resultStatus = checker.run(sc, reportWriter);
    checker.printResultInfo(resultStatus, reportWriter);

    reportWriter.flush();
    reportWriter.close();

    System.exit(resultStatus.equals(Status.SUCCESS) ? 0 : 1);
  }
