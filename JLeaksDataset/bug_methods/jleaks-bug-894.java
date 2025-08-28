    public void process() throws Exception {

        stats.printSystemInfo();

        output = output.getCanonicalFile();

        args = (CmdLineArgs) caseData.getCaseObject(CmdLineArgs.class.getName());

        prepareOutputFolder();

        if ((args.isContinue() || args.isRestart())) {
            if (finalIndexDir.exists()) {
                indexDir = finalIndexDir;
            } else if (indexDir != finalIndexDir) {
                changeTempDir();
            }
        }

        if (args.getEvidenceToRemove() != null) {
            indexDir = finalIndexDir;
        }

        saveCurrentTempDir();

        int i = 1;
        for (File source : sources) {
            LOGGER.info("Evidence " + (i++) + ": '{}'", source.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        try {
            if (!iniciarIndexacao())
                return;

            // apenas conta o número de arquivos a indexar
            contador = new ItemProducer(this, caseData, true, sources, output);
            contador.start();

            // produz lista de arquivos e propriedades a indexar
            produtor = new ItemProducer(this, caseData, false, sources, output);
            produtor.start();

            monitorarIndexacao();
            finalizarIndexacao();

        } catch (Exception e) {
            interromperIndexacao();
            throw e;
        }
    }
