public Result execStatement( String rawsql, RowMetaInterface params, Object[] data ) throws KettleDatabaseException 
{
    Result result = new Result();
    // Replace existing code with a class that removes comments from the raw
    // SQL.
    // The SqlCommentScrubber respects single-quoted strings, so if a
    // double-dash or a multiline comment appears
    // in a single-quoted string, it will be treated as a string instead of
    // comments.
    String sql = databaseMeta.getDatabaseInterface().createSqlScriptParser().removeComments(rawsql).trim();
    try {
        boolean resultSet;
        int count;
        if (params != null) {
            try (PreparedStatement prepStmt = connection.prepareStatement(databaseMeta.stripCR(sql))) {
                // set the parameters!
                setValues(params, data, prepStmt);
                resultSet = prepStmt.execute();
                count = prepStmt.getUpdateCount();
            }
        } else {
            String sqlStripped = databaseMeta.stripCR(sql);
            try (Statement stmt = connection.createStatement()) {
                resultSet = stmt.execute(sqlStripped);
                count = stmt.getUpdateCount();
            }
        }
        String upperSql = sql.toUpperCase();
        if (!resultSet) {
            // if the result is a resultset, we don't do anything with it!
            // You should have called something else!
            if (count > 0) {
                if (upperSql.startsWith("INSERT")) {
                    result.setNrLinesOutput(count);
                } else if (upperSql.startsWith("UPDATE")) {
                    result.setNrLinesUpdated(count);
                } else if (upperSql.startsWith("DELETE")) {
                    result.setNrLinesDeleted(count);
                }
            }
        }
        // See if a cache needs to be cleared...
        if (upperSql.startsWith("ALTER TABLE") || upperSql.startsWith("DROP TABLE") || upperSql.startsWith("CREATE TABLE")) {
            DBCache.getInstance().clear(databaseMeta.getName());
        }
    } catch (SQLException ex) {
        throw new KettleDatabaseException("Couldn't execute SQL: " + sql + Const.CR, ex);
    } catch (Exception e) {
        throw new KettleDatabaseException("Unexpected error executing SQL: " + Const.CR, e);
    }
    return result;
}