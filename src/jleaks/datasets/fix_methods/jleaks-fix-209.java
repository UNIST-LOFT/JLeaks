
    public final ResultSet getSuperTypes(final String catalog, final String schemaPattern, final String typeNamePattern) throws SQLException {
        try (Connection connection = getConnection()) {
            return new DatabaseMetaDataResultSet(connection.getMetaData().getSuperTypes(catalog, schemaPattern, typeNamePattern), shardingRule);
        }
    }