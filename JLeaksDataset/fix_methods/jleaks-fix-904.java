public Map<String, JsonFile> unzip(File extractionDirectory) throws IOException 
{
    try (FileInputStream fin = new FileInputStream(zipFilePath);
        ZipInputStream zipInputStream = new ZipInputStream(fin)) {
        ZipEntry zipEntry;
        Map<String, JsonFile> result = new HashMap<>();
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            Log.v(TAG, "Unzipping " + zipEntry.getName());
            if (zipEntry.isDirectory()) {
                throw new IllegalStateException("The zip file should not contain any directories.");
            } else {
                if (zipEntry.getName().toLowerCase().endsWith(".json")) {
                    if (zipEntry.getSize() < 100_000) {
                        ByteArrayOutputStream sw = new ByteArrayOutputStream();
                        copyStream(zipInputStream, sw);
                        result.put(zipEntry.getName().toLowerCase(), new JsonFile(zipEntry.getName(), new String(sw.toByteArray(), Charsets.UTF_8)));
                    }
                } else {
                    FileOutputStream fout = new FileOutputStream(new File(extractionDirectory, zipEntry.getName()));
                    copyStream(zipInputStream, fout);
                }
            }
        }
        return result;
    }
}