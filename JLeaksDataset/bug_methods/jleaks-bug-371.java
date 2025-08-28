
    public void run() {
        try {
            reader.readAll();
            close();
        } catch (Exception e) {
            throw new RuntimeException("failed to read file.", e);
        }
    }
