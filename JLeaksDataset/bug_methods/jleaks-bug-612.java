    public void save(String name) throws IOException {
        writeSoundbank(new RIFFWriter(name, "DLS "));
    }
