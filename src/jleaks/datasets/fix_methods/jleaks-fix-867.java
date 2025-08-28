private void addArchive(String path, File file, int type, boolean isOwn) throws IOException 
{
    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    ZipFile archive = type == ContextUnit.TYPE_JAR ? new JarFile(file) : new ZipFile(file);
    try {
        Enumeration<? extends ZipEntry> entries = archive.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            ContextUnit unit = units.get(path + "/" + file.getName());
            if (unit == null) {
                unit = new ContextUnit(type, path, file.getName(), isOwn, saver, decompiledData);
                if (type == ContextUnit.TYPE_JAR) {
                    unit.setManifest(((JarFile) archive).getManifest());
                }
                units.put(path + "/" + file.getName(), unit);
            }
            String name = entry.getName();
            if (!entry.isDirectory()) {
                if (name.endsWith(".class")) {
                    byte[] bytes = InterpreterUtil.getBytes(archive, entry);
                    StructClass cl = new StructClass(bytes, isOwn, loader);
                    classes.put(cl.qualifiedName, cl);
                    unit.addClass(cl, name);
                    loader.addClassLink(cl.qualifiedName, new LazyLoader.Link(LazyLoader.Link.ENTRY, file.getAbsolutePath(), name));
                } else {
                    unit.addOtherEntry(file.getAbsolutePath(), name);
                }
            } else {
                unit.addDirEntry(name);
            }
        }
    } finally {
        archive.close();
    }
}