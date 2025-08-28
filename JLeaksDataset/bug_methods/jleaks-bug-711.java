    private ArrayList<byte[]> describeParameterEncryption(SQLServerConnection connection, String userSql,
            String preparedTypeDefinitions, Parameter[] params,
            ArrayList<String> parameterNames) throws SQLServerException {
        ArrayList<byte[]> enclaveRequestedCEKs = new ArrayList<>();
        ResultSet rs = null;
        try (PreparedStatement stmt = connection.prepareStatement(connection.enclaveEstablished() ? SDPE1 : SDPE2)) {
            if (connection.enclaveEstablished()) {
                rs = executeSDPEv1(stmt, userSql, preparedTypeDefinitions);
            } else {
                rs = executeSDPEv2(stmt, userSql, preparedTypeDefinitions, vsmParams);
            }
            if (null == rs) {
                // No results. Meaning no parameter.
                // Should never happen.
                return enclaveRequestedCEKs;
            }
            processSDPEv1(userSql, preparedTypeDefinitions, params, parameterNames, connection, stmt, rs,
                    enclaveRequestedCEKs);
            // Process the third resultset.
            if (connection.isAEv2() && stmt.getMoreResults()) {
                rs = (SQLServerResultSet) stmt.getResultSet();
                while (rs.next()) {
                    hgsResponse = new VSMAttestationResponse(rs.getBytes(1));
                    // This validates and establishes the enclave session if valid
                    if (!connection.enclaveEstablished()) {
                        hgsResponse = validateAttestationResponse(hgsResponse);
                    }
                }
            }
            // Null check for rs is done already.
            rs.close();
        } catch (SQLException | IOException e) {
            if (e instanceof SQLServerException) {
                throw (SQLServerException) e;
            } else {
                throw new SQLServerException(SQLServerException.getErrString("R_UnableRetrieveParameterMetadata"), null,
                        0, e);
            }
        }
        return enclaveRequestedCEKs;
    }
