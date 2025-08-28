    public void dispose() {
        //according to this: http://download.oracle.com/javase/1.5.0/docs/guide/net/http-keepalive.html
        //should read all data from connection to make it happy.
        byte[] bytes = new byte[1024];
        try {
            InputStream in = connection.getInputStream();
            if(in != null){
                while ((in.read(bytes)) > 0) {}//do nothing
                in.close();
            }
        } catch (Exception ignore) {
            try {
                InputStream errorStream = connection.getErrorStream();
                if(errorStream != null){
                    while ((errorStream.read(bytes)) > 0) {}//do nothing
                    errorStream.close();
                }
            } catch (IOException ignoreToo) {}
        }
    }
