    public static void extractArchive(File sourceBundle, File root) throws IOException {
        ZipFile zipfile = new ZipFile(sourceBundle);

        Enumeration<? extends ZipEntry> e = zipfile.entries();

        while (e.hasMoreElements()) {
            ZipEntry ze = e.nextElement();
            File file = new File(root, ze.getName());
            if (ze.isDirectory()) {
                file.mkdirs();
            } else {
                InputStream in = zipfile.getInputStream(ze);
                assertNotNull(in);
                FileOutputStream out = new FileOutputStream(file);
                assertNotNull(out);
                copyFile(in, out);
            }
        }
    }
