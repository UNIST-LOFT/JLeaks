public void handleMessage(Message<?> message) throws MessagingException 
{
    String sessionId = resolveSessionId(message);
    if (sessionId == null) {
        logger.error("sessionId not found in message " + message);
        return;
    }
    WebSocketSession session = this.sessions.get(sessionId);
    if (session == null) {
        logger.error("Session not found for session with id " + sessionId);
        return;
    }
    try {
        findProtocolHandler(session).handleMessageToClient(session, message);
    } catch (SessionLimitExceededException e) {
        try {
            logger.error("Terminating session id '" + sessionId + "'", e);
            // Session may be unresponsive so clear first
            clearSession(session, CloseStatus.NO_STATUS_CODE);
            session.close();
        } catch (Exception secondException) {
            logger.error("Exception terminating session id '" + sessionId + "'", secondException);
        }
    } catch (Exception e) {
        logger.error("Failed to send message to client " + message, e);
    }
}