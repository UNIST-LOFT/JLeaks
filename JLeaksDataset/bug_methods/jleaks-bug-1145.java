    private static boolean isPrimaryKeyExists(Connection conn, String... tables) throws SQLException {

        for (String table : tables) {
            try {
                val resultSet = conn.getMetaData().getPrimaryKeys(conn.getCatalog(), conn.getSchema(), table);
                if (resultSet.next()) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("get primary key from table {} failed", table, e);
            }
        }
        if (!conn.isClosed()) {
            conn.close();
        }
        return false;

    }
