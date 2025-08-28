public void consumeAndCloseOutputStream() 
{
    try (InputStream outStream = processOutStream()) {
        byte[] buff = new byte[512];
        while (outStream.read(buff) >= 0) {
            // Do nothing
        }
    } catch (IOException e) {
        // Given we are closing down the process there is no point propagating IO exceptions here
    }
}