public void stopServer() 
{
    if (server == null) {
        // Create and execute our Digester
        Digester digester = createStopDigester();
        digester.setClassLoader(Thread.currentThread().getContextClassLoader());
        File file = configFile();
        FileInputStream fis = null;
        try {
            InputSource is = new InputSource("file://" + file.getAbsolutePath());
            fis = new FileInputStream(file);
            is.setByteStream(fis);
            digester.push(this);
            digester.parse(is);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Catalina.stop: ", e);
            System.exit(1);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ioe) {
            }
        }
    }
    // Stop the existing server
    Socket socket = null;
    OutputStream stream = null;
    try {
        socket = new Socket("127.0.0.1", server.getPort());
        stream = socket.getOutputStream();
        String shutdown = server.getShutdown();
        for (int i = 0; i < shutdown.length(); i++) stream.write(shutdown.charAt(i));
        stream.flush();
    } catch (IOException e) {
        log.log(Level.SEVERE, "Catalina.stop: ", e);
        System.exit(1);
    } finally {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}