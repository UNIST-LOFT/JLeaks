public void addtoJobConf(JobConf jobconf, CredentialsProperties props, Context context) throws Exception {
        try {
            // load the driver
            Class.forName("org.apache.hive.jdbc.HiveDriver");
            String url = props.getProperties().get(HIVE2_JDBC_URL);
            if (url == null || url.isEmpty()) {
                throw new CredentialException(ErrorCode.E0510,
                        HIVE2_JDBC_URL + " is required to get hive server 2 credential");
            }
            String principal = props.getProperties().get(HIVE2_SERVER_PRINCIPAL);
            if (principal == null || principal.isEmpty()) {
                throw new CredentialException(ErrorCode.E0510,
                        HIVE2_SERVER_PRINCIPAL + " is required to get hive server 2 credential");
            }
            url = url + ";principal=" + principal;
            Connection con = null;
            String tokenStr = null;
            try {
                con = DriverManager.getConnection(url);
                XLog.getLog(getClass()).debug("Connected successfully to " + url);
                // get delegation token for the given proxy user
                tokenStr = ((HiveConnection)con).getDelegationToken(jobconf.get(USER_NAME), principal);
            } finally {
                if (con != null) {
                    con.close();
                }
            }
            XLog.getLog(getClass()).debug("Got token");

            Token<DelegationTokenIdentifier> hive2Token = new Token<DelegationTokenIdentifier>();
            hive2Token.decodeFromUrlString(tokenStr);
            jobconf.getCredentials().addToken(new Text("hive.server2.delegation.token"), hive2Token);
            XLog.getLog(getClass()).debug("Added the Hive Server 2 token in job conf");
        }
        catch (Exception e) {
            XLog.getLog(getClass()).warn("Exception in addtoJobConf", e);
            throw e;
        }
    }