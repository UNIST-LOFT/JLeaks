
    private RecordCursor initFunctionsAndCursor(SqlExecutionContext executionContext, RecordCursor baseCursor) {
        try {
            cursor.of(baseCursor, executionContext);
            // init all record function for this cursor, in case functions require metadata and/or symbol tables
            for (int i = 0, m = recordFunctions.size(); i < m; i++) {
                recordFunctions.getQuick(i).init(cursor, executionContext);
            }
            return cursor;
        } catch (CairoException ex) {
            baseCursor.close();
            throw ex;
        }
    }