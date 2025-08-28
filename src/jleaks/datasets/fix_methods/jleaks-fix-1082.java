
	@Override
	protected void generateAPIFiles(ApiImplementor imp) throws IOException {
		String className = imp.getPrefix().substring(0, 1).toUpperCase() + imp.getPrefix().substring(1);
	
		Path file = getDirectory().resolve(className + ".cs");
		System.out.println("Generating " + file.toAbsolutePath());
		try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
			out.write(HEADER);
			out.write("\n\n");
			
			out.write("using System;\n");
			out.write("using System.Collections.Generic;\n");
			out.write("using System.Text;\n");
			out.write("\n");
			
			out.write("\n");
			out.write("/*\n");
			out.write(" * This file was automatically generated.\n");
			out.write(" */\n");
			out.write("namespace OWASPZAPDotNetAPI.Generated\n");
			out.write("{\n");
			out.write("\tpublic class " + className + " \n\t{");
			
			out.write("\n\t\tprivate ClientApi api = null;\n\n");
			out.write("\t\tpublic " + className + "(ClientApi api) \n\t\t{\n");
			out.write("\t\t\tthis.api = api;\n");
			out.write("\t\t}\n\n");
	
			for (ApiElement view : imp.getApiViews()) {
				this.generateCSharpElement(view, imp.getPrefix(), VIEW_ENDPOINT, out);
			}
			for (ApiElement action : imp.getApiActions()) {
				this.generateCSharpElement(action, imp.getPrefix(), ACTION_ENDPOINT, out);
			}
			for (ApiElement other : imp.getApiOthers()) {
				this.generateCSharpElement(other, imp.getPrefix(), OTHER_ENDPOINT, out);
			}
			out.write("\t}\n");
			out.write("}\n");
		}
	}