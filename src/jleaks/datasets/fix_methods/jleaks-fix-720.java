public InputStream getEntryInputStream(final ArchiveEntry entry, ArchiveEntryIterator entryIterator) throws IOException, UnsupportedFileOperationException 
{
    final IInArchive sevenZipFile = openSevenZipFile();
    final FailSafePipedInputStream in = new FailSafePipedInputStream();
    final PipedOutputStream out = new PipedOutputStream(in);
    threadPool.execute(new Runnable() {

        public void run() {
            BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
            try {
                MuArchiveExtractCallback extractCallbackSpec = new MuArchiveExtractCallback(bufferedOut, entry.getPath());
                extractCallbackSpec.Init(sevenZipFile);
                sevenZipFile.Extract(null, -1, IInArchive.NExtract_NAskMode_kExtract, extractCallbackSpec);
                bufferedOut.flush();
            } catch (Exception e) {
                FileLogger.fine("Error while retrieving 7zip entry " + entry.getName(), e);
            } finally {
                try {
                    bufferedOut.close();
                } catch (IOException e) {
                    // Not much we can do about it
                }
                try {
                    in.close();
                } catch (IOException e) {
                    // Not much we can do about it
                }
                try {
                    sevenZipFile.close();
                } catch (IOException e) {
                    // Not much we can do about it
                }
            }
        }
    });
    return in;
}