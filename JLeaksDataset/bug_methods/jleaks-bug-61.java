    protected void updateSSLKeystore() {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Processing updateSSLKeyStore");
        }

        String dbString = _configDao.getValue("ssl.keystore");

        File confFile = PropertiesUtil.findConfigFile("db.properties");
        String confPath = null;
        String keystorePath = null;
        File keystoreFile = null;

        if (null != confFile) {
            confPath = confFile.getParent();
            keystorePath = confPath + Link.keystoreFile;
            keystoreFile = new File(keystorePath);
        }

        boolean dbExisted = (dbString != null && !dbString.isEmpty());

        s_logger.info("SSL keystore located at " + keystorePath);
        try {
            if (!dbExisted && null != confFile) {
                if (!keystoreFile.exists()) {
                    generateDefaultKeystore(keystorePath);
                    s_logger.info("Generated SSL keystore.");
                }
                String base64Keystore = getBase64Keystore(keystorePath);
                ConfigurationVO configVO =
                        new ConfigurationVO("Hidden", "DEFAULT", "management-server", "ssl.keystore", base64Keystore,
                                "SSL Keystore for the management servers");
                _configDao.persist(configVO);
                s_logger.info("Stored SSL keystore to database.");
            } else { // !keystoreFile.exists() and dbExisted
                // Export keystore to local file
                byte[] storeBytes = Base64.decodeBase64(dbString);
                try {
                    String tmpKeystorePath = "/tmp/tmpkey";
                    FileOutputStream fo = new FileOutputStream(tmpKeystorePath);
                    fo.write(storeBytes);
                    fo.close();
                    Script script = new Script(true, "cp", 5000, null);
                    script.add("-f");
                    script.add(tmpKeystorePath);

                    //There is a chance, although small, that the keystorePath is null. In that case, do not add it to the script.
                    if (null != keystorePath) {
                        script.add(keystorePath);
                    }
                    String result = script.execute();
                    if (result != null) {
                        throw new IOException();
                    }
                } catch (Exception e) {
                    throw new IOException("Fail to create keystore file!", e);
                }
                s_logger.info("Stored database keystore to local.");
            }
        } catch (Exception ex) {
            s_logger.warn("Would use fail-safe keystore to continue.", ex);
        }
    }
