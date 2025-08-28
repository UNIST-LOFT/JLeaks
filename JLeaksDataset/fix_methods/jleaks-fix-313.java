    private static Hashtable<? super String, Object> getApplicationResources()
            throws NamingException {

        ClassLoader cl = helper.getContextClassLoader();

        synchronized (propertiesCache) {
            Hashtable<? super String, Object> result = propertiesCache.get(cl);
            if (result != null) {
                return result;
            }

            try {
                NamingEnumeration<InputStream> resources =
                    helper.getResources(cl, APP_RESOURCE_FILE_NAME);
                try {
                    while (resources.hasMore()) {
                        Properties props = new Properties();
                        InputStream istream = resources.next();
                        try {
                            props.load(istream);
                        } finally {
                            istream.close();
                        }

                        if (result == null) {
                            result = props;
                        } else {
                            mergeTables(result, props);
                        }
                    }
                } finally {
                    while (resources.hasMore()) {
                        InputStream istream = (InputStream)resources.next();
                        istream.close();
                    }
                }

                // Merge in properties from file in <java.home>/lib.
                InputStream istream =
                    helper.getJavaHomeLibStream(JRELIB_PROPERTY_FILE_NAME);
                if (istream != null) {
                    try {
                        Properties props = new Properties();
                        props.load(istream);

                        if (result == null) {
                            result = props;
                        } else {
                            mergeTables(result, props);
                        }
                    } finally {
                        istream.close();
                    }
                }

            } catch (IOException e) {
                NamingException ne = new ConfigurationException(
                        "Error reading application resource file");
                ne.setRootCause(e);
                throw ne;
            }
            if (result == null) {
                result = new Hashtable<>(11);
            }
            propertiesCache.put(cl, result);
            return result;
        }
    }
