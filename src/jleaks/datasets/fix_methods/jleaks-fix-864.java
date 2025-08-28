
    public RecordCursor getCursor(SqlExecutionContext executionContext) {
        final RecordCursor baseCursor = base.getCursor(executionContext);
        try {
            if (baseCursor.hasNext()) {
                map.clear();
                return initFunctionsAndCursor(executionContext, baseCursor);
            }

            return EmptyTableRecordCursor.INSTANCE;
        } catch (CairoException ex) {
            baseCursor.close();
            throw ex;
        }
    }