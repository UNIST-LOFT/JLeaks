    private void copyFilesToFatJar(List<File> libs, List<File> classes, File target) throws IOException {
        File tmpZip = File.createTempFile(target.getName(), null);
        tmpZip.delete();

        // Using Apache commons rename, because renameTo has issues across file systems
        FileUtils.moveFile(target, tmpZip);

        byte[] buffer = new byte[8192];
        ZipInputStream zin = new ZipInputStream(new FileInputStream(tmpZip));
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(target));
        for (ZipEntry ze = zin.getNextEntry(); ze != null; ze = zin.getNextEntry()) {
            if (matchesFatJarEntry(libs, ze.getName(), true) || matchesFatJarEntry(classes, ze.getName(), false)) {
                continue;
            }
            out.putNextEntry(ze);
            for(int read = zin.read(buffer); read > -1; read = zin.read(buffer)){
                out.write(buffer, 0, read);
            }
            out.closeEntry();
        }

        for (File lib : libs) {
            try (InputStream in = new FileInputStream(lib)) {
                out.putNextEntry(createZipEntry(lib, getFatJarFullPath(lib, true)));
                for (int read = in.read(buffer); read > -1; read = in.read(buffer)) {
                    out.write(buffer, 0, read);
                }
                out.closeEntry();
            }
        }
    }
