public SQLiteConnection(String url, String fileName, Properties prop) throws SQLException 
{
    final XStream outer = new XStream();
    try (final ObjectOutputStream oos = outer.createObjectOutputStream(out)) {
        oos.writeObject(xstream);
        oos.flush();
        xstream.toXML(obj, out);
    }
}