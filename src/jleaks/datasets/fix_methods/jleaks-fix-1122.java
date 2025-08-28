public void close() throws IOException, InterruptedException {
        this.serverSocketChannel.close();
        closeSocketChannels();
        Utils.closeQuietly(selector, "selector");
        acceptorThread.interrupt();
        acceptorThread.join();
        interrupt();
        join();
    }