private void init() throws JarResourceException 
{
    try {
        // extracts just sizes only.
        JarFile zf = new JarFile(jarFileName);
        try {
            Enumeration<JarEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                JarEntry ze = entries.nextElement();
                if (LOG.isTraceEnabled()) {
                    LOG.trace(dumpJarEntry(ze));
                }
                if (ze.isDirectory()) {
                    continue;
                }
                if (ze.getSize() > Integer.MAX_VALUE) {
                    throw new JarResourceException("Jar entry is too big to fit in memory.");
                }
                InputStream is = zf.getInputStream(ze);
                try {
                    byte[] bytes;
                    if (ze.getSize() < 0) {
                        bytes = ByteStreams.toByteArray(is);
                    } else {
                        bytes = new byte[(int) ze.getSize()];
                        ByteStreams.readFully(is, bytes);
                    }
                    // add to internal resource hashtable
                    entryContents.put(ze.getName(), bytes);
                    LOG.trace(ze.getName() + "size=" + ze.getSize() + ",csize=" + ze.getCompressedSize());
                } finally {
                    is.close();
                }
            }
            return zf.getManifest();
        } finally {
            zf.close();
        }
    } catch (NullPointerException e) {
        LOG.warn("Error during initialization resource. Reason {}", e.getMessage());
        throw new JarResourceException("Null pointer while loading jar file " + jarFileName);
    } catch (FileNotFoundException e) {
        LOG.warn("File {} not found. Reason : {}", jarFileName, e.getMessage());
        throw new JarResourceException("Jar file " + jarFileName + " requested to be loaded is not found");
    } catch (IOException e) {
        LOG.warn("Error while reading file {}. Reason : {}", jarFileName, e.getMessage());
        throw new JarResourceException("Error reading file " + jarFileName + ".");
    }
}