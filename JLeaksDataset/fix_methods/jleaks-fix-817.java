public void close() throws Exception 
{
    // Make sure we run close logic only once
    if (isClosed.compareAndSet(false, true)) {
        if ((request != null) && (request.isUpgrade())) {
            HttpUpgradeHandler httpUpgradeHandler = request.getHttpUpgradeHandler();
            Exception exception = null;
            try {
                httpUpgradeHandler.destroy();
                request.setUpgrade(false);
                if (response != null) {
                    response.setUpgrade(false);
                }
            } finally {
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
                (request.getContext()).fireContainerEvent(ContainerEvent.PRE_DESTROY, httpUpgradeHandler);
                request.getCoyoteRequest().getResponse().resume();
            }
            if (exception != null) {
                throw exception;
            }
        }
    }
}