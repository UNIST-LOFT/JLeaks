	private int makeJar(File mvn, boolean overwrite) {
		File jar = new File(mvn, "lombok-bootstrap.jar");
		if (jar.exists() && !overwrite) {
			System.err.println(canonical(jar) + " but '-w' not specified.");
			return 1;
		}
		try {
			InputStream input = this.getClass().getResourceAsStream("/lombok/eclipse/agent/lombok-bootstrap.jar");
			FileOutputStream output = new FileOutputStream(jar);
			byte[] buffer = new byte[4096];
			int length;
			while ((length = input.read(buffer)) > 0) output.write(buffer, 0, length);
			output.flush();
			output.close();
			System.out.println("Successfully created: " + canonical(jar));
			return 0;
		} catch (Exception e) {
			System.err.println("Could not create: " + canonical(jar));
			e.printStackTrace(System.err);
			return 1;
		}
	}
	
	private static String canonical(File out) {
		try {
			return out.getCanonicalPath();
		} catch (Exception e) {
			return out.getAbsolutePath();
		}
	}
