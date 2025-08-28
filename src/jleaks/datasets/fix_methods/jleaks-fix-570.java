public static String getDigestOf(InputStream source) throws IOException {
    try {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        DigestInputStream in = new DigestInputStream(source, md5);
        // Note: IOUtils.copy() buffers the input internally, so there is no
        // need to use a BufferedInputStream.
        IOUtils.copy(in, NullOutputStream.NULL_OUTPUT_STREAM);
        return toHexString(md5.digest());
    } catch (NoSuchAlgorithmException e) {
        throw new IOException("MD5 not installed",e);    // impossible
    } finally {
        source.close();
    }
}