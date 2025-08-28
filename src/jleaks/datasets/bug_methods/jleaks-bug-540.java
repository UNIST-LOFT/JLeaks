	public String getOriginalSourceCode() {
		try {
			if (originalSourceCode == null) {
				FileInputStream s = new FileInputStream(getFile());
				byte[] elementBytes = new byte[s.available()];
				s.read(elementBytes);
				s.close();
				originalSourceCode = new String(elementBytes, this.getFactory().getEnvironment().getEncoding());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return originalSourceCode;
	}
