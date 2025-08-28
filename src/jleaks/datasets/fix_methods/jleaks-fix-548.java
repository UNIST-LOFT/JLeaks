public List<Class<?>> call() throws Exception 
{
    BufferedReader reader = null;
    try {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Loading providers for SPI: {0}", spi);
        }
        reader = new BufferedReader(new InputStreamReader(spiRegistryUrl.openStream(), "UTF-8"));
        String providerClassName;
        final List<Class<?>> providerClasses = new ArrayList<Class<?>>();
        while ((providerClassName = reader.readLine()) != null) {
            if (providerClassName.trim().length() == 0) {
                continue;
            }
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "SPI provider: {0}", providerClassName);
            }
            providerClasses.add(loadClass(bundle, providerClassName));
        }
        return providerClasses;
    } catch (Exception e) {
        LOGGER.log(Level.WARNING, LocalizationMessages.EXCEPTION_CAUGHT_WHILE_LOADING_SPI_PROVIDERS(), e);
        throw e;
    } catch (Error e) {
        LOGGER.log(Level.WARNING, LocalizationMessages.ERROR_CAUGHT_WHILE_LOADING_SPI_PROVIDERS(), e);
        throw e;
    } finally {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ioe) {
                LOGGER.log(Level.FINE, "Error closing SPI registry stream:" + spiRegistryUrl, ioe);
            }
        }
    }
}