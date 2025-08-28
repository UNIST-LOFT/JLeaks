    public void close() {
        if (hasEntity()) {
            try {
                getEntityStream().flush();
                getEntityStream().close();

                // In case some of the output stream wrapper does not delegate close() call we
                // close the root stream manually to make sure it commits the data.
                committingOutputStream.close();
            } catch (IOException e) {
                // Happens when the client closed connection before receiving the full response.
                // This is OK and not interesting in vast majority of the cases
                // hence the log level set to FINE to make sure it does not flood the log unnecessarily
                // (especially for clients disconnecting from SSE listening, which is very common).
                Logger.getLogger(OutboundMessageContext.class.getName()).log(Level.FINE, e.getMessage(), e);
            }
        }
    }
