    public static void main(final String[] args)throws Exception
    {
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
        final DirectBuffer directBuffer = new DirectBuffer(byteBuffer);
        final int messageTemplateVersion = 0;
        int bufferOffset = 0;
        int encodingLength = 0;

        // Setup for encoding a message

        MESSAGE_HEADER
            .reset(directBuffer, bufferOffset, messageTemplateVersion)
            .blockLength(CAR.blockLength())
            .templateId((int)CAR.templateId())
            .version((short)CAR.templateVersion());

        bufferOffset += MESSAGE_HEADER.size();
        encodingLength = MESSAGE_HEADER.size();
        encodingLength += encode(CAR, directBuffer, bufferOffset);

        // Optionally write the encoded buffer to a file for decoding by the On-The-Fly decoder

        final String encodingFilename = System.getProperty(ENCODING_FILENAME);
        if (encodingFilename != null)
        {
            FileChannel channel = new FileOutputStream(encodingFilename).getChannel();

            byteBuffer.limit(encodingLength);
            channel.write(byteBuffer);
            channel.close();
        }

        // Decode the encoded message

        bufferOffset = 0;
        MESSAGE_HEADER.reset(directBuffer, bufferOffset, messageTemplateVersion);

        // Lookup the applicable flyweight to decode this type of message based on templateId and version.
        final int templateId = MESSAGE_HEADER.templateId();
        final int actingVersion = MESSAGE_HEADER.version();
        final int actingBlockLength = MESSAGE_HEADER.blockLength();

        bufferOffset += MESSAGE_HEADER.size();
        decode(CAR, directBuffer, bufferOffset, actingBlockLength, actingVersion);
    }
