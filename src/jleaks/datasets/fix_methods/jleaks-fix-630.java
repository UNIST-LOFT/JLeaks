public MidiFileFormat getMidiFileFormat(URL url) throws InvalidMidiDataException, IOException 
{
    try (// throws IOException
    FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis, bisBufferSize)) {
        // $$fb 2002-04-17: part of fix for 4635286: MidiSystem.getMidiFileFormat() returns format having invalid length
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            length = MidiFileFormat.UNKNOWN_LENGTH;
        }
        MidiFileFormat fileFormat = getMidiFileFormatFromStream(bis, (int) length, null);
        return fileFormat;
    }
}