  private static Reader pickReaderVersion(Path path, FSDataInputStreamWrapper fsdis,
      long size, CacheConfig cacheConf, DataBlockEncoding preferredEncodingInCache,
      HFileSystem hfs) throws IOException {
    FixedFileTrailer trailer = null;
    try {
      boolean isHBaseChecksum = fsdis.shouldUseHBaseChecksum();
      assert !isHBaseChecksum; // Initially we must read with FS checksum.
      trailer = FixedFileTrailer.readFromStream(fsdis.getStream(isHBaseChecksum), size);
    } catch (IllegalArgumentException iae) {
      throw new CorruptHFileException("Problem reading HFile Trailer from file " + path, iae);
    }
    switch (trailer.getMajorVersion()) {
    case 2:
      return new HFileReaderV2(
          path, trailer, fsdis, size, cacheConf, preferredEncodingInCache, hfs);
    case 3 :
      return new HFileReaderV3(
          path, trailer, fsdis, size, cacheConf, preferredEncodingInCache, hfs);
    default:
      throw new CorruptHFileException("Invalid HFile version " + trailer.getMajorVersion());
    }
  }
