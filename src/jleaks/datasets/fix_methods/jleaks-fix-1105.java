public InputStream getInputStreamForSection(FileSummary.Section section,
                                                String compressionCodec)
        throws IOException {
      FileInputStream fin = new FileInputStream(filename);
      try {

          FileChannel channel = fin.getChannel();
          channel.position(section.getOffset());
          InputStream in = new BufferedInputStream(new LimitInputStream(fin,
                  section.getLength()));

          in = FSImageUtil.wrapInputStreamForCompression(conf,
                  compressionCodec, in);
          return in;
      } catch (IOException e) {
          fin.close();
          throw e;
      }
    }