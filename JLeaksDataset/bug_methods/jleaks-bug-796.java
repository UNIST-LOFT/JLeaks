    public void writeXref(File xrefDir, String path) throws IOException {
        RuntimeEnvironment env = RuntimeEnvironment.getInstance();

        if (env.hasProjects()) {
            project = Project.getProject(path);
        } else {
            project = null;
        }

        Writer out = null;
        if (env.isCompressXref()) {
            out = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(new File(xrefDir, path + ".gz")))));
        } else {
            out = new BufferedWriter(new FileWriter(new File(xrefDir, path)));
        }
	writeXref(out);
	out.close();
    }
