    private QueryBuilder(DataSource dataSource, String query) throws SQLException {
        indexMap = new HashMap<String, List<Integer>>();
        statement = dataSource.getConnection().prepareStatement(
                parse(query, indexMap), Statement.RETURN_GENERATED_KEYS);
    }
