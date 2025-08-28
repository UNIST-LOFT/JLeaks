public boolean check() 
{
    if (file == null) {
        throw new NullPointerException("file must not be null");
    } else {
        LOGGER.log(Level.INFO, "loading properties from file {0}", file.getAbsolutePath());
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(file);
            properties.load(inStream);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "cannot load properties from file {0}: {1}", new Object[] { file.getAbsolutePath(), e.getMessage() });
        } finally {
            closeStream(inStream);
        }
    }
}