    private void sendOutput(Request request, HttpsURLConnection connection) throws IOException {
        final byte[] dataToSend = request.dataToSend();
        if (dataToSend != null && dataToSend.length > 0) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Length", String.valueOf(dataToSend.length));
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(dataToSend);
                outputStream.flush();
            }
        }
    }
