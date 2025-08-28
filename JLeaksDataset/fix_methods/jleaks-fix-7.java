public void send(Event event, String to, List<String> cc, String subject, String text,
Optional<String> html, Attachment... attachment) {
    String apiKey = configurationManager.getRequiredValue(Configuration.from(event.getOrganizationId(), event.getId(), MAILGUN_KEY));
    String domain = configurationManager.getRequiredValue(Configuration.from(event.getOrganizationId(), event.getId(), MAILGUN_DOMAIN));
    try {
        RequestBody formBody = prepareBody(event, to, cc, subject, text, html, attachment);
        Request request = new Request.Builder().url("https://api.mailgun.net/v2/" + domain + "/messages").header("Authorization", Credentials.basic("api", apiKey)).post(formBody).build();
        try (Response resp = client.newCall(request).execute()) {
            if (!resp.isSuccessful()) {
                log.warn("sending email was not successful:" + resp);
            }
        }
    } catch (IOException e) {
        log.warn("error while sending email", e);
    }
}