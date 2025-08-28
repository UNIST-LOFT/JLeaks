    private byte[] download(String textUrl) {

        URL url = buildURLForConnection(textUrl);
        HttpURLConnection connection = getHttpConnection(url);
        BufferedInputStream bis;
        ByteArrayOutputStream baos;
        byte[] buffer = null;
        int len;

        if(!urlExists(connection)) {
            return null;
        }

        try {
            bis = new BufferedInputStream(connection.getInputStream(), 8192);
            baos = new ByteArrayOutputStream();
            buffer = new byte[1024];
            while ((len = bis.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            buffer = baos.toByteArray();
        } catch (Exception e) {
            e(CoverAsyncHelper.class.getSimpleName(), "Failed to download cover :" + e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return buffer;
    }
