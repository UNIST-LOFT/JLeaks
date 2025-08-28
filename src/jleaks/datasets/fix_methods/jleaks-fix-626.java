public Soundbank getSoundbank(URL url){
    try {
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        ais.close();
        ModelByteBufferWavetable osc = new ModelByteBufferWavetable(new ModelByteBuffer(file, 0, file.length()), -4800);
        ModelPerformer performer = new ModelPerformer();
        performer.getOscillators().add(osc);
        SimpleSoundbank sbk = new SimpleSoundbank();
        SimpleInstrument ins = new SimpleInstrument();
        ins.add(performer);
        sbk.addInstrument(ins);
        return sbk;
    } catch (UnsupportedAudioFileException e1) {
        return null;
    } catch (IOException e) {
        return null;
    }
}