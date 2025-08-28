public List<String> getClassesUnder(String aPath) 
{
    List<String> classes = new ArrayList<String>();
    ClassSourceType cst = getClassSourceType(aPath);
    // Get the dex file from an apk
    if (cst == ClassSourceType.apk) {
        ZipFile archive = null;
        try {
            archive = new ZipFile(aPath);
            for (Enumeration<? extends ZipEntry> entries = archive.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                // We are dealing with an apk file
                if (entryName.endsWith(".dex"))
                    classes.addAll(DexClassProvider.classesOfDex(new File(aPath)));
            }
        } catch (IOException e) {
            throw new CompilationDeathException("Error reasing archive '" + aPath + "'", e);
        } finally {
            try {
                if (archive != null)
                    archive.close();
            } catch (Throwable t) {
            }
        }
    } else // Directly load a dex file
    if (cst == ClassSourceType.dex) {
        try {
            classes.addAll(DexClassProvider.classesOfDex(new File(aPath)));
        } catch (IOException e) {
            throw new CompilationDeathException("Error reasing '" + aPath + "'", e);
        }
    } else // load Java class files from ZIP and JAR
    if (cst == ClassSourceType.jar || cst == ClassSourceType.zip) {
        Set<String> dexEntryNames = new HashSet<String>();
        ZipFile archive = null;
        try {
            archive = new ZipFile(aPath);
            for (Enumeration<? extends ZipEntry> entries = archive.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.endsWith(".class") || entryName.endsWith(".jimple")) {
                    int extensionIndex = entryName.lastIndexOf('.');
                    entryName = entryName.substring(0, extensionIndex);
                    entryName = entryName.replace('/', '.');
                    classes.add(entryName);
                } else if (entryName.endsWith(".dex")) {
                    dexEntryNames.add(entryName);
                }
            }
        } catch (Throwable e) {
            throw new CompilationDeathException("Error reading archive '" + aPath + "'", e);
        } finally {
            try {
                if (archive != null)
                    archive.close();
            } catch (Throwable t) {
            }
        }
        if (!dexEntryNames.isEmpty()) {
            File file = new File(aPath);
            if (Options.v().process_multiple_dex()) {
                for (String dexEntryName : dexEntryNames) {
                    try {
                        classes.addAll(DexClassProvider.classesOfDex(file, dexEntryName));
                    } catch (Throwable e) {
                    }
                    /* Ignore unreadable files */
                }
            } else {
                try {
                    classes.addAll(DexClassProvider.classesOfDex(file));
                } catch (Throwable e) {
                }
                /* Ignore unreadable files */
            }
        }
    } else if (cst == ClassSourceType.directory) {
        File file = new File(aPath);
        File[] files = file.listFiles();
        if (files == null) {
            files = new File[1];
            files[0] = file;
        }
        for (File element : files) {
            if (element.isDirectory()) {
                List<String> list = getClassesUnder(aPath + File.separatorChar + element.getName());
                for (String s : list) {
                    classes.add(element.getName() + "." + s);
                }
            } else {
                String fileName = element.getName();
                if (fileName.endsWith(".class")) {
                    int index = fileName.lastIndexOf(".class");
                    classes.add(fileName.substring(0, index));
                } else if (fileName.endsWith(".jimple")) {
                    int index = fileName.lastIndexOf(".jimple");
                    classes.add(fileName.substring(0, index));
                } else if (fileName.endsWith(".java")) {
                    int index = fileName.lastIndexOf(".java");
                    classes.add(fileName.substring(0, index));
                } else if (fileName.endsWith(".dex")) {
                    try {
                        classes.addAll(DexClassProvider.classesOfDex(element));
                    } catch (IOException e) {
                        /* Ignore unreadable files */
                    }
                }
            }
        }
    } else
        throw new RuntimeException("Invalid class source type");
    return classes;
}