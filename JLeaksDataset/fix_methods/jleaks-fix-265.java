public void addPackage(String packageName, boolean recursive) throws IOException, ClassNotFoundException, NoClassDefFoundError 
{
    String[] paths = loader.getPaths();
    final String packagePath = packageName != null && packageName.length() > 0 ? (packageName.replace('.', '/') + "/") : packageName;
    int prevSize = classes.size();
    for (String p : paths) {
        File file = new File(p);
        if (file.isDirectory()) {
            addMatchingDir(null, file, packagePath, recursive);
        } else {
            try (JarFile jarFile = new JarFile(file)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    long entrySize = entry.getSize();
                    long entryTimestamp = entry.getTime();
                    if (entrySize > 0) {
                        try (InputStream is = jarFile.getInputStream(entry)) {
                            byte[] data = new byte[(int) entrySize];
                            int i = 0;
                            while (i < data.length) {
                                int n = is.read(data, i, data.length - i);
                                if (n < 0) {
                                    break;
                                }
                                i += n;
                            }
                            addMatchingFile(entryName, packagePath, recursive, data);
                        }
                    }
                }
            }
        }
    }
    if (classes.size() == 0 && (packageName == null || packageName.length() == 0)) {
        logger.warn("No classes found in the unnamed package");
    } else if (prevSize == classes.size() && packageName != null) {
        logger.warn("No classes found in package " + packageName);
    }
}