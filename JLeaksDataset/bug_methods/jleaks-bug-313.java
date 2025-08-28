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
                while (resources.hasMore()) {
                    Properties props = new Properties();
                    props.load(resources.next());

                    if (result == null) {
                        result = props;
                    } else {
                        mergeTables(result, props);
                    }
                }

                // Merge in properties from file in <java.home>/lib.
                InputStream istream =
                    helper.getJavaHomeLibStream(JRELIB_PROPERTY_FILE_NAME);
                if (istream != null) {
                    Properties props = new Properties();
                    props.load(istream);

                    if (result == null) {
                        result = props;
                    } else {
                        mergeTables(result, props);
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
