private void downloadLogFile(File file, HttpServletRequest request,
HttpServletResponse response) throws IOException {
    long len = file.length();
    String fileName = file.getName();
    response.setContentType("application/octet-stream");
    if (len <= Integer.MAX_VALUE) {
        response.setContentLength((int) len);
    } else {
        response.addHeader("Content-Length", Long.toString(len));
    }
    response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
    InputStream is = new FileInputStream(file);
    try {
        ServletOutputStream out = response.getOutputStream();
        try {
            ByteStreams.copy(is, out);
        } finally {
            try {
                out.flush();
            } finally {
                out.close();
            }
        }
    } finally {
        is.close();
    }
}