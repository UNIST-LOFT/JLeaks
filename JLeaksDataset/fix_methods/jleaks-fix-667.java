private String extractLibrary (String sharedLibName) 
{
    String srcCrc = crc(JniGenSharedLibraryLoader.class.getResourceAsStream("/" + sharedLibName));
    File nativesDir = new File(System.getProperty("java.io.tmpdir") + "/jnigen/" + srcCrc);
    File nativeFile = new File(nativesDir, sharedLibName);
    String extractedCrc = null;
    if (nativeFile.exists()) {
        try {
            extractedCrc = crc(new FileInputStream(nativeFile));
        } catch (FileNotFoundException ignored) {
        }
    }
    if (extractedCrc == null || !extractedCrc.equals(srcCrc)) {
        InputStream input = null;
        ZipFile file = null;
        FileOutputStream output = null;
        try {
            // Extract native from classpath to temp dir.
            if (nativesJar == null)
                input = JniGenSharedLibraryLoader.class.getResourceAsStream("/" + sharedLibName);
            else {
                file = new ZipFile(nativesJar);
                ZipEntry entry = file.getEntry(sharedLibName);
                input = file.getInputStream(entry);
            }
            if (input == null)
                return null;
            nativeFile.getParentFile().mkdirs();
            output = new FileOutputStream(nativeFile);
            byte[] buffer = new byte[4096];
            while (true) {
                int length = input.read(buffer);
                if (length == -1)
                    break;
                output.write(buffer, 0, length);
            }
            input.close();
            output.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } finally {
            try {
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }
            try {
                if (file != null)
                    file.close();
            } catch (IOException ignored) {
            }
            try {
                if (output != null)
                    output.close();
            } catch (IOException ignored) {
            }
        }
    }
    return nativeFile.exists() ? nativeFile.getAbsolutePath() : null;
}