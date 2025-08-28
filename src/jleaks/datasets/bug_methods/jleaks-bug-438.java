    public static void download(String source_url, File target_file) throws IOException {
        byte[] buffer = new byte[2048];
        ClientConnection connection = new ClientConnection(source_url);
        OutputStream os = new BufferedOutputStream(new FileOutputStream(target_file));
        int count;
        try {
            while ((count = connection.inputStream.read(buffer)) > 0) os.write(buffer, 0, count);
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.close();
        os.close();
    }
