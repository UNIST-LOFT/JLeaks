public void loadPropertyFile(String file, Properties target){
    try {
        // Use SecuritySupport class to provide privileged access to property file
        InputStream is = SecuritySupport.getResourceAsStream(XSLT_PROPERTIES);
        // get a buffered version
        try (BufferedInputStream bis = new BufferedInputStream(is)) {
            // and load up the property bag from this
            target.load(bis);
        }
    } catch (Exception ex) {
        // ex.printStackTrace();
        throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(ex);
    }
}