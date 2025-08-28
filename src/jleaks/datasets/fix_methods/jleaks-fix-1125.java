private static void download(NimbusClient client, String file, String localFile) throws IOException, TException, AuthorizationException {
        WritableByteChannel out = Channels.newChannel(new FileOutputStream(localFile));
        try {
            String id = client.getClient().beginFileDownload(file);
	        while(true) {
	            ByteBuffer chunk = client.getClient().downloadChunk(id);
	            int written = out.write(chunk);
	            if(written==0) break;
	        }
        } finally {
        	out.close();
        }
    }