    public void addPackage(String packageName, boolean recursive) throws IOException, ClassNotFoundException, NoClassDefFoundError {
        String[] paths = loader.getPaths();
        final String packagePath = packageName != null && packageName.length() > 0 ? (packageName.replace('.', '/') + "/") : packageName;
        int prevSize = classes.size();
        for (String p : paths) {
            File file = new File(p);
            if (file.isDirectory()) {
                addMatchingDir(null, file, packagePath, recursive);
            } else {
                JarInputStream jis = new JarInputStream(new FileInputStream(file));
                ZipEntry e = jis.getNextEntry();
                while (e != null) {
                    addMatchingFile(e.getName(), packagePath, recursive);
                    jis.closeEntry();
                    e = jis.getNextEntry();
                }
                jis.close();
            }
        }
        if (classes.size() == 0 && (packageName == null || packageName.length() == 0)) {
            logger.warn("No classes found in the unnamed package");
        } else if (prevSize == classes.size() && packageName != null) {
            logger.warn("No classes found in package " + packageName);
        }
    }
