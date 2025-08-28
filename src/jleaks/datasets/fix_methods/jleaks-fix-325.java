public void save(){
    File storeFile = DBeaverCore.getInstance().getConfigurationFile(CONFIG_FILE_NAME, false);
    try (OutputStream os = new FileOutputStream(storeFile)) {
        XMLBuilder xml = new XMLBuilder(os, GeneralUtils.DEFAULT_FILE_CHARSET_NAME);
        xml.setButify(true);
        xml.startElement("bindings");
        for (Map.Entry<String, ParameterInfo> binding : parameterMap.entrySet()) {
            xml.startElement(TAG_PARAMETER);
            xml.addAttribute(RegistryConstants.ATTR_NAME, binding.getValue().name);
            if (binding.getValue().type != null) {
                xml.addAttribute(RegistryConstants.ATTR_TYPE, binding.getValue().type);
            }
            xml.addText(binding.getValue().value);
            xml.endElement();
        }
        xml.endElement();
        xml.flush();
    } catch (IOException ex) {
        log.warn("IO error", ex);
    }
}