    public void create() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(this.file, "rw");
        this.channel = raf.getChannel();
        this.buffer = this.channel.map(FileChannel.MapMode.READ_WRITE, 0, this.capacity);
        raf.close();
        buffer.position(0);
        buffer.put(VERSION_ONE);
        this.head = 1;
        this.minSeqNum = 0L;
        this.elementCount = 0;
    }
