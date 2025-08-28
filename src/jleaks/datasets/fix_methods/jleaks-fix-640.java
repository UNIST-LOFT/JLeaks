public static Properties loadProps(String resourceName) 
{
    Properties props = new Properties();
    try (InputStream in = PropertiesLoader.class.getClassLoader().getResourceAsStream(resourceName)) {
        props.load(in);
        in.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
    return props;
}