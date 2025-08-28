private void streamFile(String urlStr, OutputStream outStream) throws IOException 
{
    if (BuildConfig.DEBUG)
        Log.d(TAG, "URL Stream Data:   " + urlStr);
    Request request = new Request.Builder().url(urlStr).build();
    OkHttpClient httpClient = HttpClient.getInstance().newBuilder().connectTimeout(2, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
    Response response = httpClient.newCall(request).execute();
    try {
        ResponseBody responseBody = response.body();
        InputStream inContent = responseBody.byteStream();
        boolean recordActive = false;
        if (fileOutputStream != null) {
            recordActive = true;
        }
        byte[] bufContent = new byte[1000];
        while (!isStopped) {
            int bytesRead = inContent.read(bufContent);
            if (bytesRead < 0) {
                break;
            }
            connectionBytesTotal += bytesRead;
            outStream.write(bufContent, 0, bytesRead);
            if ((fileOutputStream != null) && recordActive) {
                Log.v(TAG, "writing to record file..");
                fileOutputStream.write(bufContent, 0, bytesRead);
            }
        }
    } catch (Exception e) {
        response.close();
        throw e;
    }
}