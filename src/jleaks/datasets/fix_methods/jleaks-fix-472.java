public String getDocumentContent() 
{
    StringBuilder out = new StringBuilder();
    try {
        InputStream in = file.getContents();
        try {
            byte[] b = new byte[4096];
            for (int n; (n = in.read(b)) != -1; ) {
                out.append(new String(b, 0, n));
            }
        } catch (IOException e) {
            GroovyCore.logException(e.getMessage(), e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
    } catch (CoreException e) {
        GroovyCore.logException(e.getMessage(), e);
    }
    return out.toString();
}