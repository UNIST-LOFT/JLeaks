  public T parse(InputStream message) {
    ReadableBuffer rawBuffer = GrpcSerializationUtils.getBufferFromStream(message);
    try {
      if (rawBuffer != null) {
        CompositeReadableBuffer readableBuffer = new CompositeReadableBuffer();
        readableBuffer.addBuffer(rawBuffer);
        return deserialize(readableBuffer);
      } else {
        // falls back to buffer copy
        byte[] byteBuffer = new byte[message.available()];
        message.read(byteBuffer);
        return deserialize(ReadableBuffers.wrap(byteBuffer));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

