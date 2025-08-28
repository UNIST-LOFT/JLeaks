private String httpGet(HttpUrl url) throws IOException 
{
    Request request = new Request.Builder().url(url).get().build();
    return makeCall(request);
}