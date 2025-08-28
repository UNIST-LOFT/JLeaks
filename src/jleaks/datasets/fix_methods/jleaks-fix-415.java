public void setConfiguration( String policyFilePath, String webRootDir ) throws FileNotFoundException 
{
    FileInputStream inputStream = null;
    try {
        inputStream = new FileInputStream(new File(policyFilePath));
        appGuardConfig = ConfigurationParser.readConfigurationFile(inputStream, webRootDir);
        lastConfigReadTime = System.currentTimeMillis();
        configurationFilename = policyFilePath;
    } catch (ConfigurationException e) {
        // TODO: It would be ideal if this method through the
        // ConfigurationException rather than catching it and
        // writing the error to the console.
        e.printStackTrace();
    } finally {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}