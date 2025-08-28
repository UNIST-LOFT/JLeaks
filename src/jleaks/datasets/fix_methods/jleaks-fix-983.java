
    private QueryBuilder(DataSource dataSource, String query) throws SQLException {
        indexMap = new HashMap<String, List<Integer>>();
        connection = dataSource.getConnection();
        String parsedQuery = parse(query, indexMap);
        try {
            statement = connection.prepareStatement(parsedQuery, Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException error) {
            connection.close();
            throw error;
        }
    }
    