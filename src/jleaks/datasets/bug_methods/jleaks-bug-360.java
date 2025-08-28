    public static Font createFont(int fontFormat, InputStream fontStream)
        throws java.awt.FontFormatException, java.io.IOException {

        if (fontFormat != Font.TRUETYPE_FONT &&
            fontFormat != Font.TYPE1_FONT) {
            throw new IllegalArgumentException ("font format not recognized");
        }
        final InputStream fStream = fontStream;
        Object ret = java.security.AccessController.doPrivileged(
           new java.security.PrivilegedAction() {
              public Object run() {
                  File tFile = null;
                  FileOutputStream outStream = null;
                  try {
                      tFile = File.createTempFile("+~JF", ".tmp", null);
                      /* Temp file deleted by font shutdown hook */
                      BufferedInputStream inStream =
                          new BufferedInputStream(fStream);
                      outStream = new FileOutputStream(tFile);
                      int bytesRead = 0;
                      int bufSize = 8192;
                      byte [] buf = new byte[bufSize];
                      while (bytesRead != -1) {
                          try {
                              bytesRead = inStream.read(buf, 0, bufSize);
                          } catch (Throwable t) {
                              throw new IOException();
                          }
                          if (bytesRead != -1) {
                              outStream.write(buf, 0, bytesRead);
                          }
                      }
                      /* don't close the input stream */
                      outStream.close();
                  } catch (IOException e) {
                      if (outStream != null) {
                          try {
                              outStream.close();
                          } catch (Exception e1) {
                          }
                      }
                      if (tFile != null) {
                          try {
                              tFile.delete();
                          }  catch (Exception e2) {
                          }
                      }
                      return e;
                  }
                  return tFile;
              }
          });

        if (ret instanceof File) {
            return new Font((File)ret, fontFormat, true);
        } else if (ret instanceof IOException) {
            throw (IOException)ret;
        } else {
            throw new FontFormatException("Couldn't access font stream");
        }
    }
