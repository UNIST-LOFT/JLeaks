private static Reader pickReaderVersion(Path path, FSDataInputStreamWrapper fsdis,
long size, CacheConfig cacheConf, DataBlockEncoding preferredEncodingInCache,
HFileSystem hfs) throws IOException {
    FixedFileTrailer trailer = null;
    try {
        boolean isHBaseChecksum = fsdis.shouldUseHBaseChecksum();
        // Initially we must read with FS checksum.
        assert !isHBaseChecksum;
        trailer = FixedFileTrailer.readFromStream(fsdis.getStream(isHBaseChecksum), size);
        switch(trailer.getMajorVersion()) {
            case 2:
                return new HFileReaderV2(path, trailer, fsdis, size, cacheConf, preferredEncodingInCache, hfs);
            case 3:
                return new HFileReaderV3(path, trailer, fsdis, size, cacheConf, preferredEncodingInCache, hfs);
            default:
                throw new IllegalArgumentException("Invalid HFile version " + trailer.getMajorVersion());
        }
    } catch (Throwable t) {
        try {
            fsdis.close();
        } catch (Throwable t2) {
            LOG.warn("Error closing fsdis FSDataInputStreamWrapper", t2);
        }
        throw new CorruptHFileException("Problem reading HFile Trailer from file " + path, t);
    }
}