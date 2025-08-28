public void dispose() 
{
    if (isDisposed.getAndSet(true)) {
        return;
    }
    subscriptions.dispose();
    endpointStateSink.complete();
    messageSink.complete();
    tokenManager.close();
    receiver.close();
    try {
        dispatcher.invoke(() -> {
            receiver.free();
            handler.close();
        });
    } catch (IOException e) {
        logger.warning("Could not schedule disposing of receiver on ReactorDispatcher.", e);
        handler.close();
    }
}