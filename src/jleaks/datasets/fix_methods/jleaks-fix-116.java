public static int getCompressedSize(Algorithm algo, Compressor compressor,
byte[] inputBuffer, int offset, int length) throws IOException {
    DataOutputStream compressedStream = new DataOutputStream(new IOUtils.NullOutputStream());
    if (compressor != null) {
        compressor.reset();
    }
    OutputStream compressingStream = null;
    try {
        compressingStream = algo.createCompressionStream(compressedStream, compressor, 0);
        compressingStream.write(inputBuffer, offset, length);
        compressingStream.flush();
        return compressedStream.size();
    } finally {
        if (compressingStream != null)
            compressingStream.close();
    }
}