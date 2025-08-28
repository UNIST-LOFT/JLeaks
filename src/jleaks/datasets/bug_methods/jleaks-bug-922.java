    public ByteBuf serializeRequest(I message) throws IOException {
        switch (requestType) {
            case PROTOBUF:
                final PrototypeMarshaller<I> marshaller = (PrototypeMarshaller<I>) requestMarshaller;
                return serializeProto(marshaller, (Message) message);
            default:
                // TODO(minwoox) Optimize this by creating buffer with the sensible initial capacity.
                final CompositeByteBuf out = alloc.compositeBuffer();
                try (ByteBufOutputStream os = new ByteBufOutputStream(out)) {
                    if (isProto) {
                        ByteStreams.copy(method.streamRequest(message), os);
                    } else {
                        jsonMarshaller.serializeMessage(requestMarshaller, message, os);
                    }
                }
                return out;
        }
    }
