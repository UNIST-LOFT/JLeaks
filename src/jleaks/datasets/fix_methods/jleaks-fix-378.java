public void load(File file) throws IOException 
{
    if (file.isDirectory()) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                recurse(rootDirectory, child, files, templateDirectory);
            }
        }
    } else {
        if (file.getName().endsWith(".jar")) {
            try (JarInputStream stream = new JarInputStream(new FileInputStream(file))) {
                processJar(stream, files, templateDirectory);
            }
        } else {
            String rootPath = rootDirectory.getAbsolutePath();
            String filePath = file.getAbsolutePath();
            if (filePath.contains(templateDirectory) && !rootPath.equals(filePath) && isTemplateFile(filePath)) {
                files.add(new URL("file:" + filePath));
            }
        }
    }
}