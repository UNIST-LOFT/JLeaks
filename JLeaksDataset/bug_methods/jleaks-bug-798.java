  private OAuditingHook defaultHook(final ODatabaseInternal iDatabase) {
    final File auditingFileConfig = getConfigFile(iDatabase.getName());
    String content = null;
    if (auditingFileConfig != null && auditingFileConfig.exists()) {
      content = getContent(auditingFileConfig);

    } else {
      final InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_FILE_AUDITING_DB_CONFIG);

      if (resourceAsStream == null)
        OLogManager.instance().error(this, "defaultHook() resourceAsStream is null", null);

      content = getString(resourceAsStream);
      if (auditingFileConfig != null) {
        try {
          auditingFileConfig.getParentFile().mkdirs();
          auditingFileConfig.createNewFile();

          final FileOutputStream f = new FileOutputStream(auditingFileConfig);
          try {
            f.write(content.getBytes());
            f.flush();
          } finally {
            f.close();
          }
        } catch (IOException e) {
          content = "{}";
          OLogManager.instance().error(this, "Cannot save auditing file configuration", e);
        }
      }
    }
    final ODocument cfg = new ODocument().fromJSON(content, "noMap");
    return new OAuditingHook(cfg, server);
  }
