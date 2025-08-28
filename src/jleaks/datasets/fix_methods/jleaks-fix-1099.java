private FsPermission loadFromPath(Path p, char[] password)
      throws IOException, NoSuchAlgorithmException, CertificateException {
    try (FSDataInputStream in = fs.open(p)) {
      FileStatus s = fs.getFileStatus(p);
      keyStore.load(in, password);
      return s.getPermission();
    }
  }