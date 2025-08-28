    private OutputStream sendOutput(Request request, HttpsURLConnection connection) throws IOException {
        final byte[] dataToSend = request.dataToSend();
        if (dataToSend != null && dataToSend.length > 0) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Length", String.valueOf(dataToSend.length));
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(dataToSend);
            return outputStream;
        }
        return null;
    }
