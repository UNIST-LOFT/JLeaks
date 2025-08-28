    void run(File inFile, JarOutputStream jstream) throws IOException {
        // %%% maybe memory-map the file, and pass it straight into unpacker
        ByteBuffer mappedFile = null;
        try (FileInputStream fis = new FileInputStream(inFile)) {
            run(fis, jstream, mappedFile);
        }
        // Note:  caller is responsible to finish with jstream.
    }
