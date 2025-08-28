public static <T> List<T> loadInstances(ClassLoader classLoader, Class<T> type) throws IOException 
{
    List<T> result = new ArrayList<T>();
    final Enumeration<URL> e = classLoader.getResources("META-INF/services/" + type.getName());
    while (e.hasMoreElements()) {
        URL url = e.nextElement();
        BufferedReader configFile = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
        try {
            String line;
            while ((line = configFile.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || line.length() == 0)
                    continue;
                try {
                    Class<?> t = classLoader.loadClass(line);
                    // invalid type
                    if (!type.isAssignableFrom(t))
                        continue;
                    result.add(type.cast(t.newInstance()));
                } catch (ClassNotFoundException x) {
                    LOGGER.log(WARNING, "Failed to load " + line, x);
                } catch (InstantiationException x) {
                    LOGGER.log(WARNING, "Failed to load " + line, x);
                } catch (IllegalAccessException x) {
                    LOGGER.log(WARNING, "Failed to load " + line, x);
                }
            }
        } finally {
            configFile.close();
        }
    }
    return result;
}