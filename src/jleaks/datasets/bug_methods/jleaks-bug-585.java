    public FilePath createTextTempFile(final String prefix, final String suffix, final String contents, final boolean inThisDirectory) throws IOException, InterruptedException {
        try {
            return new FilePath(channel,act(new FileCallable<String>() {
                private static final long serialVersionUID = 1L;
                public String invoke(File dir, VirtualChannel channel) throws IOException {
                    if(!inThisDirectory)
                        dir = new File(System.getProperty("java.io.tmpdir"));
                    else
                        dir.mkdirs();

                    File f;
                    try {
                        f = File.createTempFile(prefix, suffix, dir);
                    } catch (IOException e) {
                        throw new IOException2("Failed to create a temporary directory in "+dir,e);
                    }

                    Writer w = new FileWriter(f);
                    w.write(contents);
                    w.close();

                    return f.getAbsolutePath();
                }
            }));
        } catch (IOException e) {
            throw new IOException2("Failed to create a temp file on "+remote,e);
        }
    }
