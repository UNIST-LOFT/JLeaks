public void execute(Connection conn) throws SQLException 
{
    mergeCondition();
    updateCount = -1;
    try {
        // SELECT ...
        if (sql.isSELECT()) {
            // If without callback, the query do NOT make sense.
            if (null != callback) {
                // Create ResultSet type upon the page. default is
                // TYPE_FORWARD_ONLY
                Pager pager = context.getPager();
                int rsType = null == pager ? ResultSet.TYPE_FORWARD_ONLY : pager.getResultSetType();
                PreparedStatement stat = null;
                ResultSet rs = null;
                try {
                    // Prepare statment for query
                    stat = conn.prepareStatement(sql.toPreparedStatementString(), rsType, ResultSet.CONCUR_READ_ONLY);
                    // Put all parameters to PreparedStatement and get
                    // ResultSet
                    adapter.process(stat, sql, entity);
                    rs = stat.executeQuery();
                    // Get result from ResultSet by callback
                    context.setResult(callback.invoke(conn, rs, this));
                } finally // Closing...
                {
                    Daos.safeClose(stat, rs);
                }
            }
        } else // UPDATE | INSERT | DELETE | TRUNCATE ...
        if (sql.isUPDATE() || sql.isINSERT() || sql.isDELETE() || sql.isTRUNCATE()) {
            PreparedStatement stat = null;
            try {
                stat = conn.prepareStatement(sql.toPreparedStatementString());
                adapter.process(stat, sql, entity);
                stat.execute();
                updateCount = stat.getUpdateCount();
                stat.close();
                if (null != callback)
                    context.setResult(callback.invoke(conn, null, this));
            } finally // Closing...
            {
                Daos.safeClose(stat, null);
            }
        } else // CREATE | DROP
        {
            Statement stat = null;
            try {
                stat = conn.createStatement();
                stat.execute(sql.toString());
                stat.close();
                if (null != callback)
                    context.setResult(callback.invoke(conn, null, this));
            } finally // Closing...
            {
                Daos.safeClose(stat, null);
            }
        }
    }// If any SQLException happend, throw out the SQL string
     catch (SQLException e) {
        throw new SQLException(format("!Nuz SQL Error: '%s'", sql.toString()), e);
    }
}