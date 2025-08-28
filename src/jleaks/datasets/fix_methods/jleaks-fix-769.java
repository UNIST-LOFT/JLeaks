public void init(PluginManager pluginManager) throws PluginException {
    PluginContext pluginContext = pluginManager.getPluginContext(this);
    try {
        InputStream inputStream = pluginContext.getResourceAsInputStream("schema/IFC2X3_TC1.exp");
        try {
            if (!pluginManager.getTempDir().exists()) {
                pluginManager.getTempDir().mkdir();
            }
            schemaFile = new File(pluginManager.getTempDir(), "IFC2X3_TC1.exp");
            FileOutputStream fileOutputStream = new FileOutputStream(schemaFile);
            try {
                IOUtils.copy(inputStream, fileOutputStream);
            } finally {
                fileOutputStream.close();
            }
        } finally {
            inputStream.close();
        }
        schemaDefinition = loadIfcSchema(schemaFile);
        initialized = true;
    } catch (Exception e) {
        throw new PluginException(e);
    }
}