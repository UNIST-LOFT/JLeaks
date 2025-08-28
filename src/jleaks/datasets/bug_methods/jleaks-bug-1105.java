    public InputStream getInputStreamForSection(FileSummary.Section section,
                                                String compressionCodec)
        throws IOException {
      FileInputStream fin = new FileInputStream(filename);
      FileChannel channel = fin.getChannel();
      channel.position(section.getOffset());
      InputStream in = new BufferedInputStream(new LimitInputStream(fin,
          section.getLength()));

      in = FSImageUtil.wrapInputStreamForCompression(conf,
          compressionCodec, in);
      return in;
    }