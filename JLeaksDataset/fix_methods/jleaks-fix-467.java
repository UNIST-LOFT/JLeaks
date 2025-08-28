public void encode(StreamingContent content, OutputStream out) throws IOException 
{
    // must not close the underlying output stream
    OutputStream out2 = new FilterOutputStream(out) {

        @Override
        public void close() throws IOException {
            // copy implementation of super.close(), except do not close the underlying output stream
            try {
                flush();
            } catch (IOException ignored) {
            }
        }
    };
    GZIPOutputStream zipper = new GZIPOutputStream(out2);
    content.writeTo(zipper);
    // cannot call just zipper.finish() because that would cause a severe memory leak
    zipper.close();
}