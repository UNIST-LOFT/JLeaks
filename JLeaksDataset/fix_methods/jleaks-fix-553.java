static void resetOracle(String[] statements) throws SQLException 
{
    dropTriggers();
    dropSequences();
    dropTables();
    for (String statement : statements) {
        if (Util.blank(statement))
            continue;
        Statement st = null;
        try {
            st = Base.connection().createStatement();
            st.executeUpdate(statement);
        } catch (SQLException e) {
            System.out.println("Problem statement: " + statement);
            throw e;
        } finally {
            closeQuietly(st);
        }
    }
}