  public ProgramController run(Program program, ProgramOptions options) {
    // Extract and verify parameters
    ApplicationSpecification appSpec = program.getSpecification();
    Preconditions.checkNotNull(appSpec, "Missing application specification.");

    Type processorType = program.getProcessorType();
    Preconditions.checkNotNull(processorType, "Missing processor type.");
    Preconditions.checkArgument(processorType == Type.MAPREDUCE, "Only MAPREDUCE process type is supported.");

    MapReduceSpecification spec = appSpec.getMapReduces().get(program.getProgramName());
    Preconditions.checkNotNull(spec, "Missing MapReduceSpecification for %s", program.getProgramName());

    OperationContext opexContext = new OperationContext(program.getAccountId(), program.getApplicationId());

    // Starting long-running transaction that we will also use in mapreduce tasks
    Transaction tx;
    try {
      tx = opex.startTransaction(opexContext, false);
      txAgent = new SmartTransactionAgent(opex, opexContext, tx);
      txAgent.start();
    } catch (OperationException e) {
      LOG.error("Failed to start transaction for mapreduce job: " + program.getProgramName());
      throw Throwables.propagate(e);
    }

    TransactionProxy transactionProxy = new TransactionProxy();
    transactionProxy.setTransactionAgent(txAgent);

    DataFabric dataFabric = new DataFabricImpl(opex, opexContext);
    DataSetInstantiator dataSetContext =
      new DataSetInstantiator(dataFabric, transactionProxy, program.getClassLoader());
    dataSetContext.setDataSets(Lists.newArrayList(program.getSpecification().getDataSets().values()));

    try {
      RunId runId = RunIds.generate();
      final BasicMapReduceContext context =
        new BasicMapReduceContext(program, runId, options.getUserArguments(), txAgent,
                                  DataSets.createDataSets(dataSetContext, spec.getDataSets()), spec);

      MapReduce job = (MapReduce) program.getMainClass().newInstance();
      context.injectFields(job);

      // note: this sets logging context on the thread level
      LoggingContextAccessor.setLoggingContext(context.getLoggingContext());

      controller = new MapReduceProgramController(context);

      LOG.info("Starting MapReduce job: " + context.toString());
      submit(job, program.getProgramJarLocation(), context, tx);

      // adding listener which stops mapreduce job when controller stops.
      controller.addListener(new AbstractListener() {
        @Override
        public void stopping() {
          LOG.info("Stopping mapreduce job: " + context);
          try {
            if (!jobConf.isComplete()) {
              jobConf.killJob();
            }
          } catch (Exception e) {
            throw Throwables.propagate(e);
          }
          LOG.info("Mapreduce job stopped: " + context);
        }
      }, MoreExecutors.sameThreadExecutor());

      return controller;

    } catch (Throwable e) {
      try {
        transactionProxy.getTransactionAgent().abort();
      } catch (OperationException ex) {
        throw Throwables.propagate(ex);
      }
      LOG.error("Failed to run mapreduce job: " + program.getProgramName(), e);
      throw Throwables.propagate(e);
    }
  }
