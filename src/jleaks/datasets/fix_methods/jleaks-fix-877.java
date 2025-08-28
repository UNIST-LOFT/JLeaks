static Object find(final String factoryId, final String fallbackClassName) throws ClassNotFoundException 
{
    ClassLoader classLoader = getContextClassLoader();
    String serviceId = "META-INF/services/" + factoryId;
    BufferedReader rd = null;
    InputStream is = null;
    // try to find services in CLASSPATH
    try {
        if (classLoader == null) {
            is = ClassLoader.getSystemResourceAsStream(serviceId);
        } else {
            is = classLoader.getResourceAsStream(serviceId);
        }
        if (is != null) {
            rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String factoryClassName = rd.readLine();
            if (factoryClassName != null && !"".equals(factoryClassName)) {
                return newInstance(factoryClassName, classLoader);
            }
        }
    } catch (Exception ex) {
        LOGGER.log(Level.FINER, "Failed to load service " + factoryId + " from " + serviceId, ex);
    } finally {
        try {
            if (rd != null)
                rd.close();
            if (is != null)
                is.close();
        } catch (IOException ex) {
            LOGGER.log(Level.FINER, "Failed to close  BufferedReader/InputStream.", ex);
        }
    }
    FileInputStream fis = null;
    // try to read from $java.home/lib/jaxrs.properties
    try {
        String javah = System.getProperty("java.home");
        String configFile = javah + File.separator + "lib" + File.separator + "jaxrs.properties";
        File f = new File(configFile);
        if (f.exists()) {
            Properties props = new Properties();
            fis = new FileInputStream(f);
            props.load(fis);
            String factoryClassName = props.getProperty(factoryId);
            return newInstance(factoryClassName, classLoader);
        }
    } catch (Exception ex) {
        LOGGER.log(Level.FINER, "Failed to load service " + factoryId + " from $java.home/lib/jaxrs.properties", ex);
    } finally {
        try {
            if (fis != null)
                fis.close();
        } catch (IOException ex) {
            LOGGER.log(Level.FINER, "Failed to close  FileInputStream.", ex);
        }
    }
    // Use the system property
    try {
        String systemProp = System.getProperty(factoryId);
        if (systemProp != null) {
            return newInstance(systemProp, classLoader);
        }
    } catch (SecurityException se) {
        LOGGER.log(Level.FINER, "Failed to load service " + factoryId + " from a system property", se);
    }
    ClassLoader moduleClassLoader = getModuleClassLoader();
    rd = null;
    is = null;
    if (moduleClassLoader != null) {
        try {
            is = moduleClassLoader.getResourceAsStream(serviceId);
            if (is != null) {
                rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String factoryClassName = rd.readLine();
                if (factoryClassName != null && !"".equals(factoryClassName)) {
                    return newInstance(factoryClassName, moduleClassLoader);
                }
            }
        } catch (Exception ex) {
        } finally {
            try {
                if (rd != null)
                    rd.close();
                if (is != null)
                    is.close();
            } catch (IOException ex) {
                LOGGER.log(Level.FINER, "Failed to close  BufferedReader/InputStream.", ex);
            }
        }
    }
    if (fallbackClassName == null) {
        throw new ClassNotFoundException("Provider for " + factoryId + " cannot be found", null);
    }
    return newInstance(fallbackClassName, classLoader);
}