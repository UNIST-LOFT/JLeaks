    public SettingsPersistence(String cfgfile) throws IOException {
        InputStream in = OGAgent.class.getResourceAsStream("oga.properties");
        if (in != null) {
            ogcProperties.load(in);
            in.close();
        }
        if (cfgfile != null) {
            propertyFile = new File(cfgfile);
            FileInputStream is = new FileInputStream(propertyFile);
            ogcProperties.load(is);
            is.close();
            existingSettings = true;
        }
	}
