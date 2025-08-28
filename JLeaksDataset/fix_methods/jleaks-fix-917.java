private void printApk(String outputDir, File originalApk) throws IOException 
{
    ZipOutputStream outputApk = null;
    if (Options.v().output_jar()) {
        outputApk = PackManager.v().getJarFile();
        G.v().out.println("Writing APK to: " + Options.v().output_dir());
    } else {
        String outputFileName = outputDir + File.separatorChar + originalApk.getName();
        File outputFile = new File(outputFileName);
        if (outputFile.exists() && !Options.v().force_overwrite()) {
            throw new CompilationDeathException("Output file " + outputFile + " exists. Not overwriting.");
        }
        outputApk = new ZipOutputStream(new FileOutputStream(outputFile));
        G.v().out.println("Writing APK to: " + outputFileName);
    }
    G.v().out.println("do not forget to sign the .apk file with jarsigner and to align it with zipalign");
    // Copy over additional resources from original APK
    ZipFile original = null;
    try {
        original = new ZipFile(originalApk);
        copyAllButClassesDexAndSigFiles(original, outputApk);
    } finally {
        if (original != null)
            original.close();
    }
    // put our classes.dex into the zip archive
    File tmpFile = File.createTempFile("toDex", null);
    FileInputStream fis = new FileInputStream(tmpFile);
    try {
        outputApk.putNextEntry(new ZipEntry(CLASSES_DEX));
        writeTo(tmpFile.getAbsolutePath());
        while (fis.available() > 0) {
            byte[] data = new byte[fis.available()];
            fis.read(data);
            outputApk.write(data);
        }
        outputApk.closeEntry();
    } finally {
        fis.close();
        tmpFile.delete();
        if (outputApk != null)
            outputApk.close();
    }
}