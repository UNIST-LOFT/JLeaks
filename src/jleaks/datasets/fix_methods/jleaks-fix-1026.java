private WikiPageProperties loadAttributes(final FileVersion fileVersion) throws IOException 
{
    final WikiPageProperties props = new WikiPageProperties();
    InputStream content = fileVersion.getContent();
    try {
        props.loadFromXml(content);
    } finally {
        content.close();
    }
    props.setLastModificationTime(fileVersion.getLastModificationTime());
    return props;
}