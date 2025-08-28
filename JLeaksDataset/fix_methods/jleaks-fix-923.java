public void writeEntry(String entryName, InputStream inputStream) throws IOException 
{
    String name = entry.getName();
    writeParentFolderEntries(name);
    if (this.writtenEntries.add(name)) {
        entry.setUnixMode(name.endsWith("/") ? UNIX_DIR_MODE : UNIX_FILE_MODE);
        entry.getGeneralPurposeBit().useUTF8ForNames(true);
        if (!entry.isDirectory() && entry.getSize() == -1) {
            entryWriter = SizeCalculatingEntryWriter.get(entryWriter);
            entry.setSize(entryWriter.size());
        }
        entryWriter = addUnpackCommentIfNecessary(entry, entryWriter, unpackHandler);
        writeToArchive(entry, entryWriter);
    }
}