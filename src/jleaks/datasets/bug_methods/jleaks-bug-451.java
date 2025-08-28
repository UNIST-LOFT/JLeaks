	public static byte [] readContent(File file) {
		byte [] buffer = new byte[(int) file.length()];
		try {
			BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
			is.read(buffer,  0,  buffer.length);
			is.close();
		} catch (Throwable t) {
			System.err.println("Failed to read byte content of " + file.getAbsolutePath());
			t.printStackTrace();
		}
		return buffer;
	}
