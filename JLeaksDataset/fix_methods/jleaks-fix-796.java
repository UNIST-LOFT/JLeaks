public void writeXref(File xrefDir, String path) throws IOException 
{
    RuntimeEnvironment env = RuntimeEnvironment.getInstance();
    if (env.hasProjects()) {
        project = Project.getProject(path);
    } else {
        project = null;
    }
    final boolean compressed = env.isCompressXref();
    final File file = new File(xrefDir, path + (compressed ? ".gz" : ""));
    OutputStream out = new FileOutputStream(file);
    try {
        if (compressed) {
            out = new GZIPOutputStream(out);
        }
        writeXref(new BufferedWriter(new OutputStreamWriter(out)));
    } finally {
        out.close();
    }
}