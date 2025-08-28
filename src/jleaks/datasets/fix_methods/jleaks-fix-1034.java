private void writeBootstrapPage(VaadinResponse response, String html){
    response.setContentType("text/html");
    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"))) {
        writer.append(html);
    }
}