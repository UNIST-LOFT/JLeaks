@Override public void close() throws IOException 
{
    if (inputSize > 0) {
        deflater.setInput(inputBuf, 0, inputSize);
        deflater.finish();
        inputSize = 0;
        deflate();
    }
    if (outputSize > 0) {
        writeChunk();
    }
    deflater.end();
}