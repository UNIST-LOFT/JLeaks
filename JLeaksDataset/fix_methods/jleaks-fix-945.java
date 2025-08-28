public void beginTransaction() throws ProducerFencedException 
{
    try {
        this.delegate.beginTransaction();
    } catch (RuntimeException e) {
        this.txFailed = true;
        logger.error("Illegal transaction state; producer removed from cache; possible cause: " + "broker restarted during transaction", e);
        try {
            this.delegate.close();
        } catch (Exception ee) {
            // empty
        }
        throw e;
    }
}