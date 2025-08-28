  public InputStream getInputStream(final VirtualFile file) throws IOException {
    lock.lock();
    try {
      final ZipEntry entry = convertToEntry(file);
      if (entry == null) {
        lock.unlock();
        return new ByteArrayInputStream(new byte[0]);
      }

      final ZipFile zip = getZip();
      assert zip != null;

      return new BufferedInputStream(zip.getInputStream(entry)) {
        public void close() throws IOException {
          super.close();
          lock.unlock();
        }
      };
    }
    catch (Throwable e) {
      lock.unlock();
      throw new RuntimeException(e);
    }
  }
