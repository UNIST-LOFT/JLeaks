    public SettingsPersistence(String cfgfile) throws IOException {
        InputStream in=null;
        try {
        in = OGAgent.class.getResourceAsStream("oga.properties");
        if (in != null) {
            ogcProperties.load(in);
            in.close();
            }
        } catch (IOException ioe) {
          throw ioe; //do we need to propagate this up ?
        } finally {
            if (in != null) {
                in.close(); // this will be just thrown up
            }
        }

        if (cfgfile != null) {
            propertyFile = new File(cfgfile);
            FileInputStream is=null;
            try {
            is = new FileInputStream(propertyFile);
            ogcProperties.load(is);
            } catch (IOException ioe) {
              throw ioe; //do we need to propagate this up ?
            } finally {
              try {
              if (is != null) {
                  is.close();
              }
              } catch (IOException ioe) {
                throw ioe; 
              } finally {
              existingSettings = true;
              }
            }
        }
	}
