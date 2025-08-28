/* L3 */ private int findColumn(String columnName) throws SQLServerException 
{
    if (paramNames == null) {
        SQLServerStatement s = null;
        try {
            // Note we are concatenating the information from the passed in sql, not any arguments provided by the user
            // if the user can execute the sql, any fragments of it is potentially executed via the meta data call through injection
            // is not a security issue.
            s = (SQLServerStatement) connection.createStatement();
            ThreePartName threePartName = ThreePartName.parse(procedureName);
            StringBuilder metaQuery = new StringBuilder("exec sp_sproc_columns ");
            if (null != threePartName.getDatabasePart()) {
                metaQuery.append("@procedure_qualifier=");
                metaQuery.append(threePartName.getDatabasePart());
                metaQuery.append(", ");
            }
            if (null != threePartName.getOwnerPart()) {
                metaQuery.append("@procedure_owner=");
                metaQuery.append(threePartName.getOwnerPart());
                metaQuery.append(", ");
            }
            if (null != threePartName.getProcedurePart()) {
                // we should always have a procedure name part
                metaQuery.append("@procedure_name=");
                metaQuery.append(threePartName.getProcedurePart());
                metaQuery.append(" , @ODBCVer=3");
            } else {
                // This should rarely happen, this will only happen if we cant find the stored procedure name
                // invalidly formatted call syntax.
                MessageFormat form = new MessageFormat(SQLServerException.getErrString("R_parameterNotDefinedForProcedure"));
                Object[] msgArgs = { columnName, "" };
                SQLServerException.makeFromDriverError(connection, this, form.format(msgArgs), "07009", false);
            }
            ResultSet rs = s.executeQueryInternal(metaQuery.toString());
            paramNames = new ArrayList<String>();
            while (rs.next()) {
                String sCol = rs.getString(4);
                paramNames.add(sCol.trim());
            }
        } catch (SQLException e) {
            SQLServerException.makeFromDriverError(connection, this, e.toString(), null, false);
        } finally {
            if (null != s)
                s.close();
        }
    }
    int l = 0;
    if (paramNames != null)
        l = paramNames.size();
    // In order to be as accurate as possible when locating parameter name
    // indexes, as well as be deterministic when running on various client
    // locales, we search for parameter names using the following scheme:
    // 1. Search using case-sensitive non-locale specific (binary) compare first.
    // 2. Search using case-insensitive, non-locale specific (binary) compare last.
    int i;
    int matchPos = -1;
    // Search using case-sensitive, non-locale specific (binary) compare.
    // If the user supplies a true match for the parameter name, we will find it here.
    for (i = 0; i < l; i++) {
        String sParam = paramNames.get(i);
        sParam = sParam.substring(1, sParam.length());
        if (sParam.equals(columnName)) {
            matchPos = i;
            break;
        }
    }
    if (-1 == matchPos) {
        // Check for case-insensitive match using a non-locale aware method.
        // Use VM supplied String.equalsIgnoreCase to do the "case-insensitive search".
        for (i = 0; i < l; i++) {
            String sParam = paramNames.get(i);
            sParam = sParam.substring(1, sParam.length());
            if (sParam.equalsIgnoreCase(columnName)) {
                matchPos = i;
                break;
            }
        }
    }
    if (-1 == matchPos) {
        MessageFormat form = new MessageFormat(SQLServerException.getErrString("R_parameterNotDefinedForProcedure"));
        Object[] msgArgs = { columnName, procedureName };
        SQLServerException.makeFromDriverError(connection, this, form.format(msgArgs), "07009", false);
    }
    // @RETURN_VALUE is always in the list. If the user uses return value ?=call(@p1) syntax then
    // @p1 is index 2 otherwise its index 1.
    if (// 3.2717
    bReturnValueSyntax)
        return matchPos + 1;
    else
        return matchPos;
}