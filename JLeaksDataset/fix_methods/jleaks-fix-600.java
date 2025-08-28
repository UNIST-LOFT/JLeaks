public void compress() 
{
    compressionThread.submit(new Runnable() {

        public void run() {
            try {
                try (InputStream in = read();
                    OutputStream out = new GZIPOutputStream(new FileOutputStream(gz))) {
                    Util.copyStream(in, out);
                }
                // if the compressed file is created successfully, remove the original
                file.delete();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to compress " + file, e);
                // in case a processing is left in the middle
                gz.delete();
            }
        }
    });
}