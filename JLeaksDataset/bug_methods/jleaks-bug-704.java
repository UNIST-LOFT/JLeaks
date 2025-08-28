    private void serializeChunkletStore(ChunkletStore cStore, File location) {
        try {
            FileOutputStream fileOut = new FileOutputStream(location);
            ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
            objOut.writeObject(cStore);
            objOut.close();
            fileOut.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
