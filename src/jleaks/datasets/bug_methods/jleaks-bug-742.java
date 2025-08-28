	public byte[] read(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int len = 0;
        try {
            while ((len = is.read(buffer)) == 0) {
            }
        } catch (SocketException e) {
            // do not pollute the log with a stacktrace, log only the message
            logger.debug("Socket exception occured: " + e.getMessage());
            return null;
        } catch (SocketTimeoutException e) {
            logger.debug("Socket timeout, returning null.");
            return null;
        }
        if (len == -1) {
            return null;
        } else {
            do {
                baos.write(buffer, 0, len);
                if (len < buffer.length) {
                    break;
                }
                int av = is.available();
                if (av == 0) {
                    break;
                }
            } while ((len = is.read(buffer)) > 0);

            baos.flush();
            baos.close();
            return baos.toByteArray();
        }
	}
