public static Properties getAssetInfo(MD5Key id) 
{
    File infoFile = getAssetInfoFile(id);
    Properties props = new Properties();
    try (InputStream is = new FileInputStream(infoFile)) {
        props.load(is);
    } catch (IOException ioe) {
        // do nothing
    }
    return props;
}