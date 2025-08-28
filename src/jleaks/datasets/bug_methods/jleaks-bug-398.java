    void tailLogsInThread() {
        logTailThread = new Thread(() -> {
            try {
                cppLogHandler.tailStream();
                cppLogHandler.close();
            } catch (IOException e) {
                LOGGER.error("Error tailing C++ process logs", e);
            }
        });
        logTailThread.start();
    }
