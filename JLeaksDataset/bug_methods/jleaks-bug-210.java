public void serialize(LittleEndianOutput out) {
    out.writeShort(encryptionType);

    byte[] data = new byte[1024];
    LittleEndianByteArrayOutputStream bos = new LittleEndianByteArrayOutputStream(data, 0); // NOSONAR

    switch (encryptionInfo.getEncryptionMode()) {
        case xor:
            ((XOREncryptionHeader)encryptionInfo.getHeader()).write(bos);
            ((XOREncryptionVerifier)encryptionInfo.getVerifier()).write(bos);
            break;
        case binaryRC4:
            out.writeShort(encryptionInfo.getVersionMajor());
            out.writeShort(encryptionInfo.getVersionMinor());
            ((BinaryRC4EncryptionHeader)encryptionInfo.getHeader()).write(bos);
            ((BinaryRC4EncryptionVerifier)encryptionInfo.getVerifier()).write(bos);
            break;
        case cryptoAPI:
            out.writeShort(encryptionInfo.getVersionMajor());
            out.writeShort(encryptionInfo.getVersionMinor());
            out.writeInt(encryptionInfo.getEncryptionFlags());
            ((CryptoAPIEncryptionHeader)encryptionInfo.getHeader()).write(bos);
            ((CryptoAPIEncryptionVerifier)encryptionInfo.getVerifier()).write(bos);
            break;
        default:
            throw new EncryptedDocumentException("not supported");
    }

    out.write(data, 0, bos.getWriteIndex());
}