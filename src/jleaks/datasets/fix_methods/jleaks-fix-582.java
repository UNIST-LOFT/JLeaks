public void visit(final File f, final String _relativePath) throws IOException 
{
    int mode = IOUtils.mode(f);
    // On Windows, the elements of relativePath are separated by
    // back-slashes (\), but Zip files need to have their path elements separated
    // by forward-slashes (/)
    String relativePath = _relativePath.replace('\\', '/');
    if (f.isDirectory()) {
        ZipEntry dirZipEntry = new ZipEntry(relativePath + '/');
        // Setting this bit explicitly is needed by some unzipping applications (see JENKINS-3294).
        dirZipEntry.setExternalAttributes(BITMASK_IS_DIRECTORY);
        if (mode != -1)
            dirZipEntry.setUnixMode(mode);
        dirZipEntry.setTime(f.lastModified());
        zip.putNextEntry(dirZipEntry);
        zip.closeEntry();
    } else {
        ZipEntry fileZipEntry = new ZipEntry(relativePath);
        if (mode != -1)
            fileZipEntry.setUnixMode(mode);
        fileZipEntry.setTime(f.lastModified());
        zip.putNextEntry(fileZipEntry);
        FileInputStream in = new FileInputStream(f);
        try {
            int len;
            while ((len = in.read(buf)) > 0) zip.write(buf, 0, len);
        } finally {
            in.close();
        }
        zip.closeEntry();
    }
    entriesWritten++;
}