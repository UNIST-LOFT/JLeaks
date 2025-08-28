public byte[] enhance() 
{
    this.enhancedByteCode = this.javaByteCode;
    if (isClass()) {
        // before we can start enhancing this class we must make sure it is not a PlayPlugin.
        // PlayPlugins can be included as regular java files in a Play-application.
        // If a PlayPlugin is present in the application, it is loaded when other plugins are loaded.
        // All plugins must be loaded before we can start enhancing.
        // This is a problem when loading PlayPlugins bundled as regular app-class since it uses the same classloader
        // as the other (soon to be) enhanched play-app-classes.
        boolean shouldEnhance = true;
        try {
            CtClass ctClass = enhanceChecker_classPool.makeClass(new ByteArrayInputStream(this.enhancedByteCode));
            if (ctClass.subclassOf(ctPlayPluginClass)) {
                shouldEnhance = false;
            }
        } catch (Exception e) {
            // nop
        }
        if (shouldEnhance) {
            Play.pluginCollection.enhance(this);
        }
    }
    if (System.getProperty("precompile") != null) {
        try {
            // emit bytecode to standard class layout as well
            File f = Play.getFile("precompiled/java/" + (name.replace(".", "/")) + ".class");
            f.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(f);
            try {
                fos.write(this.enhancedByteCode);
            } finally {
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    return this.enhancedByteCode;
}