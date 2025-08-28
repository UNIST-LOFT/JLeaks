public void scrubRegistrationProperties() 
{
    if (!exist()) {
        return;
    }
    try {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.setIOFactory(new FilteringOutputWriterFactory());
        PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout(config);
        layout.load(reader());
        try (FileWriter out = new FileWriter(this.configFile)) {
            layout.save(out);
        }
        loadProperties();
    } catch (ConfigurationException | IOException e) {
        LOG.warn("[Agent Auto Registration] Unable to scrub registration key.", e);
    }
}