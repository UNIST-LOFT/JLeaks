public ResultSet getTypeInfo() throws SQLException 
{
    Registry registry = connection.getRegistry();
    ResultField[] resultFields = new ResultField[18];
    List<Object[]> results = new ArrayList<>();
    resultFields[0] = new ResultField("TYPE_NAME", 0, (short) 0, registry.loadType("text"), (short) 0, 0, Format.Binary);
    resultFields[1] = new ResultField("DATA_TYPE", 0, (short) 0, registry.loadType("int2"), (short) 0, 0, Format.Binary);
    resultFields[2] = new ResultField("PRECISION", 0, (short) 0, registry.loadType("int4"), (short) 0, 0, Format.Binary);
    resultFields[3] = new ResultField("LITERAL_PREFIX", 0, (short) 0, registry.loadType("text"), (short) 0, 0, Format.Binary);
    resultFields[4] = new ResultField("LITERAL_SUFFIX", 0, (short) 0, registry.loadType("text"), (short) 0, 0, Format.Binary);
    resultFields[5] = new ResultField("CREATE_PARAMS", 0, (short) 0, registry.loadType("text"), (short) 0, 0, Format.Binary);
    resultFields[6] = new ResultField("NULLABLE", 0, (short) 0, registry.loadType("int2"), (short) 0, 0, Format.Binary);
    resultFields[7] = new ResultField("CASE_SENSITIVE", 0, (short) 0, registry.loadType("bool"), (short) 0, 0, Format.Binary);
    resultFields[8] = new ResultField("SEARCHABLE", 0, (short) 0, registry.loadType("int2"), (short) 0, 0, Format.Binary);
    resultFields[9] = new ResultField("UNSIGNED_ATTRIBUTE", 0, (short) 0, registry.loadType("bool"), (short) 0, 0, Format.Binary);
    resultFields[10] = new ResultField("FIXED_PREC_SCALE", 0, (short) 0, registry.loadType("bool"), (short) 0, 0, Format.Binary);
    resultFields[11] = new ResultField("AUTO_INCREMENT", 0, (short) 0, registry.loadType("bool"), (short) 0, 0, Format.Binary);
    resultFields[12] = new ResultField("LOCAL_TYPE_NAME", 0, (short) 0, registry.loadType("text"), (short) 0, 0, Format.Binary);
    resultFields[13] = new ResultField("MINIMUM_SCALE", 0, (short) 0, registry.loadType("int2"), (short) 0, 0, Format.Binary);
    resultFields[14] = new ResultField("MAXIMUM_SCALE", 0, (short) 0, registry.loadType("int2"), (short) 0, 0, Format.Binary);
    resultFields[15] = new ResultField("SQL_DATA_TYPE", 0, (short) 0, registry.loadType("int4"), (short) 0, 0, Format.Binary);
    resultFields[16] = new ResultField("SQL_DATETIME_SUB", 0, (short) 0, registry.loadType("int4"), (short) 0, 0, Format.Binary);
    resultFields[17] = new ResultField("NUM_PREC_RADIX", 0, (short) 0, registry.loadType("int4"), (short) 0, 0, Format.Binary);
    String sql = "SELECT t.typname,t.oid FROM pg_catalog.pg_type t" + " JOIN pg_catalog.pg_namespace n ON (t.typnamespace = n.oid) " + " WHERE n.nspname != 'pg_toast'";
    ResultSet rs = null;
    try {
        rs = execForResultSet(sql);
        while (rs.next()) {
            Object[] row = new Object[18];
            int typeOid = rs.getInt(2);
            Type type = registry.loadType(typeOid);
            row[0] = SQLTypeMetaData.getTypeName(type, null, 0);
            row[1] = SQLTypeMetaData.getSQLType(type);
            row[2] = SQLTypeMetaData.getMaxPrecision(type);
            if (SQLTypeMetaData.requiresQuoting(type)) {
                row[3] = "\'";
                row[4] = "\'";
            }
            row[6] = SQLTypeMetaData.isNullable(type, null, 0);
            row[7] = SQLTypeMetaData.isCaseSensitive(type);
            row[8] = true;
            row[9] = !SQLTypeMetaData.isSigned(type);
            row[10] = SQLTypeMetaData.isCurrency(type);
            row[11] = SQLTypeMetaData.isAutoIncrement(type, null, 0);
            row[13] = SQLTypeMetaData.getMinScale(type);
            row[14] = SQLTypeMetaData.getMaxScale(type);
            // Unused
            row[15] = null;
            // Unused
            row[16] = null;
            row[17] = SQLTypeMetaData.getPrecisionRadix(type);
            results.add(row);
        }
    } finally {
        if (rs != null) {
            rs.close();
        }
    }
    return createResultSet(Arrays.asList(resultFields), results);
}