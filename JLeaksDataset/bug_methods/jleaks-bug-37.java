  public ReadableByteChannel get(PageId pageId, int pageOffset)
      throws IOException, PageNotFoundException {
    Preconditions.checkArgument(pageOffset >= 0, "page offset should be non-negative");
    try {
      byte[] page = mDb.get(getKeyFromPageId(pageId));
      if (page == null) {
        throw new PageNotFoundException(new String(getKeyFromPageId(pageId)));
      }
      Preconditions.checkArgument(pageOffset <= page.length,
          "page offset %s exceeded page size %s", pageOffset, page.length);
      ByteArrayInputStream bais = new ByteArrayInputStream(page);
      bais.skip(pageOffset);
      return Channels.newChannel(bais);
    } catch (RocksDBException e) {
      throw new IOException("Failed to retrieve page", e);
    }
  }
