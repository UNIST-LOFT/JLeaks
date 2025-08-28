    public void toXML(final XStream xstream, final Object obj, final Writer out) throws IOException {
        final XStream outer = new XStream();
        final ObjectOutputStream oos = outer.createObjectOutputStream(out);
        try {
            oos.writeObject(xstream);
            oos.flush();
            xstream.toXML(obj, out);
        } finally {
            oos.close();
        }
    }
