private void internalStart() 
{
    if (!started) {
        createLocationIfRequiredAndVerify(rootDirectory);
        try {
            rw = new RandomAccessFile(lockFile, "rw");
        } catch (FileNotFoundException e) {
            // should not happen normally since we checked that everything is fine right above
            throw new RuntimeException(e);
        }
        try {
            lock = rw.getChannel().lock();
        } catch (Exception e) {
            try {
                rw.close();
            } catch (IOException e1) {
                // ignore silently
            }
            throw new RuntimeException("Couldn't lock rootDir: " + rootDirectory.getAbsolutePath(), e);
        }
        started = true;
        LOGGER.debug("RootDirectory Locked");
    }
}