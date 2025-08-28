private void loadFromObjectInputStream(InputStream serialObjectInputStream){
    try (ObjectInputStream input = new ObjectInputStream(serialObjectInputStream)) {
        wordIndexTable = (short[]) input.readObject();
        charIndexTable = (char[]) input.readObject();
        wordItem_charArrayTable = (char[][][]) input.readObject();
        wordItem_frequencyTable = (int[][]) input.readObject();
        // log.info("load core dict from serialization.");
    }
}