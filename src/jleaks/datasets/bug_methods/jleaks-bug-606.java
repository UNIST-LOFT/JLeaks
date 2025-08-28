
    private void extractGzip(File file) throws FileNotFoundException, IOException {
        String originalPath = file.getPath();
        File gzip = new File(originalPath + ".gz");
        if (gzip.isFile()) {
            gzip.delete();
        }
        file.renameTo(gzip);
        file = new File(originalPath);

        byte[] buffer = new byte[4096];

        GZIPInputStream cin = new GZIPInputStream(new FileInputStream(gzip));

        FileOutputStream out = new FileOutputStream(file);

        int len;
        while ((len = cin.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        cin.close();
        out.close();
        FileUtils.delete(gzip);
    }
