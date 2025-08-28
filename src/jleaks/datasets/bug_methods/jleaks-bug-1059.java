    public ClientRequestExecutor create(SocketDestination dest) throws Exception {
        int numCreated = created.incrementAndGet();

        if(logger.isDebugEnabled())
            logger.debug("Creating socket " + numCreated + " for " + dest.getHost() + ":"
                         + dest.getPort() + " using protocol "
                         + dest.getRequestFormatType().getCode());

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.socket().setReceiveBufferSize(this.socketBufferSize);
        socketChannel.socket().setSendBufferSize(this.socketBufferSize);
        socketChannel.socket().setTcpNoDelay(true);
        socketChannel.socket().setSoTimeout(soTimeoutMs);
        socketChannel.socket().setKeepAlive(this.socketKeepAlive);
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(dest.getHost(), dest.getPort()));

        long finishConnectTimeoutMs = System.currentTimeMillis() + connectTimeoutMs;

        // Since we're non-blocking and it takes a non-zero amount of time
        // to connect, invoke finishConnect and loop.
        while(!socketChannel.finishConnect()) {
            long diff = finishConnectTimeoutMs - System.currentTimeMillis();

            if(diff < 0)
                throw new ConnectException("Cannot connect socket " + numCreated + " for "
                                           + dest.getHost() + ":" + dest.getPort() + " after "
                                           + connectTimeoutMs + " ms");

            if(logger.isTraceEnabled())
                logger.trace("Still creating socket " + numCreated + " for " + dest.getHost() + ":"
                             + dest.getPort() + ", " + diff + " ms. remaining to connect");

            // Break up the connection timeout into chunks N/10 of the
            // total.
            try {
                Thread.sleep(connectTimeoutMs / 10);
            } catch(InterruptedException e) {
                if(logger.isEnabledFor(Level.WARN))
                    logger.warn(e, e);
            }
        }

        if(logger.isDebugEnabled())
            logger.debug("Created socket " + numCreated + " for " + dest.getHost() + ":"
                         + dest.getPort() + " using protocol "
                         + dest.getRequestFormatType().getCode());

        // check buffer sizes--you often don't get out what you put in!
        if(socketChannel.socket().getReceiveBufferSize() != this.socketBufferSize)
            logger.debug("Requested socket receive buffer size was " + this.socketBufferSize
                         + " bytes but actual size is "
                         + socketChannel.socket().getReceiveBufferSize() + " bytes.");

        if(socketChannel.socket().getSendBufferSize() != this.socketBufferSize)
            logger.debug("Requested socket send buffer size was " + this.socketBufferSize
                         + " bytes but actual size is "
                         + socketChannel.socket().getSendBufferSize() + " bytes.");

        ClientRequestSelectorManager selectorManager = selectorManagers[counter.getAndIncrement()
                                                                        % selectorManagers.length];

        Selector selector = selectorManager.getSelector();
        ClientRequestExecutor clientRequestExecutor = new ClientRequestExecutor(selector,
                                                                                socketChannel,
                                                                                socketBufferSize);
        BlockingClientRequest<String> clientRequest = new BlockingClientRequest<String>(new ProtocolNegotiatorClientRequest(dest.getRequestFormatType()),
                                                                                        this.getTimeout());
        clientRequestExecutor.addClientRequest(clientRequest);

        selectorManager.registrationQueue.add(clientRequestExecutor);
        selector.wakeup();

        // Block while we wait for the protocol negotiation to complete.
        clientRequest.await();

        // This will throw an error if the result of the protocol negotiation
        // failed, otherwise it returns an uninteresting token we can safely
        // ignore.
        clientRequest.getResult();

        return clientRequestExecutor;
    }
