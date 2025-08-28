    protected void doHandle(final Channel channel, final MessageInputStream message) {
        log.tracef("%s handling incoming data", this);
        final SimpleDataInput input = new SimpleDataInput(Marshalling.createByteInput(message));
        Exception error = null;
        ManagementRequestHeader requestHeader = null;
        ManagementRequestHandler requestHandler = null;
        try {
            ManagementProtocolHeader header = ManagementProtocolHeader.parse(input);
            if (header.isRequest()) {
                requestHeader = (ManagementRequestHeader)header;
                requestHandler = requestReceiver.readRequest(requestHeader, input);
            } else {
                responseReceiver.handleResponse((ManagementResponseHeader)header, input);
            }
        } catch (Exception e) {
            error = e;
            log.tracef(e, "%s error handling incoming data", this);
        } finally {
            log.tracef("%s done handling incoming data", this);
            try {
                //Consume the rest of the output if any
                while (input.read() != -1) {
                }

            } catch (IOException ignore) {
            }
            IoUtils.safeClose(input);
            IoUtils.safeClose(message);
        }

        if (requestHeader != null) {
            if (error == null) {
                try {
                    requestReceiver.processRequest(requestHeader, requestHandler);
                } catch (Exception e) {
                    error = e;
                }
            }

            if (error != null) {
                //TODO temporary debug stack
                error.printStackTrace();
            }
            requestReceiver.writeResponse(requestHeader, requestHandler, error);
        }
    }
