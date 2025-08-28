	private void generateCSharpComponent(ApiImplementor imp) throws IOException {
		String className = imp.getPrefix().substring(0, 1).toUpperCase() + imp.getPrefix().substring(1);
	
		File f = new File(this.dir, className + ".cs");
		System.out.println("Generating " + f.getAbsolutePath());
		FileWriter out = new FileWriter(f);
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
			this.generateCSharpElement(view, imp.getPrefix(), "view", out);
		}
		for (ApiElement action : imp.getApiActions()) {
			this.generateCSharpElement(action, imp.getPrefix(), "action", out);
		}
		for (ApiElement other : imp.getApiOthers()) {
			this.generateCSharpElement(other, imp.getPrefix(), "other", out);
		}
		out.write("\t}\n");
		out.write("}\n");
		out.close();
	}
