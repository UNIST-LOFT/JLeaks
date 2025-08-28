public OutputStream writeMapTile(String mapId, TileType tileType, Vector2i tile) throws IOException 
{
    Path file = getFilePath(mapId, tileType, tile);
    OutputStream os = AtomicFileHelper.createFilepartOutputStream(file);
    os = new BufferedOutputStream(os);
    try {
        os = compression.compress(os);
    } catch (IOException ex) {
        os.close();
        throw ex;
    }
    return os;
}