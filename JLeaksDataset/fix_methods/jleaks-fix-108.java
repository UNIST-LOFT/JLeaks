public List<Path> compact(final CompactionRequest request) throws IOException 
{
    FileDetails fd = getFileDetails(request.getFiles(), request.isAllFiles());
    this.progress = new CompactionProgress(fd.maxKeyCount);
    // Find the smallest read point across all the Scanners.
    long smallestReadPoint = getSmallestReadPoint();
    List<StoreFileScanner> scanners = createFileScanners(request.getFiles(), smallestReadPoint);
    StoreFile.Writer writer = null;
    List<Path> newFiles = new ArrayList<Path>();
    boolean cleanSeqId = false;
    IOException e = null;
    try {
        InternalScanner scanner = null;
        try {
            /* Include deletes, unless we are doing a compaction of all files */
            ScanType scanType = request.isAllFiles() ? ScanType.COMPACT_DROP_DELETES : ScanType.COMPACT_RETAIN_DELETES;
            scanner = preCreateCoprocScanner(request, scanType, fd.earliestPutTs, scanners);
            if (scanner == null) {
                scanner = createScanner(store, scanners, scanType, smallestReadPoint, fd.earliestPutTs);
            }
            scanner = postCreateCoprocScanner(request, scanType, scanner);
            if (scanner == null) {
                // NULL scanner returned from coprocessor hooks means skip normal processing.
                return newFiles;
            }
            // Create the writer even if no kv(Empty store file is also ok),
            // because we need record the max seq id for the store file, see HBASE-6059
            if (fd.minSeqIdToKeep > 0) {
                smallestReadPoint = Math.min(fd.minSeqIdToKeep, smallestReadPoint);
                cleanSeqId = true;
            }
            writer = store.createWriterInTmp(fd.maxKeyCount, this.compactionCompression, true, fd.maxMVCCReadpoint >= smallestReadPoint, fd.maxTagsLength > 0);
            boolean finished = performCompaction(scanner, writer, smallestReadPoint, cleanSeqId);
            if (!finished) {
                writer.close();
                store.getFileSystem().delete(writer.getPath(), false);
                writer = null;
                throw new InterruptedIOException("Aborting compaction of store " + store + " in region " + store.getRegionInfo().getRegionNameAsString() + " because it was interrupted.");
            }
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    } catch (IOException ioe) {
        e = ioe;
        // Throw the exception
        throw ioe;
    } finally {
        if (writer != null) {
            if (e != null) {
                writer.close();
            } else {
                writer.appendMetadata(fd.maxSeqId, request.isAllFiles());
                writer.close();
                newFiles.add(writer.getPath());
            }
        }
    }
    return newFiles;
}