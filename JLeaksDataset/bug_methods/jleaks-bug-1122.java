    public void close() throws IOException, InterruptedException {
        this.serverSocketChannel.close();
        closeSocketChannels();
        acceptorThread.interrupt();
        acceptorThread.join();
        interrupt();
        join();
    }
