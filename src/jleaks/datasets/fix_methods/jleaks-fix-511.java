private void handleTARArchive(ArchiveStoreContext ctx, FreenetURI key, InputStream data, String element, ArchiveExtractCallback callback, MutableBoolean gotElement, boolean throwAtExit, ObjectContainer container, ClientContext context) throws ArchiveFailureException, ArchiveRestartException 
{
    if (logMINOR)
        Logger.minor(this, "Handling a TAR Archive");
    TarInputStream tarIS = null;
    try {
        tarIS = new TarInputStream(data);
        // MINOR: Assumes the first entry in the tarball is a directory.
        TarEntry entry;
        byte[] buf = new byte[32768];
        HashSet<String> names = new HashSet<String>();
        boolean gotMetadata = false;
        outerTAR: while (true) {
            entry = tarIS.getNextEntry();
            if (entry == null)
                break;
            if (entry.isDirectory())
                continue;
            String name = entry.getName();
            if (names.contains(name)) {
                Logger.error(this, "Duplicate key " + name + " in archive " + key);
                continue;
            }
            long size = entry.getSize();
            if (name.equals(".metadata"))
                gotMetadata = true;
            if (size > maxArchivedFileSize && !name.equals(element)) {
                addErrorElement(ctx, key, name, "File too big: " + size + " greater than current archived file size limit " + maxArchivedFileSize, true);
            } else {
                // Read the element
                long realLen = 0;
                Bucket output = tempBucketFactory.makeBucket(size);
                OutputStream out = output.getOutputStream();
                try {
                    int readBytes;
                    while ((readBytes = tarIS.read(buf)) > 0) {
                        out.write(buf, 0, readBytes);
                        readBytes += realLen;
                        if (readBytes > maxArchivedFileSize) {
                            addErrorElement(ctx, key, name, "File too big: " + maxArchivedFileSize + " greater than current archived file size limit " + maxArchivedFileSize, true);
                            out.close();
                            out = null;
                            output.free();
                            continue outerTAR;
                        }
                    }
                } finally {
                    if (out != null)
                        out.close();
                }
                if (size <= maxArchivedFileSize) {
                    addStoreElement(ctx, key, name, output, gotElement, element, callback, container, context);
                    names.add(name);
                    trimStoredData();
                } else {
                    // We are here because they asked for this file.
                    callback.gotBucket(output, container, context);
                    gotElement.value = true;
                    addErrorElement(ctx, key, name, "File too big: " + size + " greater than current archived file size limit " + maxArchivedFileSize, true);
                }
            }
        }
        // If no metadata, generate some
        if (!gotMetadata) {
            generateMetadata(ctx, key, names, gotElement, element, callback, container, context);
            trimStoredData();
        }
        if (throwAtExit)
            throw new ArchiveRestartException("Archive changed on re-fetch");
        if ((!gotElement.value) && element != null)
            callback.notInArchive(container, context);
    } catch (IOException e) {
        throw new ArchiveFailureException("Error reading archive: " + e.getMessage(), e);
    } finally {
        Closer.close(tarIS);
    }
}