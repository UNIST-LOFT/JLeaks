private void getParameterEncryptionMetadata(Parameter[] params) throws SQLServerException 
{
    /*
         * The parameter list is created from the data types provided by the user for the parameters. the data types do
         * not need to be the same as in the table definition. Also, when string is sent to an int field, the parameter
         * is defined as nvarchar(<size of string>). Same for varchar datatypes, exact length is used.
         */
    assert connection != null : "Connection should not be null";
    try (Statement stmt = connection.prepareCall("exec sp_describe_parameter_encryption ?,?")) {
        if (getStatementLogger().isLoggable(java.util.logging.Level.FINE)) {
            getStatementLogger().fine("Calling stored procedure sp_describe_parameter_encryption to get parameter encryption information.");
        }
        ((SQLServerCallableStatement) stmt).isInternalEncryptionQuery = true;
        ((SQLServerCallableStatement) stmt).setNString(1, preparedSQL);
        ((SQLServerCallableStatement) stmt).setNString(2, preparedTypeDefinitions);
        try (ResultSet rs = ((SQLServerCallableStatement) stmt).executeQueryInternal()) {
            if (null == rs) {
                // No results. Meaning no parameter.
                // Should never happen.
                return;
            }
            Map<Integer, CekTableEntry> cekList = new HashMap<>();
            CekTableEntry cekEntry = null;
            while (rs.next()) {
                int currentOrdinal = rs.getInt(DescribeParameterEncryptionResultSet1.KeyOrdinal.value());
                if (!cekList.containsKey(currentOrdinal)) {
                    cekEntry = new CekTableEntry(currentOrdinal);
                    cekList.put(cekEntry.ordinal, cekEntry);
                } else {
                    cekEntry = cekList.get(currentOrdinal);
                }
                cekEntry.add(rs.getBytes(DescribeParameterEncryptionResultSet1.EncryptedKey.value()), rs.getInt(DescribeParameterEncryptionResultSet1.DbId.value()), rs.getInt(DescribeParameterEncryptionResultSet1.KeyId.value()), rs.getInt(DescribeParameterEncryptionResultSet1.KeyVersion.value()), rs.getBytes(DescribeParameterEncryptionResultSet1.KeyMdVersion.value()), rs.getString(DescribeParameterEncryptionResultSet1.KeyPath.value()), rs.getString(DescribeParameterEncryptionResultSet1.ProviderName.value()), rs.getString(DescribeParameterEncryptionResultSet1.KeyEncryptionAlgorithm.value()));
            }
            if (getStatementLogger().isLoggable(java.util.logging.Level.FINE)) {
                getStatementLogger().fine("Matadata of CEKs is retrieved.");
            }
            // Process the second resultset.
            if (!stmt.getMoreResults()) {
                throw new SQLServerException(this, SQLServerException.getErrString("R_UnexpectedDescribeParamFormat"), null, 0, false);
            }
            // Parameter count in the result set.
            int paramCount = 0;
            try (ResultSet secondRs = stmt.getResultSet()) {
                while (secondRs.next()) {
                    paramCount++;
                    String paramName = secondRs.getString(DescribeParameterEncryptionResultSet2.ParameterName.value());
                    int paramIndex = parameterNames.indexOf(paramName);
                    int cekOrdinal = secondRs.getInt(DescribeParameterEncryptionResultSet2.ColumnEncryptionKeyOrdinal.value());
                    cekEntry = cekList.get(cekOrdinal);
                    // cekEntry will be null if none of the parameters are encrypted.
                    if ((null != cekEntry) && (cekList.size() < cekOrdinal)) {
                        MessageFormat form = new MessageFormat(SQLServerException.getErrString("R_InvalidEncryptionKeyOrdinal"));
                        Object[] msgArgs = { cekOrdinal, cekEntry.getSize() };
                        throw new SQLServerException(this, form.format(msgArgs), null, 0, false);
                    }
                    SQLServerEncryptionType encType = SQLServerEncryptionType.of((byte) secondRs.getInt(DescribeParameterEncryptionResultSet2.ColumnEncrytionType.value()));
                    if (SQLServerEncryptionType.PlainText != encType) {
                        params[paramIndex].cryptoMeta = new CryptoMetadata(cekEntry, (short) cekOrdinal, (byte) secondRs.getInt(DescribeParameterEncryptionResultSet2.ColumnEncryptionAlgorithm.value()), null, encType.value, (byte) secondRs.getInt(DescribeParameterEncryptionResultSet2.NormalizationRuleVersion.value()));
                        // Decrypt the symmetric key.(This will also validate and throw if needed).
                        SQLServerSecurityUtility.decryptSymmetricKey(params[paramIndex].cryptoMeta, connection);
                    } else {
                        if (params[paramIndex].getForceEncryption()) {
                            MessageFormat form = new MessageFormat(SQLServerException.getErrString("R_ForceEncryptionTrue_HonorAETrue_UnencryptedColumn"));
                            Object[] msgArgs = { userSQL, paramIndex + 1 };
                            SQLServerException.makeFromDriverError(connection, this, form.format(msgArgs), null, true);
                        }
                    }
                }
                if (getStatementLogger().isLoggable(java.util.logging.Level.FINE)) {
                    getStatementLogger().fine("Parameter encryption metadata is set.");
                }
            }
            if (paramCount != params.length) {
                // Encryption metadata wasn't sent by the server.
                // We expect the metadata to be sent for all the parameters in the original
                // sp_describe_parameter_encryption.
                // For parameters that don't need encryption, the encryption type is set to plaintext.
                MessageFormat form = new MessageFormat(SQLServerException.getErrString("R_MissingParamEncryptionMetadata"));
                Object[] msgArgs = { userSQL };
                throw new SQLServerException(this, form.format(msgArgs), null, 0, false);
            }
        }
    } catch (SQLException e) {
        if (e instanceof SQLServerException) {
            throw (SQLServerException) e;
        } else {
            throw new SQLServerException(SQLServerException.getErrString("R_UnableRetrieveParameterMetadata"), null, 0, e);
        }
    }
    connection.resetCurrentCommand();
}