private ObservableOnSubscribe<Object> receiveLoop() 
{
    Message message;
    try {
        while (!closed.get() && (message = consumer.receive(receiverTimeout)) != null) {
            streamStep.logDebug(message.toString());
            acceptRows(singletonList(Arrays.asList(message.getBody(Object.class), jmsDelegate.destinationName)));
        }
    } catch (JMSRuntimeException | JMSException jmsException) {
        error(jmsException);
    } finally {
        super.close();
        if (!closed.get()) {
            close();
            streamStep.logBasic(getString(PKG, "JmsStreamSource.HitReceiveTimeout"));
        }
    }
}