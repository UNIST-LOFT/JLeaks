    public RecordCursor getCursor(SqlExecutionContext executionContext) {
        final RecordCursor baseCursor = base.getCursor(executionContext);
        if (baseCursor.hasNext()) {
            map.clear();
            return initFunctionsAndCursor(executionContext, baseCursor);
        }

        baseCursor.close();
        return EmptyTableRecordCursor.INSTANCE;
    }
