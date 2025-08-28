private ArrayList<byte[]> describeParameterEncryption(SQLServerConnection connection, String userSql,
String preparedTypeDefinitions, Parameter[] params,
ArrayList<String> parameterNames) throws SQLServerException {
    ArrayList<byte[]> enclaveRequestedCEKs = new ArrayList<>();
    try (PreparedStatement stmt = connection.prepareStatement(connection.enclaveEstablished() ? SDPE1 : SDPE2)) {
        try (ResultSet rs = connection.enclaveEstablished() ? executeSDPEv1(stmt, userSql, preparedTypeDefinitions) : executeSDPEv2(stmt, userSql, preparedTypeDefinitions, aasParams)) {
            if (null == rs) {
                // No results. Meaning no parameter.
                // Should never happen.
                return enclaveRequestedCEKs;
            }
            processSDPEv1(userSql, preparedTypeDefinitions, params, parameterNames, connection, stmt, rs, enclaveRequestedCEKs);
            // Process the third resultset.
            if (connection.isAEv2() && stmt.getMoreResults()) {
                try (ResultSet hgsRs = (SQLServerResultSet) stmt.getResultSet()) {
                    if (hgsRs.next()) {
                        hgsResponse = new AASAttestationResponse(hgsRs.getBytes(1));
                        // This validates and establishes the enclave session if valid
                        validateAttestationResponse();
                    } else {
                        SQLServerException.makeFromDriverError(null, this, SQLServerException.getErrString("R_UnableRetrieveParameterMetadata"), "0", false);
                    }
                }
            }
        }
    } catch (SQLException | IOException e) {
        if (e instanceof SQLServerException) {
            throw (SQLServerException) e;
        } else {
            throw new SQLServerException(SQLServerException.getErrString("R_UnableRetrieveParameterMetadata"), null, 0, e);
        }
    }
    return enclaveRequestedCEKs;
}