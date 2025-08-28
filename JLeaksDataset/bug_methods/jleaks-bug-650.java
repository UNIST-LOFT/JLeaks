	public static void saveAsJar(ArrayList<ClassNode> nodeList, String path,
			String manifest) {
		try {
			JarOutputStream out = new JarOutputStream(
					new FileOutputStream(path));
			for (ClassNode cn : nodeList) {
				ClassWriter cw = new ClassWriter(0);
				cn.accept(cw);

				out.putNextEntry(new ZipEntry(cn.name + ".class"));
				out.write(cw.toByteArray());
				out.closeEntry();
			}

			out.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
			out.write((manifest.trim() + "\r\n\r\n").getBytes());
			out.closeEntry();

			for(FileContainer container : BytecodeViewer.files)
				for (Entry<String, byte[]> entry : container.files.entrySet()) {
					String filename = entry.getKey();
					if (!filename.startsWith("META-INF")) {
						out.putNextEntry(new ZipEntry(filename));
						out.write(entry.getValue());
						out.closeEntry();
					}
				}

			out.close();
		} catch (IOException e) {
			new the.bytecode.club.bytecodeviewer.api.ExceptionUI(e);
		}
	}
