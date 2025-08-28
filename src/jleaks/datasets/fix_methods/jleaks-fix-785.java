private Map<String, Map<String, ZipUtils.Info>> getResourceMap() throws IOException 
{
    Map<String, Map<String, ZipUtils.Info>> res = content;
    if (res == null) {
        synchronized (this) {
            res = content;
            if (res == null) {
                try (SeekableByteChannel channel = getChannel()) {
                    res = ZipUtils.readEntries(channel);
                }
                content = res;
            }
        }
    }
    return res;
}