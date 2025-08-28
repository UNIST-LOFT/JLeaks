    private static void updateDigest(final MessageDigest digest, final File sourceFile, final byte[] buffer) throws IOException {
        if (sourceFile.isFile()) {
            InputStream fis = new FileInputStream(sourceFile);
            int numRead;
            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    digest.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
            fis.close();
        } else if (sourceFile.isDirectory()) {
            File[] files = sourceFile.listFiles();
            if (files!=null) {
                for (File file : files) {
                    updateDigest(digest, file, buffer);
                }
            }
        }
    }
