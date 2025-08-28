public byte[] read(InputStream is) throws IOException 
{
    if (charReader != null)
        return charReader.read();
    else if (byteReader != null)
        return byteReader.read();
    else
        return -1;
}