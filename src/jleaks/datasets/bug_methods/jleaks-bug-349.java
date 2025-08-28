    static KeyStore getCacertsKeyStore(String dbgname) throws Exception
    {
        String storeFileName = null;
        File storeFile = null;
        FileInputStream fis = null;
        String defaultTrustStoreType;
        String defaultTrustStoreProvider;
        final HashMap<String,String> props = new HashMap<>();
        final String sep = File.separator;
        KeyStore ks = null;

        AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
            public Void run() throws Exception {
                props.put("trustStore", System.getProperty(
                                "javax.net.ssl.trustStore"));
                props.put("javaHome", System.getProperty(
                                        "java.home"));
                props.put("trustStoreType", System.getProperty(
                                "javax.net.ssl.trustStoreType",
                                KeyStore.getDefaultType()));
                props.put("trustStoreProvider", System.getProperty(
                                "javax.net.ssl.trustStoreProvider", ""));
                props.put("trustStorePasswd", System.getProperty(
                                "javax.net.ssl.trustStorePassword", ""));
                return null;
            }
        });

        /*
         * Try:
         *      javax.net.ssl.trustStore  (if this variable exists, stop)
         *      jssecacerts
         *      cacerts
         *
         * If none exists, we use an empty keystore.
         */

        storeFileName = props.get("trustStore");
        if (!"NONE".equals(storeFileName)) {
            if (storeFileName != null) {
                storeFile = new File(storeFileName);
                fis = getFileInputStream(storeFile);
            } else {
                String javaHome = props.get("javaHome");
                storeFile = new File(javaHome + sep + "lib" + sep
                                                + "security" + sep +
                                                "jssecacerts");
                if ((fis = getFileInputStream(storeFile)) == null) {
                    storeFile = new File(javaHome + sep + "lib" + sep
                                                + "security" + sep +
                                                "cacerts");
                    fis = getFileInputStream(storeFile);
                }
            }

            if (fis != null) {
                storeFileName = storeFile.getPath();
            } else {
                storeFileName = "No File Available, using empty keystore.";
            }
        }

        defaultTrustStoreType = props.get("trustStoreType");
        defaultTrustStoreProvider = props.get("trustStoreProvider");
        if (debug != null && Debug.isOn(dbgname)) {
            System.out.println("trustStore is: " + storeFileName);
            System.out.println("trustStore type is : " +
                                defaultTrustStoreType);
            System.out.println("trustStore provider is : " +
                                defaultTrustStoreProvider);
        }

        /*
         * Try to initialize trust store.
         */
        if (defaultTrustStoreType.length() != 0) {
            if (debug != null && Debug.isOn(dbgname)) {
                System.out.println("init truststore");
            }
            if (defaultTrustStoreProvider.length() == 0) {
                ks = KeyStore.getInstance(defaultTrustStoreType);
            } else {
                ks = KeyStore.getInstance(defaultTrustStoreType,
                                        defaultTrustStoreProvider);
            }
            char[] passwd = null;
            String defaultTrustStorePassword = props.get("trustStorePasswd");
            if (defaultTrustStorePassword.length() != 0)
                passwd = defaultTrustStorePassword.toCharArray();

            // if trustStore is NONE, fis will be null
            ks.load(fis, passwd);

            // Zero out the temporary password storage
            if (passwd != null) {
                for (int i = 0; i < passwd.length; i++) {
                    passwd[i] = (char)0;
                }
            }
        }

        if (fis != null) {
            fis.close();
        }

        return ks;
    }
