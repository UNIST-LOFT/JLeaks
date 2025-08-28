  SoundPlayback(AudioFormat format) throws LineUnavailableException {
    // acquire resources in the constructor so that they can be used before the task is started
    this.line = AudioSystem.getSourceDataLine(format);
    this.line.open();
    this.line.start();
    this.gainControl = (FloatControl) this.line.getControl(FloatControl.Type.MASTER_GAIN);
    this.muteControl = (BooleanControl) this.line.getControl(BooleanControl.Type.MUTE);
    this.masterVolume = this.createVolumeControl();
  }
