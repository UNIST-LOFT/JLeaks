    public void consumeAndCloseOutputStream() {
        try {
            byte[] buff = new byte[512];
            while (processOutStream().read(buff) >= 0) {
                // Do nothing
            }
            processOutStream().close();
        } catch (IOException e) {
            // Given we are closing down the process there is no point propagating IO exceptions here
        }
    }
