public static void cacheBytecode(byte[] byteCode, String name, String source) 
{
    try {
        if (!Play.initialized || Play.tmpDir == null || Play.readOnlyTmp || !Play.configuration.getProperty("play.bytecodeCache", "true").equals("true")) {
            return;
        }
        File f = cacheFile(name.replace("/", "_").replace("{", "_").replace("}", "_").replace(":", "_"));
        FileOutputStream fos = new FileOutputStream(f);
        try {
            fos.write(hash(source).getBytes("utf-8"));
            fos.write(0);
            fos.write(byteCode);
        } finally {
            fos.close();
        }
        // emit bytecode to standard class layout as well
        if (!name.contains("/") && !name.contains("{")) {
            f = new File(Play.tmpDir, "classes/" + name.replace(".", "/") + ".class");
            f.getParentFile().mkdirs();
            writeByteArrayToFile(f, byteCode);
        }
        if (Logger.isTraceEnabled()) {
            Logger.trace("%s cached", name);
        }
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}