private void save() throws IOException 
{
    ObjectOutputStream stream = null;
    try {
        stream = new ObjectOutputStream(new FileOutputStream(fHistoryStore));
        stream.writeObject(this);
    } finally {
        if (stream != null) {
            stream.close();
        }
    }
}