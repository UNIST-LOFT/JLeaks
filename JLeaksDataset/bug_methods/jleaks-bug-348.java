        private static synchronized KeyManager[] getDefaultKeyManager()
                throws Exception {
            if (defaultKeyManagers != null) {
                return defaultKeyManagers;
            }

            final Map<String,String> props = new HashMap<>();
            AccessController.doPrivileged(
                        new PrivilegedExceptionAction<Object>() {
                public Object run() throws Exception {
                    props.put("keyStore",  System.getProperty(
                                "javax.net.ssl.keyStore", ""));
                    props.put("keyStoreType", System.getProperty(
                                "javax.net.ssl.keyStoreType",
                                KeyStore.getDefaultType()));
                    props.put("keyStoreProvider", System.getProperty(
                                "javax.net.ssl.keyStoreProvider", ""));
                    props.put("keyStorePasswd", System.getProperty(
                                "javax.net.ssl.keyStorePassword", ""));
                    return null;
                }
            });

            final String defaultKeyStore = props.get("keyStore");
            String defaultKeyStoreType = props.get("keyStoreType");
            String defaultKeyStoreProvider = props.get("keyStoreProvider");
            if (debug != null && Debug.isOn("defaultctx")) {
                System.out.println("keyStore is : " + defaultKeyStore);
                System.out.println("keyStore type is : " +
                                        defaultKeyStoreType);
                System.out.println("keyStore provider is : " +
                                        defaultKeyStoreProvider);
            }

            if (P11KEYSTORE.equals(defaultKeyStoreType) &&
                    !NONE.equals(defaultKeyStore)) {
                throw new IllegalArgumentException("if keyStoreType is "
                    + P11KEYSTORE + ", then keyStore must be " + NONE);
            }

            FileInputStream fs = null;
            if (defaultKeyStore.length() != 0 && !NONE.equals(defaultKeyStore)) {
                fs = AccessController.doPrivileged(
                        new PrivilegedExceptionAction<FileInputStream>() {
                    public FileInputStream run() throws Exception {
                        return new FileInputStream(defaultKeyStore);
                    }
                });
            }

            String defaultKeyStorePassword = props.get("keyStorePasswd");
            char[] passwd = null;
            if (defaultKeyStorePassword.length() != 0) {
                passwd = defaultKeyStorePassword.toCharArray();
            }

            /**
             * Try to initialize key store.
             */
            KeyStore ks = null;
            if ((defaultKeyStoreType.length()) != 0) {
                if (debug != null && Debug.isOn("defaultctx")) {
                    System.out.println("init keystore");
                }
                if (defaultKeyStoreProvider.length() == 0) {
                    ks = KeyStore.getInstance(defaultKeyStoreType);
                } else {
                    ks = KeyStore.getInstance(defaultKeyStoreType,
                                        defaultKeyStoreProvider);
                }

                // if defaultKeyStore is NONE, fs will be null
                ks.load(fs, passwd);
            }
            if (fs != null) {
                fs.close();
                fs = null;
            }

            /*
             * Try to initialize key manager.
             */
            if (debug != null && Debug.isOn("defaultctx")) {
                System.out.println("init keymanager of type " +
                    KeyManagerFactory.getDefaultAlgorithm());
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());

            if (P11KEYSTORE.equals(defaultKeyStoreType)) {
                kmf.init(ks, null); // do not pass key passwd if using token
            } else {
                kmf.init(ks, passwd);
            }

            defaultKeyManagers = kmf.getKeyManagers();
            return defaultKeyManagers;
        }
