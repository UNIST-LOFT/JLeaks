public boolean configure(String name, Map<String, Object> params) throws ConfigurationException 
{
    File configFile = PropertiesUtil.findConfigFile(configuration);
    FileInputStream inputFile = null;
    try {
        if (null == configFile) {
            throw new FileNotFoundException("Configuration file was not found!");
        }
        inputFile = new FileInputStream(configFile);
    } catch (FileNotFoundException e) {
        s_logger.error(e.getMessage());
        throw new ConfigurationException(e.getMessage());
    }
    final Properties configProps = new Properties();
    try {
        configProps.load(inputFile);
    } catch (IOException e) {
        s_logger.error(e.getMessage());
        throw new ConfigurationException(e.getMessage());
    } finally {
        try {
            inputFile.close();
        } catch (IOException e) {
        }
    }
    _mgmt_cidr = configProps.getProperty("management.cidr");
    _mgmt_gateway = configProps.getProperty("management.gateway");
    s_logger.info("Management network " + _mgmt_cidr + " gateway: " + _mgmt_gateway);
    return true;
}