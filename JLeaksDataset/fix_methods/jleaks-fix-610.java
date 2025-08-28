private static void loadDefaultProperties() 
{
    String fname = StaticProperty.javaHome();
    if (fname == null) {
        throw new Error("Can't find java.home ??");
    }
    try {
        File f = new File(fname, "conf");
        f = new File(f, "net.properties");
        fname = f.getCanonicalPath();
        try (FileInputStream in = new FileInputStream(fname);
            BufferedInputStream bin = new BufferedInputStream(in)) {
            props.load(bin);
        }
    } catch (Exception e) {
        // Do nothing. We couldn't find or access the file
        // so we won't have default properties...
    }
}