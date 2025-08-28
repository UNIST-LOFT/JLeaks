public InputStream getInputStream(final VirtualFile file) throws IOException 
{
    lock.lock();
    try {
        final ZipEntry entry = convertToEntry(file);
        if (entry == null) {
            return new ByteArrayInputStream(new byte[0]);
        }
        final ZipFile zip = getZip();
        assert zip != null;
        ByteArrayOutputStream result = new ByteArrayOutputStream((int) entry.getSize());
        InputStream nativeStream = new BufferedInputStream(zip.getInputStream(entry));
        try {
            FileUtil.copy(nativeStream, result);
        } finally {
            nativeStream.close();
        }
        return new ByteArrayInputStream(result.toByteArray());
    } finally {
        lock.unlock();
    }
}