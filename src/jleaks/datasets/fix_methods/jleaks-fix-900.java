private void refreshSettings() 
{
    int size = cache.getJadxSettings().getSrhResourceSkipSize() * 1048576;
    if (size != sizeLimit || !cache.getJadxSettings().getSrhResourceFileExt().equals(fileExts)) {
        clear();
        sizeLimit = size;
        fileExts = cache.getJadxSettings().getSrhResourceFileExt();
        String[] exts = fileExts.split("\\|");
        for (String ext : exts) {
            ext = ext.trim();
            if (!ext.isEmpty()) {
                anyExt = ext.equals("*");
                if (anyExt) {
                    break;
                }
                extSet.add(ext);
            }
        }
        try (ZipFile zipFile = getZipFile(cache.getJRoot())) {
            // reindex
            traverseTree(cache.getJRoot(), zipFile);
        } catch (Exception e) {
            LOG.error("Failed to apply settings to resource index", e);
        }
    }
}