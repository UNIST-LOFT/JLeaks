protected void writeToNew(Path newPath) throws IOException {
    try (FSDataOutputStream out =
        FileSystem.create(fs, newPath, permissions);) {
      keyStore.store(out, password);
    } catch (KeyStoreException e) {
      throw new IOException("Can't store keystore " + this, e);
    } catch (NoSuchAlgorithmException e) {
      throw new IOException(
          "No such algorithm storing keystore " + this, e);
    } catch (CertificateException e) {
      throw new IOException(
          "Certificate exception storing keystore " + this, e);
    }
  }