private File downloadReplicationFile(String fileName, URL baseUrl)
{
    URL changesetUrl;
    try {
        changesetUrl = new URL(baseUrl, fileName);
    } catch (MalformedURLException e) {
        throw new OsmosisRuntimeException("The server file URL could not be created.", e);
    }
    try {
        File outputFile;
        // Open an input stream for the changeset file on the server.
        URLConnection connection = changesetUrl.openConnection();
        // timeout 15 minutes
        connection.setReadTimeout(15 * 60 * 1000);
        // timeout 15 minutes
        connection.setConnectTimeout(15 * 60 * 1000);
        try (BufferedInputStream source = new BufferedInputStream(connection.getInputStream(), 65536)) {
            // Create a temporary file to write the data to.
            outputFile = File.createTempFile("change", null);
            // Open a output stream for the destination file.
            try (BufferedOutputStream sink = new BufferedOutputStream(new FileOutputStream(outputFile), 65536)) {
                // Download the file.
                byte[] buffer = new byte[65536];
                for (int bytesRead = source.read(buffer); bytesRead > 0; bytesRead = source.read(buffer)) {
                    sink.write(buffer, 0, bytesRead);
                }
            }
        }
        return outputFile;
    } catch (IOException e) {
        throw new OsmosisRuntimeException("Unable to read the changeset file " + fileName + " from the server.", e);
    }
}