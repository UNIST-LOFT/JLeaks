public Object run() 
{
    try {
        return new BufferedInputStream(xmlFile.openStream());
    } catch (IOException ex) {
        return null;
    }
}