private JavaKeyStoreProvider(URI uri, Configuration conf) throws IOException {
    this.uri = uri;
    path = ProviderUtils.unnestUri(uri);
    fs = path.getFileSystem(conf);
    // Get the password from the user's environment
    if (System.getenv().containsKey(CREDENTIAL_PASSWORD_NAME)) {
      password = System.getenv(CREDENTIAL_PASSWORD_NAME).toCharArray();
    }
    // if not in ENV get check for file
    if (password == null) {
      String pwFile = conf.get(KEYSTORE_PASSWORD_FILE_KEY);
      if (pwFile != null) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL pwdFile = cl.getResource(pwFile);
        if (pwdFile != null) {
          try (InputStream is = pwdFile.openStream()) {
            password = IOUtils.toString(is).trim().toCharArray();
          }
        }
      }
    }
    if (password == null) {
      password = KEYSTORE_PASSWORD_DEFAULT.toCharArray();
    }

    try {
      keyStore = KeyStore.getInstance(SCHEME_NAME);
      if (fs.exists(path)) {
        // save off permissions in case we need to
        // rewrite the keystore in flush()
        FileStatus s = fs.getFileStatus(path);
        permissions = s.getPermission();

        try (FSDataInputStream in = fs.open(path)) {
          keyStore.load(in, password);
        }
      } else {
        permissions = new FsPermission("700");
        // required to create an empty keystore. *sigh*
        keyStore.load(null, password);
      }
    } catch (KeyStoreException e) {
      throw new IOException("Can't create keystore", e);
    } catch (NoSuchAlgorithmException e) {
      throw new IOException("Can't load keystore " + path, e);
    } catch (CertificateException e) {
      throw new IOException("Can't load keystore " + path, e);
    }
    ReadWriteLock lock = new ReentrantReadWriteLock(true);
    readLock = lock.readLock();
    writeLock = lock.writeLock();
  }