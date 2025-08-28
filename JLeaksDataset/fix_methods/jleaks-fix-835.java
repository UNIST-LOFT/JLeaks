public void profileStep() throws Exception 
{
    final Spoon spoon = ((Spoon) SpoonFactory.getInstance());
    try {
        final TransMeta transMeta = spoon.getActiveTransformation();
        if (transMeta == null || spoon.getActiveTransGraph() == null) {
            return;
        }
        StepMeta stepMeta = spoon.getActiveTransGraph().getCurrentStep();
        if (stepMeta == null) {
            return;
        }
        // TODO: show the transformation execution configuration dialog
        // 
        // 
        TransExecutionConfiguration executionConfiguration = spoon.getTransPreviewExecutionConfiguration();
        TransExecutionConfigurationDialog tecd = new TransExecutionConfigurationDialog(spoon.getShell(), executionConfiguration, transMeta);
        if (!tecd.open())
            return;
        // Pass the configuration to the transMeta object:
        // 
        String[] args = null;
        Map<String, String> arguments = executionConfiguration.getArguments();
        if (arguments != null) {
            args = convertArguments(arguments);
        }
        transMeta.injectVariables(executionConfiguration.getVariables());
        // Set the named parameters
        Map<String, String> paramMap = executionConfiguration.getParams();
        Set<String> keys = paramMap.keySet();
        for (String key : keys) {
            // $NON-NLS-1$
            transMeta.setParameterValue(key, Const.NVL(paramMap.get(key), ""));
        }
        transMeta.activateParameters();
        // Do we need to clear the log before running?
        // 
        if (executionConfiguration.isClearingLog()) {
            spoon.getActiveTransGraph().transLogDelegate.clearLog();
        }
        // Now that we have the transformation and everything we can run it
        // and profile it...
        // 
        Trans trans = new Trans(transMeta, Spoon.loggingObject);
        trans.prepareExecution(executionConfiguration.getArgumentStrings());
        trans.setSafeModeEnabled(executionConfiguration.isSafeModeEnabled());
        trans.setPreview(true);
        trans.prepareExecution(args);
        trans.setRepository(spoon.rep);
        // Open a server socket. This thing will block on init() until
        // DataCleaner connects to it...
        final DataCleanerKettleFileWriter writer = new DataCleanerKettleFileWriter(trans, stepMeta);
        try {
            writer.run();
        } finally {
            writer.close();
        }
        // Pass along the configuration of the KettleDatabaseStore...
        // 
        AnalyzerBeansConfiguration abc = new AnalyzerBeansConfigurationImpl();
        AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(abc);
        Datastore datastore = new KettleDatastore(transMeta.getName(), stepMeta.getName(), transMeta.getStepFields(stepMeta));
        analysisJobBuilder.setDatastore(datastore);
        DatastoreConnection connection = null;
        try {
            connection = datastore.openConnection();
            DataContext dataContext = connection.getDataContext();
            // add all columns of a table
            Column[] customerColumns = dataContext.getTableByQualifiedLabel(stepMeta.getName()).getColumns();
            analysisJobBuilder.addSourceColumns(customerColumns);
            List<InputColumn<?>> numberColumns = analysisJobBuilder.getAvailableInputColumns(Number.class);
            if (!numberColumns.isEmpty()) {
                analysisJobBuilder.addAnalyzer(NumberAnalyzer.class).addInputColumns(numberColumns);
            }
            List<InputColumn<?>> dateColumns = analysisJobBuilder.getAvailableInputColumns(Date.class);
            if (!dateColumns.isEmpty()) {
                analysisJobBuilder.addAnalyzer(DateAndTimeAnalyzer.class).addInputColumns(dateColumns);
            }
            List<InputColumn<?>> booleanColumns = analysisJobBuilder.getAvailableInputColumns(Boolean.class);
            if (!booleanColumns.isEmpty()) {
                analysisJobBuilder.addAnalyzer(BooleanAnalyzer.class).addInputColumns(booleanColumns);
            }
            List<InputColumn<?>> stringColumns = analysisJobBuilder.getAvailableInputColumns(String.class);
            if (!stringColumns.isEmpty()) {
                analysisJobBuilder.addAnalyzer(StringAnalyzer.class).addInputColumns(stringColumns);
            }
            // Write the job.xml to a temporary file...
            // 
            final FileObject jobFile = KettleVFS.createTempFile("datacleaner-job", ".xml", System.getProperty("java.io.tmpdir"), new Variables());
            OutputStream jobOutputStream = null;
            try {
                jobOutputStream = KettleVFS.getOutputStream(jobFile, false);
                new JaxbJobWriter(abc).write(analysisJobBuilder.toAnalysisJob(), jobOutputStream);
                jobOutputStream.close();
            } finally {
                if (jobOutputStream != null) {
                    jobOutputStream.close();
                }
            }
            // Write the conf.xml to a temporary file...
            // 
            String confXml = generateConfXml(transMeta.getName(), stepMeta.getName(), writer.getFilename());
            final FileObject confFile = KettleVFS.createTempFile("datacleaner-conf", ".xml", System.getProperty("java.io.tmpdir"), new Variables());
            OutputStream confOutputStream = null;
            try {
                confOutputStream = KettleVFS.getOutputStream(confFile, false);
                confOutputStream.write(confXml.getBytes(Const.XML_ENCODING));
                confOutputStream.close();
            } finally {
                if (confOutputStream != null) {
                    confOutputStream.close();
                }
            }
            // Launch DataCleaner and point to the generated configuration
            // and job XML files...
            // 
            Spoon.getInstance().getDisplay().syncExec(new Runnable() {

                public void run() {
                    new Thread() {

                        public void run() {
                            launchDataCleaner(KettleVFS.getFilename(confFile), KettleVFS.getFilename(jobFile), transMeta.getName(), writer.getFilename());
                        }
                    }.start();
                }
            });
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    } catch (final Exception e) {
        new ErrorDialog(spoon.getShell(), "Error", "unexpected error occurred", e);
    } finally {
        // 
    }
}