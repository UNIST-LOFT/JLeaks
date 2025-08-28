public SignalServiceMessagesResult getMessages() throws IOException 
{
    try (Response response = makeServiceRequest(String.format(MESSAGE_PATH, ""), "GET", (RequestBody) null, NO_HEADERS, NO_HANDLER, Optional.absent())) {
        validateServiceResponse(response);
        List<SignalServiceEnvelopeEntity> envelopes = readBodyJson(response.body(), SignalServiceEnvelopeEntityList.class).getMessages();
        long serverDeliveredTimestamp = 0;
        try {
            String stringValue = response.header(SERVER_DELIVERED_TIMESTAMP_HEADER);
            stringValue = stringValue != null ? stringValue : "0";
            serverDeliveredTimestamp = Long.parseLong(stringValue);
        } catch (NumberFormatException e) {
            Log.w(TAG, e);
        }
        return new SignalServiceMessagesResult(envelopes, serverDeliveredTimestamp);
    }
}