protected void doAccept(final Selector selector, ServerSocketChannel server, long now) throws IOException 
{
    logger.debug("New accept");
    SocketChannel channel = server.accept();
    if (this.isShuttingDown()) {
        if (logger.isInfoEnabled()) {
            logger.info("New connection from " + channel.socket().getInetAddress().getHostAddress() + " rejected; the server is in the process of shutting down.");
        }
        channel.close();
    } else {
        try {
            channel.configureBlocking(false);
            Socket socket = channel.socket();
            setSocketAttributes(socket);
            TcpNioConnection connection = createTcpNioConnection(channel);
            if (connection == null) {
                return;
            }
            connection.setTaskExecutor(this.getTaskExecutor());
            connection.setLastRead(now);
            this.channelMap.put(channel, connection);
            channel.register(selector, SelectionKey.OP_READ, connection);
        } catch (Exception e) {
            logger.error("Exception accepting new connection", e);
            channel.close();
        }
    }
}