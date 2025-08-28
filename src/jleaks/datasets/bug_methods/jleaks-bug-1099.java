  private FsPermission loadFromPath(Path p, char[] password)
      throws IOException, NoSuchAlgorithmException, CertificateException {
    FileStatus s = fs.getFileStatus(p);
    keyStore.load(fs.open(p), password);
    return s.getPermission();
  }
