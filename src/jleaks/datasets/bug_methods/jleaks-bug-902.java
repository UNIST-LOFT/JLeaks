    public static void main(String[] args) throws Exception {
        NetworkParameters params = NetworkParameters.prodNet();
        FullPrunedBlockStore store = new H2FullPrunedBlockStore(params, "toy-full.blockchain", 100);
        FullPrunedBlockChain chain = new FullPrunedBlockChain(params, store);
        
        String defaultDataDir;
        if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
            defaultDataDir = System.getenv("APPDATA") + "\\.bitcoin/";
        } else {
            defaultDataDir = System.getProperty("user.home") + "/.bitcoin/";
        }
        
        // TODO: Move this to a library function
        FileInputStream stream = new FileInputStream(new File(defaultDataDir + "blk0001.dat"));
        int i = 0;
        while (stream.available() > 0) {
            try {
                while(stream.read() != ((params.packetMagic >>> 24) & 0xff) || stream.read() != ((params.packetMagic >>> 16) & 0xff) ||
                        stream.read() != ((params.packetMagic >>> 8) & 0xff) || stream.read() != (params.packetMagic & 0xff))
                    ;
            } catch (IOException e) {
                break;
            }
            byte[] bytes = new byte[4];
            stream.read(bytes, 0, 4);
            long size = Utils.readUint32BE(Utils.reverseBytes(bytes), 0);
            if (size > Block.MAX_BLOCK_SIZE || size <= 0)
                continue;
            bytes = new byte[(int) size];
            stream.read(bytes, 0, (int)size);
            Block block = new Block(params, bytes);
            if (store.get(block.getHash()) == null)
                chain.add(block);
            
            if (i % 10000 == 0)
                System.out.println(i);
            i++;
        }
        System.out.println("Imported " + chain.getChainHead().getHeight() + " blocks.");
    }
