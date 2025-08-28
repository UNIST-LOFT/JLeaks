    public void writeStringResponse(VaadinResponse response, String contentType,
            String reponseString) throws IOException {

        response.setContentType(contentType);

        final OutputStream out = response.getOutputStream();
        final PrintWriter outWriter = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(out, "UTF-8")));
        outWriter.print(reponseString);
        outWriter.close();
    }
