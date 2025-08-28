    public void compress() {
        compressionThread.submit(new Runnable() {
            public void run() {
                try {
                    InputStream in = read();
                    OutputStream out = new GZIPOutputStream(new FileOutputStream(gz));
                    try {
                        Util.copyStream(in,out);
                    } finally {
                        in.close();
                        out.close();
                    }
                    // if the compressed file is created successfully, remove the original
                    file.delete();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to compress "+file,e);
                    gz.delete(); // in case a processing is left in the middle
                }
            }
        });
    }
