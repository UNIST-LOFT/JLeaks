private void serializeChunkletStore(ChunkletStore cStore, File location) 
{
    FileOutputStream fileOut = null;
    ObjectOutputStream objOut = null;
    try {
        fileOut = new FileOutputStream(location);
        objOut = new ObjectOutputStream(fileOut);
        objOut.writeObject(cStore);
    } catch (IOException ex) {
        ex.printStackTrace();
    } finally {
        if (fileOut != null) {
            try {
                fileOut.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (objOut != null) {
            try {
                objOut.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}