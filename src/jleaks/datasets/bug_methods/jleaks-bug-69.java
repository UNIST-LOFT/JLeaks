    protected void init() throws IOException {
        _selector = Selector.open();
        SocketChannel sch = null;
        InetSocketAddress addr = null;

        try {
            sch = SocketChannel.open();
            sch.configureBlocking(true);
            s_logger.info("Connecting to " + _host + ":" + _port);

            if (_bindAddress != null) {
                s_logger.info("Binding outbound interface at " + _bindAddress);

                addr = new InetSocketAddress(_bindAddress, 0);
                sch.socket().bind(addr);
            }

            addr = new InetSocketAddress(_host, _port);
            sch.connect(addr);
        } catch (IOException e) {
            _selector.close();
            throw e;
        }

        SSLEngine sslEngine = null;
        try {
            // Begin SSL handshake in BLOCKING mode
            sch.configureBlocking(true);

            SSLContext sslContext = Link.initSSLContext(true);
            sslEngine = sslContext.createSSLEngine(_host, _port);
            sslEngine.setUseClientMode(true);

            Link.doHandshake(sch, sslEngine, true);
            s_logger.info("SSL: Handshake done");
            s_logger.info("Connected to " + _host + ":" + _port);
        } catch (Exception e) {
            _selector.close();
            throw new IOException("SSL: Fail to init SSL! " + e);
        }

        Task task = null;
        try {
            sch.configureBlocking(false);
            Link link = new Link(addr, this);
            link.setSSLEngine(sslEngine);
            SelectionKey key = sch.register(_selector, SelectionKey.OP_READ);
            link.setKey(key);
            key.attach(link);
            // Notice we've already connected due to the handshake, so let's get the
            // remaining task done
            task = _factory.create(Task.Type.CONNECT, link, null);
        } catch (Exception e) {
            _selector.close();
            throw new IOException("Fail to init NioClient! " + e);
        }
        _executor.execute(task);
    }
