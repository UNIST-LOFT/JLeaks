private void writeFile() 
{
    DataOutputStream output = null;
    try {
        OutputStream outputStream = atomicFile.startWrite();
        output = new DataOutputStream(outputStream);
        output.writeInt(VERSION);
        int flags = cipher != null ? FLAG_ENCRYPTED_INDEX : 0;
        output.writeInt(flags);
        if (cipher != null) {
            byte[] initializationVector = new byte[16];
            new Random().nextBytes(initializationVector);
            output.write(initializationVector);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector);
            try {
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
                // Should never happen.
                throw new IllegalStateException(e);
            }
            output.flush();
            output = new DataOutputStream(new CipherOutputStream(outputStream, cipher));
        }
        output.writeInt(keyToContent.size());
        int hashCode = 0;
        for (CachedContent cachedContent : keyToContent.values()) {
            cachedContent.writeToStream(output);
            hashCode += cachedContent.headerHashCode();
        }
        output.writeInt(hashCode);
        atomicFile.endWrite(output);
    } catch (IOException e) {
        throw new CacheException(e);
    } finally {
        Util.closeQuietly(output);
    }
}