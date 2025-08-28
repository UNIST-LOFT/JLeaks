private String createTarBucket(OutputStream os, ObjectContainer container) throws IOException 
{
    if (logMINOR)
        Logger.minor(this, "Create a TAR Bucket");
    TarArchiveOutputStream tarOS = new TarArchiveOutputStream(os);
    try {
        tarOS.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        TarArchiveEntry ze;
        for (ContainerElement ph : containerItems) {
            if (logMINOR)
                Logger.minor(this, "Putting into tar: " + ph + " data length " + ph.data.size() + " name " + ph.targetInArchive);
            ze = new TarArchiveEntry(ph.targetInArchive);
            ze.setModTime(0);
            long size = ph.data.size();
            ze.setSize(size);
            tarOS.putArchiveEntry(ze);
            BucketTools.copyTo(ph.data, tarOS, size);
            tarOS.closeArchiveEntry();
        }
    } finally {
        tarOS.close();
    }
    return ARCHIVE_TYPE.TAR.mimeTypes[0];
}