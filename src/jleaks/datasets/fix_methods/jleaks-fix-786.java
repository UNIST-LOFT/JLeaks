
    private void encodeObject(OutputStream out) {
        try (XMLEncoder e = new XMLEncoder(new BufferedOutputStream(out))) {
            e.writeObject(this);
        }
    }