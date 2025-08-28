private void extractGzip(File file) throws FileNotFoundException, IOException {
    final String originalPath = file.getPath();
    File gzip = new File(originalPath + ".gz");
    if (gzip.isFile()) {
        gzip.delete();
    }
    if (!file.renameTo(gzip)) {
        throw new IOException("Unable to rename '" + file.getPath() + "'");
    }
    final File newfile = new File(originalPath);
    final byte[] buffer = new byte[4096];
    GZIPInputStream cin = null;
    FileOutputStream out = null;
    try {
        cin = new GZIPInputStream(new FileInputStream(gzip));
        out = new FileOutputStream(newfile);
        int len;
        while ((len = cin.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    } finally {
        if (cin != null) {
            cin.close();
        }
        if (out != null) {
            out.close();
        }
        if (gzip.isFile()) {
            FileUtils.delete(gzip);
        }
    }
}