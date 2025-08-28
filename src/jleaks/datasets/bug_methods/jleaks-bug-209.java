    public final ResultSet getSuperTypes(final String catalog, final String schemaPattern, final String typeNamePattern) throws SQLException {
        return new DatabaseMetaDataResultSet(getConnection().getMetaData().getSuperTypes(catalog, schemaPattern, typeNamePattern), shardingRule);
    }
