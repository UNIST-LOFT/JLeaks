protected void extractResources(ServletContext context, String path, File toDir) 
{
    for (String resource : context.getResourcePaths(path)) {
        // extract the resource to the directory if it does not exist
        File f = new File(toDir, resource.substring(path.length()));
        if (!f.exists()) {
            InputStream is = null;
            OutputStream os = null;
            try {
                if (resource.charAt(resource.length() - 1) == '/') {
                    // directory
                    f.mkdirs();
                    extractResources(context, resource, f);
                } else {
                    // file
                    f.getParentFile().mkdirs();
                    is = context.getResourceAsStream(resource);
                    os = new FileOutputStream(f);
                    byte[] buffer = new byte[4096];
                    int len = 0;
                    while ((len = is.read(buffer)) > -1) {
                        os.write(buffer, 0, len);
                    }
                }
            } catch (FileNotFoundException e) {
                logger.error("Failed to find resource \"" + resource + "\"", e);
            } catch (IOException e) {
                logger.error("Failed to copy resource \"" + resource + "\" to " + f, e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
    }
}