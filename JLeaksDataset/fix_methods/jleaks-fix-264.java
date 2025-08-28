public static Properties loadProperties(String name) {
    Properties p = new Properties();
    p.put("platform", name);
    p.put("platform.path.separator", File.pathSeparator);
    String s = System.mapLibraryName("/");
    int i = s.indexOf('/');
    p.put("platform.library.prefix", s.substring(0, i));
    p.put("platform.library.suffix", s.substring(i + 1));
    name = "properties/" + name + ".properties";
    InputStream is = Loader.class.getResourceAsStream(name);
    try {
        try {
            p.load(new InputStreamReader(is));
        } catch (NoSuchMethodError e) {
            p.load(is);
        }
    } catch (Exception e) {
        name = "properties/generic.properties";
        InputStream is2 = Loader.class.getResourceAsStream(name);
        try {
            try {
                p.load(new InputStreamReader(is2));
            } catch (NoSuchMethodError e2) {
                p.load(is2);
            }
        } catch (Exception e2) {
            // give up and return defaults
        } finally {
            try {
                is2.close();
            } catch (IOException ex) { }
        }
    } finally {
        try {
            is.close();
        } catch (IOException ex) { }
    }
    return p;
}