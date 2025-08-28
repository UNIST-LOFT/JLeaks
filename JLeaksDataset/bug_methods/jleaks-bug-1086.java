	private void generatePythonComponent(ApiImplementor imp) throws IOException {
		File f = new File(this.dir, createFileName(imp.getPrefix()));
		System.out.println("Generating " + f.getAbsolutePath());
		FileWriter out = new FileWriter(f);
		out.write(HEADER);
		out.write("class " + safeName(imp.getPrefix()) + "(object):\n\n");
		out.write("    def __init__(self, zap):\n");
		out.write("        self.zap = zap\n");
		out.write("\n");
		
		for (ApiElement view : imp.getApiViews()) {
			this.generatePythonElement(view, imp.getPrefix(), "view", out);
		}
		for (ApiElement action : imp.getApiActions()) {
			this.generatePythonElement(action, imp.getPrefix(), "action", out);
		}
		for (ApiElement other : imp.getApiOthers()) {
			this.generatePythonElement(other, imp.getPrefix(), "other", out);
		}
		out.write("\n");
		out.close();
	}
