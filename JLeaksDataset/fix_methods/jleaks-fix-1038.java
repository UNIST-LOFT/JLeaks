protected void sendUploadResponse(VaadinRequest request,
VaadinResponse response) throws IOException {
    response.setContentType("text/html");
    try (OutputStream out = response.getOutputStream()) {
        final PrintWriter outWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, "UTF-8")));
        outWriter.print("<html><body>download handled</body></html>");
        outWriter.flush();
    }
}