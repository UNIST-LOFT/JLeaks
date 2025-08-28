public void addFile(String jarPath, File file, ApplicationModule module)
				throws IOException, CancelledException {
			if (!file.exists()) {
				throw new AssertException(
					"Attempted to write a file that does not exist to the jar! File = " +
						file.getAbsolutePath());
			}
			if (!file.isFile()) {
				throw new AssertException(
					"Attempted to write a directory to the jar! File = " + file.getAbsolutePath());
			}
			jarPath = jarPath.replaceAll("\\\\", "/"); // handle windows separators
			long modifiedTime = file.lastModified();
			addToModuleTree(jarPath, module);
			if (extensionPointSuffixPattern.matcher(jarPath).matches()) {
				try (FileInputStream inStream = new FileInputStream(file)) {
					checkExtensionPointClass(jarPath, inStream);
				}
			}

			if (prefix != null) {
				jarPath = prefix + jarPath;
			}
			if (jarPath.contains("..")) {
				jarPath = Path.of(jarPath).normalize().toString();
			}
			ZipEntry entry = new ZipEntry(jarPath);
			entry.setTime(modifiedTime);
			try {
				jarOut.putNextEntry(entry);
			}
			catch (ZipException e) {
				System.out.println(e.getMessage());
				return;
			}

			try (InputStream in = new FileInputStream(file)) {

				byte[] bytes = new byte[4096];
				int numRead;

				while ((numRead = in.read(bytes)) != -1) {
					monitor.checkCanceled();
					jarOut.write(bytes, 0, numRead);
				}
			}

			jarOut.closeEntry();

		}