    public void connectStreams(Duration timeout) throws IOException {
        // The order here is important.  It must match the order that the C++ process tries to connect to the pipes, otherwise
        // a timeout is guaranteed.  Also change api::CIoManager in the C++ code if changing the order here.
        if (logPipeName != null) {
            logStream = namedPipeHelper.openNamedPipeInputStream(logPipeName, timeout);
        }
        if (commandPipeName != null) {
            commandStream = namedPipeHelper.openNamedPipeOutputStream(commandPipeName, timeout);
        }
        if (processInPipeName != null) {
            processInStream = namedPipeHelper.openNamedPipeOutputStream(processInPipeName, timeout);
        }
        if (processOutPipeName != null) {
            processOutStream = namedPipeHelper.openNamedPipeInputStream(processOutPipeName, timeout);
        }
        if (restorePipeName != null) {
            restoreStream = namedPipeHelper.openNamedPipeOutputStream(restorePipeName, timeout);
        }
        if (persistPipeName != null) {
            persistStream = namedPipeHelper.openNamedPipeInputStream(persistPipeName, timeout);
        }
    }
