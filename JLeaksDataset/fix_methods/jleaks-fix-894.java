public void process() throws Exception 
{
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
        // $NON-NLS-1$ //$NON-NLS-2$
        LOGGER.info("Evidence " + (i++) + ": '{}'", source.getAbsolutePath());
    }
    try {
        if (!iniciarIndexacao())
            return;
        // apenas conta o número de arquivos a indexar
        counter = new ItemProducer(this, caseData, true, sources, output);
        counter.start();
        // produz lista de arquivos e propriedades a indexar
        producer = new ItemProducer(this, caseData, false, sources, output);
        producer.start();
        monitorarIndexacao();
        finalizarIndexacao();
    } catch (Exception e) {
        interromperIndexacao();
        throw e;
    } finally {
        closeItemProducers();
    }
    filtrarPalavrasChave();
    removeEmptyTreeNodes();
    new P2PBookmarker(caseData).createBookmarksForSharedFiles(output.getParentFile());
    updateImagePaths();
    shutDownSleuthkitServers();
    deleteTempDir();
    stats.logarEstatisticas(this);
}