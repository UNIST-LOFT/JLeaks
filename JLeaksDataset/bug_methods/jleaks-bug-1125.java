    private static void download(NimbusClient client, String file, String localFile) throws IOException, TException, AuthorizationException {
        String id = client.getClient().beginFileDownload(file);
        WritableByteChannel out = Channels.newChannel(new FileOutputStream(localFile));
        while(true) {
            ByteBuffer chunk = client.getClient().downloadChunk(id);
            int written = out.write(chunk);
            if(written==0) break;
        }
        out.close();
    }
