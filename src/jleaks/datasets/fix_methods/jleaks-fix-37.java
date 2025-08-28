public ReadableByteChannel get(PageId pageId, int pageOffset){
    Preconditions.checkArgument(pageOffset >= 0, "page offset should be non-negative");
    try {
        byte[] page = mDb.get(getKeyFromPageId(pageId));
        if (page == null) {
            throw new PageNotFoundException(new String(getKeyFromPageId(pageId)));
        }
        Preconditions.checkArgument(pageOffset <= page.length, "page offset %s exceeded page size %s", pageOffset, page.length);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(page)) {
            int bytesSkipped = (int) bais.skip(pageOffset);
            if (pageOffset != bytesSkipped) {
                throw new IOException(String.format("Failed to read page %s from offset %s: %s bytes skipped", pageId, pageOffset, bytesSkipped));
            }
            int bytesRead = 0;
            int bytesLeft = Math.min(page.length - pageOffset, buffer.length - bufferOffset);
            while (bytesLeft >= 0) {
                int bytes = bais.read(buffer, bufferOffset + bytesRead, bytesLeft);
                if (bytes <= 0) {
                    break;
                }
                bytesRead += bytes;
                bytesLeft -= bytes;
            }
            return bytesRead;
        }
    } catch (RocksDBException e) {
        throw new IOException("Failed to retrieve page", e);
    }
}