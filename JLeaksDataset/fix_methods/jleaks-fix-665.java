private ByteBuffer compress(ByteBuffer source) throws IOException 
{
    if (source.remaining() == 0) {
        return source;
    }
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(source.remaining() / 2);
    OutputStream compressor = null;
    try {
        try (ByteBufferInputStream sourceStream = new ByteBufferInputStream(source)) {
            if (compressionType == CompressionType.GZIP) {
                compressor = new GZIPOutputStream(outputStream);
            }
            if (compressionType == CompressionType.DEFLATE) {
                compressor = new DeflaterOutputStream(outputStream);
            }
            copy(sourceStream, compressor);
        } finally {
            if (compressor != null) {
                compressor.close();
            }
        }
        return ByteBuffer.wrap(outputStream.toByteArray());
    } finally {
        outputStream.close();
    }
}