    public void load() throws IOException {
        if (root != this) {
            root.load();
            return;
        }
        if (buffer != null)
            return;
        if (file == null) {
            throw new IllegalStateException(
                    "No file associated with this ByteBuffer!");
        }

        DataInputStream is = new DataInputStream(getInputStream());
        buffer = new byte[(int) capacity()];
        offset = 0;
        is.readFully(buffer);
        is.close();

    }

