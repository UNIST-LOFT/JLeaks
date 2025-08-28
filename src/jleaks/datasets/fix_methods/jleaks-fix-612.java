public void save(String name) throws IOException 
{
    try (RIFFWriter writer = new RIFFWriter(out, "DLS ")) {
        writeSoundbank(writer);
    }
}