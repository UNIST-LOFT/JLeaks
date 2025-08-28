private void copyStream(HttpServletResponse servletResponse, InputStream is) throws IOException 
{
    if (servletResponse != null && is != null) {
        try {
            OutputStream os = servletResponse.getOutputStream();
            byte[] buffer = new byte[8096];
            int read = 0;
            while ((read = is.read(buffer)) > 0) {
                os.write(buffer, 0, read);
            }
            os.flush();
        } catch (IOException ex) {
            throw ex;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Logger.error("Cannot close input stream.", e);
            }
        }
    }
}