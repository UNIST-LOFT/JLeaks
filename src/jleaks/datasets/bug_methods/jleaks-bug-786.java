    private void encodeObject(OutputStream out) {
        XMLEncoder e = new XMLEncoder(new BufferedOutputStream(out));
        e.writeObject(this);
        e.close();
    }
