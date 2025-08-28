protected void init() throws IOException 
{
    _selector = Selector.open();
    Task task = null;
    try (SocketChannel sch = SocketChannel.open()) {
        sch.configureBlocking(true);
        s_logger.info("Connecting to " + _host + ":" + _port);
        if (_bindAddress != null) {
            s_logger.info("Binding outbound interface at " + _bindAddress);
            InetSocketAddress bindAddr = new InetSocketAddress(_bindAddress, 0);
            sch.socket().bind(bindAddr);
        }
        InetSocketAddress peerAddr = new InetSocketAddress(_host, _port);
        sch.connect(peerAddr);
        SSLEngine sslEngine = null;
        // Begin SSL handshake in BLOCKING mode
        sch.configureBlocking(true);
        SSLContext sslContext = Link.initSSLContext(true);
        sslEngine = sslContext.createSSLEngine(_host, _port);
        sslEngine.setUseClientMode(true);
        Link.doHandshake(sch, sslEngine, true);
        s_logger.info("SSL: Handshake done");
        s_logger.info("Connected to " + _host + ":" + _port);
        sch.configureBlocking(false);
        Link link = new Link(peerAddr, this);
        link.setSSLEngine(sslEngine);
        SelectionKey key = sch.register(_selector, SelectionKey.OP_READ);
        link.setKey(key);
        key.attach(link);
        // Notice we've already connected due to the handshake, so let's get the
        // remaining task done
        task = _factory.create(Task.Type.CONNECT, link, null);
    } catch (GeneralSecurityException e) {
        throw new IOException("Failed to initialise security", e);
    } finally {
        _selector.close();
    }
    _executor.execute(task);
}