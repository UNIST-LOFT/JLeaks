  public void run() {
    ConnectionTable.threadWantsSharedResources();
    if (logger.isTraceEnabled(LogMarker.DM)) {
      logger.trace(LogMarker.DM, "Starting P2P Listener on  {}", id);
    }
    for (;;) {
      SystemFailure.checkFailure();
      if (stopper.isCancelInProgress()) {
        break;
      }
      if (stopped) {
        break;
      }
      if (Thread.currentThread().isInterrupted()) {
        break;
      }
      if (stopper.isCancelInProgress()) {
        break; // part of bug 37271
      }

      Socket othersock = null;
      try {
        if (this.useNIO) {
          SocketChannel otherChannel = channel.accept();
          othersock = otherChannel.socket();
        } else {
          try {
            othersock = socket.accept();
          } catch (SSLException ex) {
            // SW: This is the case when there is a problem in P2P
            // SSL configuration, so need to exit otherwise goes into an
            // infinite loop just filling the logs
            logger.warn(
                LocalizedMessage.create(
                    LocalizedStrings.TCPConduit_STOPPING_P2P_LISTENER_DUE_TO_SSL_CONFIGURATION_PROBLEM),
                ex);
            break;
          }
          socketCreator.configureServerSSLSocket(othersock);
        }
        if (stopped) {
          try {
            if (othersock != null) {
              othersock.close();
            }
          } catch (Exception e) {
          }
          continue;
        }

        acceptConnection(othersock);

      } catch (ClosedByInterruptException cbie) {
        // safe to ignore
      } catch (ClosedChannelException e) {
        break; // we're dead
      } catch (CancelException e) {
        break;
      } catch (Exception e) {
        if (!stopped) {
          if (e instanceof SocketException && "Socket closed".equalsIgnoreCase(e.getMessage())) {
            // safe to ignore; see bug 31156
            if (!socket.isClosed()) {
              logger.warn(
                  LocalizedMessage.create(
                      LocalizedStrings.TCPConduit_SERVERSOCKET_THREW_SOCKET_CLOSED_EXCEPTION_BUT_SAYS_IT_IS_NOT_CLOSED),
                  e);
              try {
                socket.close();
                createServerSocket();
              } catch (IOException ioe) {
                logger.fatal(
                    LocalizedMessage.create(
                        LocalizedStrings.TCPConduit_UNABLE_TO_CLOSE_AND_RECREATE_SERVER_SOCKET),
                    ioe);
                // post 5.1.0x, this should force shutdown
                try {
                  Thread.sleep(5000);
                } catch (InterruptedException ie) {
                  // Don't reset; we're just exiting the thread
                  logger.info(LocalizedMessage.create(
                      LocalizedStrings.TCPConduit_INTERRUPTED_AND_EXITING_WHILE_TRYING_TO_RECREATE_LISTENER_SOCKETS));
                  return;
                }
              }
            }
          } else {
            this.getStats().incFailedAccept();
            if (e instanceof IOException && "Too many open files".equals(e.getMessage())) {
              getConTable().fileDescriptorsExhausted();
            } else {
              logger.warn(e.getMessage(), e);
            }
          }
        }
        // connections.cleanupLowWater();
      }
      if (!stopped && socket.isClosed()) {
        // NOTE: do not check for distributed system closing here. Messaging
        // may need to occur during the closing of the DS or cache
        logger.warn(
            LocalizedMessage.create(LocalizedStrings.TCPConduit_SERVERSOCKET_CLOSED_REOPENING));
        try {
          createServerSocket();
        } catch (ConnectionException ex) {
          logger.warn(ex.getMessage(), ex);
        }
      }
    } // for

    if (logger.isTraceEnabled(LogMarker.DM)) {
      logger.debug("Stopped P2P Listener on  {}", id);
    }
  }
