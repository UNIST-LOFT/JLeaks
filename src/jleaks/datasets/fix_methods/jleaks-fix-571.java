public void finishBulkLoad(@NotNull DBCSession session) throws DBCException {
    try {
        csvWriter.flush();
        csvWriter.close();
    } catch (IOException e) {
        log.debug(e);
    }
    csvWriter = null;
    String tableFQN = table.getFullyQualifiedName(DBPEvaluationContext.DML);
    session.getProgressMonitor().subTask("Copy into " + tableFQN);
    String queryText = "COPY " + tableFQN + " FROM STDIN (FORMAT CSV)";

    try {
        Object rowCount;
        try (Reader csvReader = new FileReader(csvFile, StandardCharsets.UTF_8)) {
            rowCount = copyInMethod.invoke(copyManager, queryText, csvReader, copyBufferSize);
        }

        // Commit changes
        DBCTransactionManager txnManager = DBUtils.getTransactionManager(session.getExecutionContext());
        if (txnManager != null && !txnManager.isAutoCommit()) {
            session.getProgressMonitor().subTask("Commit COPY");
            txnManager.commit(session);
        }
        log.debug("CSV has been imported (" + rowCount + ")");
    } catch (Throwable e) {
        if (e instanceof InvocationTargetException) {
            e = ((InvocationTargetException) e).getTargetException();
        }
        throw new DBCException("Error copying dataset on remote server", e);
    }
}