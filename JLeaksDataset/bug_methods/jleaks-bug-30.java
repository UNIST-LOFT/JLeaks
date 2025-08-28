private void downloadLogFile(File file, HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
    long len = file.length();
    InputStream is = new FileInputStream(file);
    String fileName = file.getName();
    response.setContentType("application/octet-stream");
    if (len <= Integer.MAX_VALUE) {
      response.setContentLength((int) len);
    } else {
      response.addHeader("Content-Length", Long.toString(len));
    }
    response.addHeader("Content-Disposition", "attachment;filename=" + fileName);

    ServletOutputStream out = null;
    try {
      out = response.getOutputStream();
      ByteStreams.copy(is, out);
    } finally {
      if (out != null) {
        out.flush();
        out.close();
      }
      if (is != null) {
        is.close();
      }
    }
  }