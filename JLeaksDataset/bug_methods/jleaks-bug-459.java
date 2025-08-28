    public boolean read(GlowChunk chunk, int x, int z) throws IOException {
        RegionFile region = cache.getRegionFile(dir, x, z);
        int regionX = x & (REGION_SIZE - 1);
        int regionZ = z & (REGION_SIZE - 1);
        if (!region.hasChunk(regionX, regionZ)) {
            return false;
        }

        DataInputStream in = region.getChunkDataInputStream(regionX, regionZ);

        NBTInputStream nbt = new NBTInputStream(in, false);
        CompoundTag root = nbt.readCompound();
        CompoundTag levelTag = root.getCompound("Level");
        nbt.close();

        // read the vertical sections
        List<CompoundTag> sectionList = levelTag.getCompoundList("Sections");
        ChunkSection[] sections = new ChunkSection[16];
        for (CompoundTag sectionTag : sectionList) {
            int y = sectionTag.getByte("Y");
            byte[] types = sectionTag.getByteArray("Blocks");
            byte[] data = sectionTag.getByteArray("Data");
            byte[] blockLight = sectionTag.getByteArray("BlockLight");
            byte[] skyLight = sectionTag.getByteArray("SkyLight");
            sections[y] = new ChunkSection(types, expand(data), expand(skyLight), expand(blockLight));
        }

        // initialize the chunk
        chunk.initializeSections(sections);
        chunk.setPopulated(levelTag.getBool("TerrainPopulated"));

        // read biomes
        if (levelTag.isByteArray("Biomes")) {
            chunk.setBiomes(levelTag.getByteArray("Biomes"));
        }

        // read "Entities" eventually
        // read "HeightMap" if we need to

        // read tile entities
        List<CompoundTag> storedTileEntities = levelTag.getCompoundList("TileEntities");
        for (CompoundTag tileEntityTag : storedTileEntities) {
            TileEntity tileEntity = chunk.getBlock(
                    tileEntityTag.getInt("x"),
                    tileEntityTag.getInt("y"),
                    tileEntityTag.getInt("z")).getTileEntity();
            if (tileEntity != null) {
                try {
                    tileEntity.loadNbt(tileEntityTag);
                } catch (Exception ex) {
                    GlowServer.logger.log(Level.SEVERE, "Error loading TileEntity at " + tileEntity.getBlock(), ex);
                }
            }
        }

        return true;
    }
