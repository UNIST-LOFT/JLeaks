public void create() throws IOException 
{
    try (RandomAccessFile raf = new RandomAccessFile(this.file, "rw")) {
        this.buffer = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, this.capacity);
    }
    buffer.position(0);
    buffer.put(VERSION_ONE);
    this.head = 1;
    this.minSeqNum = 0L;
    this.elementCount = 0;
}