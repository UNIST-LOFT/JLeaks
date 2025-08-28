    private RecordCursor initFunctionsAndCursor(SqlExecutionContext executionContext, RecordCursor baseCursor) {
        long maxInMemoryRows = executionContext.getCairoSecurityContext().getMaxInMemoryRows();
        if (maxInMemoryRows > baseCursor.size()) {
            map.setMaxSize(maxInMemoryRows);
            cursor.of(baseCursor);
            // init all record function for this cursor, in case functions require metadata and/or symbol tables
            for (int i = 0, m = recordFunctions.size(); i < m; i++) {
                recordFunctions.getQuick(i).init(cursor, executionContext);
            }
            return cursor;
        }
        baseCursor.close();
        throw LimitOverflowException.instance(maxInMemoryRows);
    }
