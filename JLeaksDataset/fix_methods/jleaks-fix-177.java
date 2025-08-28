private void loadFromInputStream(InputStream serialObjectInputStream){
    try (ObjectInputStream input = new ObjectInputStream(serialObjectInputStream)) {
        bigramHashTable = (long[]) input.readObject();
        frequencyTable = (int[]) input.readObject();
        // log.info("load bigram dict from serialization.");
    }
}