    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        File configFile = PropertiesUtil.findConfigFile(configuration);
        final Properties configProps = new Properties();
        try {
            configProps.load(new FileInputStream(configFile));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ConfigurationException(ex.getMessage());
        }
        _mgmt_cidr = configProps.getProperty("management.cidr");
        _mgmt_gateway = configProps.getProperty("management.gateway");
        s_logger.info("Management network " + _mgmt_cidr + " gateway: " + _mgmt_gateway);
        return true;
    }
