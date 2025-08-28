protected DataBuffer read() throws IOException 
{
    return this.dataBuffer.read(destination, offset, length);
}