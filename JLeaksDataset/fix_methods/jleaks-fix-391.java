public void writeTo(BufferedSink sink) throws IOException 
{
    OutputStream os = sink.outputStream();
    if (useGzip(payload)) {
        final Deflater def = new Deflater(GZIP_COMPRESSION_LEVEL);
        os = new DeflaterOutputStream(os, def);
    }
    try {
        payloadSerializer.serializePayload(os, payload);
    } finally {
        os.close();
        payload.recycle();
    }
}