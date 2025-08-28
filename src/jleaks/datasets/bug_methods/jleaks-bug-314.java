        public void run() {
            while (!finished) {
                try {
                    ListIterator<HttpConnection> li =
                        connsToRegister.listIterator();
                    for (HttpConnection c : connsToRegister) {
                        reRegister(c);
                    }
                    connsToRegister.clear();

                    List<Event> list = null;
                    selector.select(1000);
                    synchronized (lolock) {
                        if (events.size() > 0) {
                            list = events;
                            events = new LinkedList<Event>();
                        }
                    }

                    if (list != null) {
                        for (Event r: list) {
                            handleEvent (r);
                        }
                    }

                    /* process the selected list now  */

                    Set<SelectionKey> selected = selector.selectedKeys();
                    Iterator<SelectionKey> iter = selected.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove ();
                        if (key.equals (listenerKey)) {
                            if (terminating) {
                                continue;
                            }
                            SocketChannel chan = schan.accept();

                            // Set TCP_NODELAY, if appropriate
                            if (ServerConfig.noDelay()) {
                                chan.socket().setTcpNoDelay(true);
                            }

                            if (chan == null) {
                                continue; /* cancel something ? */
                            }
                            chan.configureBlocking (false);
                            SelectionKey newkey = chan.register (selector, SelectionKey.OP_READ);
                            HttpConnection c = new HttpConnection ();
                            c.selectionKey = newkey;
                            c.setChannel (chan);
                            newkey.attach (c);
                            requestStarted (c);
                            allConnections.add (c);
                        } else {
                            try {
                                if (key.isReadable()) {
                                    boolean closed;
                                    SocketChannel chan = (SocketChannel)key.channel();
                                    HttpConnection conn = (HttpConnection)key.attachment();

                                    key.cancel();
                                    chan.configureBlocking (true);
                                    if (idleConnections.remove(conn)) {
                                        // was an idle connection so add it
                                        // to reqConnections set.
                                        requestStarted (conn);
                                    }
                                    handle (chan, conn);
                                } else {
                                    assert false;
                                }
                            } catch (CancelledKeyException e) {
                                handleException(key, null);
                            } catch (IOException e) {
                                handleException(key, e);
                            }
                        }
                    }
                    // call the selector just to process the cancelled keys
                    selector.selectNow();
                } catch (IOException e) {
                    logger.log (Level.FINER, "Dispatcher (4)", e);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.log (Level.FINER, "Dispatcher (7)", e);
                }
            }
        }
