    private Connection createConnection() throws RemoteException {
        Connection conn;

        TCPTransport.tcpLog.log(Log.BRIEF, "create connection");

        if (!usingMultiplexer) {
            Socket sock = ep.newSocket();
            conn = new TCPConnection(this, sock);

            try {
                DataOutputStream out =
                    new DataOutputStream(conn.getOutputStream());
                writeTransportHeader(out);

                // choose protocol (single op if not reusable socket)
                if (!conn.isReusable()) {
                    out.writeByte(TransportConstants.SingleOpProtocol);
                } else {
                    out.writeByte(TransportConstants.StreamProtocol);
                    out.flush();

                    /*
                     * Set socket read timeout to configured value for JRMP
                     * connection handshake; this also serves to guard against
                     * non-JRMP servers that do not respond (see 4322806).
                     */
                    int originalSoTimeout = 0;
                    try {
                        originalSoTimeout = sock.getSoTimeout();
                        sock.setSoTimeout(handshakeTimeout);
                    } catch (Exception e) {
                        // if we fail to set this, ignore and proceed anyway
                    }

                    DataInputStream in =
                        new DataInputStream(conn.getInputStream());
                    byte ack = in.readByte();
                    if (ack != TransportConstants.ProtocolAck) {
                        throw new ConnectIOException(
                            ack == TransportConstants.ProtocolNack ?
                            "JRMP StreamProtocol not supported by server" :
                            "non-JRMP server at remote endpoint");
                    }

                    String suggestedHost = in.readUTF();
                    int    suggestedPort = in.readInt();
                    if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE)) {
                        TCPTransport.tcpLog.log(Log.VERBOSE,
                            "server suggested " + suggestedHost + ":" +
                            suggestedPort);
                    }

                    // set local host name, if unknown
                    TCPEndpoint.setLocalHost(suggestedHost);
                    // do NOT set the default port, because we don't
                    // know if we can't listen YET...

                    // write out default endpoint to match protocol
                    // (but it serves no purpose)
                    TCPEndpoint localEp =
                        TCPEndpoint.getLocalEndpoint(0, null, null);
                    out.writeUTF(localEp.getHost());
                    out.writeInt(localEp.getPort());
                    if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE)) {
                        TCPTransport.tcpLog.log(Log.VERBOSE, "using " +
                            localEp.getHost() + ":" + localEp.getPort());
                    }

                    /*
                     * After JRMP handshake, set socket read timeout to value
                     * configured for the rest of the lifetime of the
                     * connection.  NOTE: this timeout, if configured to a
                     * finite duration, places an upper bound on the time
                     * that a remote method call is permitted to execute.
                     */
                    try {
                        /*
                         * If socket factory had set a non-zero timeout on its
                         * own, then restore it instead of using the property-
                         * configured value.
                         */
                        sock.setSoTimeout((originalSoTimeout != 0 ?
                                           originalSoTimeout :
                                           responseTimeout));
                    } catch (Exception e) {
                        // if we fail to set this, ignore and proceed anyway
                    }

                    out.flush();
                }
            } catch (IOException e) {
                if (e instanceof RemoteException)
                    throw (RemoteException) e;
                else
                    throw new ConnectIOException(
                        "error during JRMP connection establishment", e);
            }
        } else {
            try {
                conn = multiplexer.openConnection();
            } catch (IOException e) {
                synchronized (this) {
                    usingMultiplexer = false;
                    multiplexer = null;
                }
                throw new ConnectIOException(
                    "error opening virtual connection " +
                    "over multiplexed connection", e);
            }
        }
        return conn;
    }
