    public Map<TypeDescription, Class<?>> load(ClassLoader classLoader, Map<TypeDescription, byte[]> types) {
        DexProcessor.Conversion conversion = dexProcessor.create();
        for (Map.Entry<TypeDescription, byte[]> entry : types.entrySet()) {
            conversion.register(entry.getKey().getName(), entry.getValue());
        }
        File jar = new File(privateDirectory, randomString.nextString() + JAR_FILE_EXTENSION);
        try {
            if (!jar.createNewFile()) {
                throw new IllegalStateException("Cannot create " + jar);
            }
            JarOutputStream zipOutputStream = new JarOutputStream(new FileOutputStream(jar));
            try {
                zipOutputStream.putNextEntry(new JarEntry(DEX_CLASS_FILE));
                conversion.drainTo(zipOutputStream);
                zipOutputStream.closeEntry();
            } finally {
                zipOutputStream.close();
            }
            return doLoad(classLoader, types.keySet(), jar);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot write to zip file " + jar, exception);
        } finally {
            if (!jar.delete()) {
                Logger.getLogger("net.bytebuddy").warning("Could not delete " + jar);
            }
        }
    }
