static public void copyToFile(byte[] src, SFile dest) throws IOException 
{
    try (OutputStream fos = dest.createBufferedOutputStream()) {
        fos.write(src);
    }
}