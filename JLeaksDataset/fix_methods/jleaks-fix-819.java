public void close() throws Exception 
{
    // Make sure we run close logic only once
    if (isClosed.compareAndSet(false, true)) {
        if ((request != null) && (request.isUpgrade())) {
            HttpUpgradeHandler httpUpgradeHandler = request.getHttpUpgradeHandler();
            try {
                httpUpgradeHandler.destroy();
            } finally {
                ((StandardContext) request.getContext()).fireContainerEvent(ContainerEvent.PRE_DESTROY, httpUpgradeHandler);
                request.getCoyoteRequest().getResponse().resume();
            }
            Exception exception = null;
            try {
                inputStream.close();
            } catch (Exception ex) {
                exception = ex;
            }
            try {
                outputStream.close();
            } catch (Exception ex) {
                exception = ex;
            }
            if (exception != null) {
                throw exception;
            }
        }
    }
}