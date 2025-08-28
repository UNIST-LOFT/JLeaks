private void process(String urlSource, String urlTarget,
String user, String password, String serverList) throws SQLException {
    Connection connSource = null, connTarget = null;
    Statement statSource = null, statTarget = null;
    try {
        org.h2.Driver.load();
        // verify that the database doesn't exist,
        // or if it exists (an old cluster instance), it is deleted
        boolean exists = true;
        try {
            connTarget = DriverManager.getConnection(urlTarget + ";IFEXISTS=TRUE;CLUSTER=" + Constants.CLUSTERING_ENABLED, user, password);
            Statement stat = connTarget.createStatement();
            stat.execute("DROP ALL OBJECTS DELETE FILES");
            stat.close();
            exists = false;
            connTarget.close();
        } catch (SQLException e) {
            if (e.getErrorCode() == ErrorCode.DATABASE_NOT_FOUND_1) {
                // database does not exists yet - ok
                exists = false;
            } else {
                throw e;
            }
        }
        if (exists) {
            throw new SQLException("Target database must not yet exist. Please delete it first: " + urlTarget);
        }
        // use cluster='' so connecting is possible
        // even if the cluster is enabled
        connSource = DriverManager.getConnection(urlSource + ";CLUSTER=''", user, password);
        statSource = connSource.createStatement();
        // enable the exclusive mode and close other connections,
        // so that data can't change while restoring the second database
        statSource.execute("SET EXCLUSIVE 2");
        // Pipe reader should be declared outside the try block to be visible in finally{}.
        // It can be safely initialized here as it throws no exceptions.
        PipedReader pipeReader = new PipedReader();
        try {
            // Pipe writer is used + closed in the inner class, in a separate thread (needs to be final).
            // It should be initialized within try{} so an exception could be caught if creation fails.
            // In that scenario, the the writer should be null and needs no closing,
            // and the main goal is that finally{} should bring the source DB
            // out of exclusive mode, and close the reader.
            final PipedWriter pipeWriter = new PipedWriter(pipeReader);
            // Backup data from source database in script form.
            // Start writing to pipe writer in separate thread.
            final ResultSet rs = statSource.executeQuery("SCRIPT");
            // Delete the target database first.
            connTarget = DriverManager.getConnection(urlTarget + ";CLUSTER=''", user, password);
            statTarget = connTarget.createStatement();
            statTarget.execute("DROP ALL OBJECTS DELETE FILES");
            connTarget.close();
            new Thread(new Runnable() {

                public void run() {
                    try {
                        while (rs.next()) {
                            pipeWriter.write(rs.getString(1) + "\n");
                        }
                    } catch (Exception eScript) {
                        throw new IllegalStateException("Producing script from the source DB is failing.", eScript);
                    } finally {
                        try {
                            pipeWriter.close();
                        } catch (IOException eCloseWriter) {
                            throw new IllegalStateException("Closing the pipe writer failed.", eCloseWriter);
                        }
                    }
                }
            }).start();
            // Read data from pipe reader, restore on target.
            connTarget = DriverManager.getConnection(urlTarget, user, password);
            RunScript runScript = new RunScript();
            runScript.setOut(out);
            runScript.execute(connTarget, pipeReader);
            statTarget = connTarget.createStatement();
            // set the cluster to the serverList on both databases
            statSource.executeUpdate("SET CLUSTER '" + serverList + "'");
            statTarget.executeUpdate("SET CLUSTER '" + serverList + "'");
        } catch (IOException eAttach) {
            throw new IllegalStateException("Failed attaching pipe writer to pipe reader.", eAttach);
        } finally {
            // switch back to the regular mode
            statSource.execute("SET EXCLUSIVE FALSE");
            try {
                pipeReader.close();
            } catch (Exception eCloseReader) {
                throw new IllegalStateException("Failed closing the pipe reader.", eCloseReader);
            }
        }
    } finally {
        JdbcUtils.closeSilently(statSource);
        JdbcUtils.closeSilently(statTarget);
        JdbcUtils.closeSilently(connSource);
        JdbcUtils.closeSilently(connTarget);
    }
}