	public String getDocumentContent() {
		StringBuilder out = new StringBuilder();
		try {
			InputStream in = file.getContents();
			byte[] b = new byte[4096];
			for (int n; (n = in.read(b)) != -1;) {
				out.append(new String(b, 0, n));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return out.toString();
	}
