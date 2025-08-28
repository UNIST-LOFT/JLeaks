    public OutputStream writeMapTile(String mapId, TileType tileType, Vector2i tile) throws IOException {
        Path file = getFilePath(mapId, tileType, tile);

        OutputStream os = AtomicFileHelper.createFilepartOutputStream(file);
        os = new BufferedOutputStream(os);
        os = compression.compress(os);

        return os;
    }
