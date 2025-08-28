public static List<String> getFileNameByPackageName(Context context, String packageName) throws PackageManager.NameNotFoundException, IOException 
{
    List<String> classNames = new ArrayList<>();
    for (String path : getSourcePaths(context)) {
        DexFile dexfile = null;
        try {
            if (path.endsWith(EXTRACTED_SUFFIX)) {
                // NOT use new DexFile(path), because it will throw "permission error in /data/dalvik-cache"
                dexfile = DexFile.loadDex(path, path + ".tmp", 0);
            } else {
                dexfile = new DexFile(path);
            }
            Enumeration<String> dexEntries = dexfile.entries();
            while (dexEntries.hasMoreElements()) {
                String className = dexEntries.nextElement();
                if (className.contains(packageName)) {
                    classNames.add(className);
                }
            }
        } catch (Throwable ignore) {
            Log.e("ARouter", "Scan map file in dex files made error.", ignore);
        } finally {
            if (null != dexfile) {
                try {
                    dexfile.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }
    Log.d("ARouter", "Filter " + classNames.size() + " classes by packageName <" + packageName + ">");
    return classNames;
}