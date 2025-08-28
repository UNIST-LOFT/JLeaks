public boolean installPlugin(InputStream in, String pluginFilename) 
{
    if (in == null || pluginFilename == null || pluginFilename.length() < 1) {
        Log.error("Error installing plugin: Input stream was null or pluginFilename was null or had no length.");
        return false;
    }
    try {
        byte[] b = new byte[1024];
        int len;
        // If pluginFilename is a path instead of a simple file name, we only want the file name
        int index = pluginFilename.lastIndexOf(File.separator);
        if (index != -1) {
            pluginFilename = pluginFilename.substring(index + 1);
        }
        // Absolute path to the plugin file
        String absolutePath = pluginDirectory + File.separator + pluginFilename;
        // Save input stream contents to a temp file
        try (OutputStream out = new FileOutputStream(absolutePath + ".part")) {
            while ((len = in.read(b)) != -1) {
                // write byte to file
                out.write(b, 0, len);
            }
        }
        // Delete old .jar (if it exists)
        new File(absolutePath).delete();
        // Rename temp file to .jar
        new File(absolutePath + ".part").renameTo(new File(absolutePath));
        // Ask the plugin monitor to update the plugin immediately.
        pluginMonitor.run();
    } catch (IOException e) {
        Log.error("Error installing new version of plugin: " + pluginFilename, e);
        return false;
    }
    return true;
}