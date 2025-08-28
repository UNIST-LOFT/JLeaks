    public void dispose() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        subscriptions.dispose();
        endpointStateSink.complete();
        messageSink.complete();
        tokenManager.close();
        handler.close();
    }
