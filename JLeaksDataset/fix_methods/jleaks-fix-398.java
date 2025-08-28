
    NativeAutodetectProcess(String jobId, InputStream logStream, OutputStream processInStream, InputStream processOutStream,
                            InputStream persistStream, int numberOfAnalysisFields, List<Path> filesToDelete,
                            ExecutorService executorService) throws EsRejectedExecutionException {
        cppLogHandler = new CppLogMessageHandler(jobId, logStream);
        this.processInStream = new BufferedOutputStream(processInStream);
        this.processOutStream = processOutStream;
        this.persistStream = persistStream;
        this.recordWriter = new LengthEncodedWriter(this.processInStream);
        startTime = ZonedDateTime.now();
        this.numberOfAnalysisFields = numberOfAnalysisFields;
        this.filesToDelete = filesToDelete;
        logTailThread = executorService.submit(() -> {
            try (CppLogMessageHandler h = cppLogHandler) {
                h.tailStream();
            } catch (IOException e) {
                LOGGER.error("Error tailing C++ process logs", e);
            }
        });
    }