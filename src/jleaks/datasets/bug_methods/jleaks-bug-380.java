    private static void recurse(File rootDirectory, File file, List<URL> files, String templateDirectory) throws IOException
    {
        if (file.isDirectory())
        {
            File[] children = file.listFiles();
            if (children != null)
            {
                for (File child : children)
                {
                    recurse(rootDirectory, child, files, templateDirectory);
                }
            }
        }
        else
        {
            if (file.getName().endsWith(".jar"))
            {
                JarInputStream stream = new JarInputStream(new FileInputStream(file));
                processJar(stream, files, templateDirectory);
                stream.close();
            }
            else
            {
                String rootPath = rootDirectory.getAbsolutePath();
                String filePath = file.getAbsolutePath();
                if (filePath.contains(templateDirectory) && !rootPath.equals(filePath) && isTemplateFile(filePath))
                {
                    files.add(new URL("file:" + filePath));
                }
            }
        }
    }
