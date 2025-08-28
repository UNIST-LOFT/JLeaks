    public Soundbank getSoundbank(URL url)
            throws InvalidMidiDataException, IOException {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Soundbank sbk = getSoundbank(ais);
            ais.close();
            return sbk;
        } catch (UnsupportedAudioFileException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }
