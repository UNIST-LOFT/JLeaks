public Mono<Void> transferTo(File dest) 
{
    if (this.storage == null || !getFilename().isPresent()) {
        return Mono.error(new IllegalStateException("The part does not represent a file."));
    }
    ReadableByteChannel input = null;
    FileChannel output = null;
    try {
        input = Channels.newChannel(this.storage.getInputStream());
        output = new FileOutputStream(destination).getChannel();
        long size = (input instanceof FileChannel ? ((FileChannel) input).size() : Long.MAX_VALUE);
        long totalWritten = 0;
        while (totalWritten < size) {
            long written = output.transferFrom(input, totalWritten, size - totalWritten);
            if (written <= 0) {
                break;
            }
            totalWritten += written;
        }
    } catch (IOException ex) {
        return Mono.error(ex);
    } finally {
        if (input != null) {
            try {
                input.close();
            } catch (IOException ignored) {
            }
        }
        if (output != null) {
            try {
                output.close();
            } catch (IOException ignored) {
            }
        }
    }
    return Mono.empty();
}