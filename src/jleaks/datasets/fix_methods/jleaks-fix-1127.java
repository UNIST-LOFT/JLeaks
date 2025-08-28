public void setup(final ProcessContext context) {
        String configBody = context.getProperty(MIME_CONFIG_BODY).getValue();
        String configFile = context.getProperty(MIME_CONFIG_FILE).evaluateAttributeExpressions().getValue();
        if (configBody == null && configFile == null){
            this.detector = config.getDetector();
            this.mimeTypes = config.getMimeRepository();
        } else if (configBody != null) {
            try {
                this.detector = MimeTypesFactory.create(new ByteArrayInputStream(configBody.getBytes()));
                this.mimeTypes = (MimeTypes)this.detector;
            } catch (Exception e) {
                context.yield();
                throw new ProcessException("Failed to load config body", e);
            }

        } else {
            try (final FileInputStream fis = new FileInputStream(configFile);
                 final InputStream bis = new BufferedInputStream(fis)) {
                this.detector = MimeTypesFactory.create(bis);
                this.mimeTypes = (MimeTypes)this.detector;
            } catch (Exception e) {
                context.yield();
                throw new ProcessException("Failed to load config file", e);
            }
        }
    }