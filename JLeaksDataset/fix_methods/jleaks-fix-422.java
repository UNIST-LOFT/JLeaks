public static void writeInheritanceTree(InheritanceTree tree, File file) throws IOException 
{
    XStream xstream = new XStream();
    XStream.setupDefaultSecurity(xstream);
    xstream.allowTypesByWildcard(new String[] { "org.evosuite.**", "org.jgrapht.**" });
    try (GZIPOutputStream output = new GZIPOutputStream(new FileOutputStream(file))) {
        xstream.toXML(tree, output);
    }
}