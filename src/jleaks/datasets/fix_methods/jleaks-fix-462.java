public void apply() 
{
    InputStream is = null;
    ZipFile zip = null;
    ByteArrayOutputStream bout = null;
    try {
        long size;
        if (archiveFile.getName().endsWith(".apk") || archiveFile.getName().endsWith(".zip")) {
            zip = new ZipFile(archiveFile);
            ZipEntry mft = zip.getEntry("AndroidManifest.xml");
            size = mft.getSize();
            is = zip.getInputStream(mft);
        } else {
            size = archiveFile.length();
            is = new FileInputStream(archiveFile);
        }
        if (size > Integer.MAX_VALUE) {
            throw new IOException("File larger than " + Integer.MAX_VALUE + " bytes not supported");
        }
        bout = new ByteArrayOutputStream((int) size);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) > 0) {
            bout.write(buffer, 0, bytesRead);
        }
        this.xml = decompressXML(bout.toByteArray());
    } catch (Exception e) {
        fallback = true;
    } finally {
        closeResource(is);
        closeResource(zip);
        closeResource(bout);
    }
}