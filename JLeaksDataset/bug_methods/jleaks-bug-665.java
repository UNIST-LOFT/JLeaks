        private ByteBuffer compress(ByteBuffer source) throws IOException {
            if (source.remaining() == 0) {
                return source;
            }

            ByteBufferInputStream sourceStream = new ByteBufferInputStream(source);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(source.remaining() / 2);
            OutputStream compressor = null;
            if (compressionType == CompressionType.GZIP) {
                compressor = new GZIPOutputStream(outputStream);
            }

            if (compressionType == CompressionType.DEFLATE) {
                compressor = new DeflaterOutputStream(outputStream);
            }

            try {
                copy(sourceStream, compressor);
            } finally {
                compressor.close();
            }

            return ByteBuffer.wrap(outputStream.toByteArray());
        }
