  private static void ensureDbTablesAreCreated(final Connection conn) {
    synchronized (dbSetupLock) {
      try {
        if (areDbTablesCreated) {
          return;
        }
        final ResultSet rs = conn.getMetaData().getTables(null, null, null, null);
        final List<String> existing = new ArrayList<>();
        while (rs.next()) {
          existing.add(rs.getString("TABLE_NAME").toUpperCase());
        }
        rs.close();
        if (!existing.contains("TA_USERS")) {
          final Statement s = conn.createStatement();
          s.execute("create table ta_users" + "(" + "userName varchar(40) NOT NULL PRIMARY KEY, "
              + "password varchar(40) NOT NULL, " + "email varchar(40) NOT NULL, " + "joined timestamp NOT NULL, "
              + "lastLogin timestamp NOT NULL, " + "admin integer NOT NULL " + ")");
          s.close();
        }
        if (!existing.contains("BANNED_USERNAMES")) {
          final Statement s = conn.createStatement();
          s.execute("create table banned_usernames" + "(" + "username varchar(40) NOT NULL PRIMARY KEY, "
              + "ban_till timestamp  " + ")");
          s.close();
        }
        if (!existing.contains("BANNED_IPS")) {
          final Statement s = conn.createStatement();
          s.execute(
              "create table banned_ips" + "(" + "ip varchar(40) NOT NULL PRIMARY KEY, " + "ban_till timestamp  " + ")");
          s.close();
        }
        if (!existing.contains("BANNED_MACS")) {
          final Statement s = conn.createStatement();
          s.execute("create table banned_macs" + "(" + "mac varchar(40) NOT NULL PRIMARY KEY, " + "ban_till timestamp  "
              + ")");
          s.close();
        }
        if (!existing.contains("MUTED_USERNAMES")) {
          final Statement s = conn.createStatement();
          s.execute("create table muted_usernames" + "(" + "username varchar(40) NOT NULL PRIMARY KEY, "
              + "mute_till timestamp  " + ")");
          s.close();
        }
        if (!existing.contains("MUTED_IPS")) {
          final Statement s = conn.createStatement();
          s.execute(
              "create table muted_ips" + "(" + "ip varchar(40) NOT NULL PRIMARY KEY, " + "mute_till timestamp  " + ")");
          s.close();
        }
        if (!existing.contains("MUTED_MACS")) {
          final Statement s = conn.createStatement();
          s.execute("create table muted_macs" + "(" + "mac varchar(40) NOT NULL PRIMARY KEY, " + "mute_till timestamp  "
              + ")");
          s.close();
        }
        if (!existing.contains("BAD_WORDS")) {
          final Statement s = conn.createStatement();
          s.execute("create table bad_words" + "(" + "word varchar(40) NOT NULL PRIMARY KEY " + ")");
          s.close();
        }
        areDbTablesCreated = true;
      } catch (final SQLException sqle) {
        // only close if an error occurs
        try {
          conn.close();
        } catch (final SQLException e) {
          // ignore close errors
        }
        logger.log(Level.SEVERE, sqle.getMessage(), sqle);
        throw new IllegalStateException("Could not create tables");
      }
    }
  }
