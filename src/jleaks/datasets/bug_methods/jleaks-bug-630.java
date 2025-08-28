    public MidiFileFormat getMidiFileFormat(URL url) throws InvalidMidiDataException, IOException {
        InputStream urlStream = url.openStream(); // throws IOException
        BufferedInputStream bis = new BufferedInputStream( urlStream, bisBufferSize );
        MidiFileFormat fileFormat = null;
        try {
            fileFormat = getMidiFileFormat( bis ); // throws InvalidMidiDataException
        } finally {
            bis.close();
        }
        return fileFormat;
    }
