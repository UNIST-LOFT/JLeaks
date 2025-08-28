        public List<Class<?>> call() throws Exception {
            try {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.log(Level.FINEST, "Loading providers for SPI: {0}", spi);
                }
                final BufferedReader br = new BufferedReader(new InputStreamReader(spiRegistryUrl.openStream(), "UTF-8"));
                String providerClassName;
                final List<Class<?>> providerClasses = new ArrayList<Class<?>>();
                while ((providerClassName = br.readLine()) != null) {
                    if (providerClassName.trim().length() == 0) {
                        continue;
                    }
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "SPI provider: {0}", providerClassName);
                    }
                    providerClasses.add(loadClass(bundle, providerClassName));
                }
                br.close();
                return providerClasses;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, LocalizationMessages.EXCEPTION_CAUGHT_WHILE_LOADING_SPI_PROVIDERS(), e);
                throw e;
            } catch (Error e) {
                LOGGER.log(Level.WARNING, LocalizationMessages.ERROR_CAUGHT_WHILE_LOADING_SPI_PROVIDERS(), e);
                throw e;
            }
        }
