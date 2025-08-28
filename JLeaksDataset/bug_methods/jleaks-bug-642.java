    private void save() throws IOException {
        ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(
                fHistoryStore));
        stream.writeObject(this);
        stream.close();
    }
