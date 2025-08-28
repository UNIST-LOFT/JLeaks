    private void saveProfiles()
    {
        if (customProfiles == null) {
            return;
        }
        File storeFile = DBeaverCore.getInstance().getConfigurationFile(CONFIG_FILE_NAME, false);
        try {
            OutputStream os = new FileOutputStream(storeFile);
            try {
                XMLBuilder xml = new XMLBuilder(os, GeneralUtils.DEFAULT_FILE_CHARSET_NAME);
                xml.setButify(true);
                xml.startElement(RegistryConstants.TAG_PROFILES);
                for (DBDDataFormatterProfile profile : customProfiles) {
                    xml.startElement(RegistryConstants.TAG_PROFILE);
                    xml.addAttribute(RegistryConstants.ATTR_NAME, profile.getProfileName());
                    SimplePreferenceStore store = (SimplePreferenceStore) profile.getPreferenceStore();
                    Map<String, String> props = store.getProperties();
                    if (props != null) {
                        for (Map.Entry<String,String> entry : props.entrySet()) {
                            xml.startElement(RegistryConstants.TAG_PROPERTY);
                            xml.addAttribute(RegistryConstants.ATTR_NAME, entry.getKey());
                            xml.addAttribute(RegistryConstants.ATTR_VALUE, entry.getValue());
                            xml.endElement();
                        }
                    }
                    xml.endElement();
                }
                xml.endElement();
                xml.flush();
                os.close();
            }
            catch (IOException ex) {
                log.warn("IO error", ex);
            }
        } catch (FileNotFoundException ex) {
            log.warn("Can't open profiles store file " + storeFile.getPath(), ex);
        }
    }
