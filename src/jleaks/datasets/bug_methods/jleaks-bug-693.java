	public static String getStreamContents(InputStream ins, String charsetName)
			throws IOException {
		UtfBomAwareReader reader;

        if (ins  == null) {
            throw new IOException("No stream to open");
        }

		if (charsetName == null) {
			reader = new UtfBomAwareReader(ins);
		} else {
			String charsetCanonicalName = Charset.forName(charsetName).name();
			String encoding;

			reader = new UtfBomAwareReader(ins, charsetName);
			encoding = Charset.forName(reader.getEncoding()).name();

			if (charsetCanonicalName.startsWith("UTF")
					&& !charsetCanonicalName.equals(encoding)) {
				reader.close();
				throw new IllegalArgumentException("Expected encoding was '"
						+ charsetCanonicalName + "' but a BOM was detected for '"
						+ encoding + "'");
			}
		}
		return getReaderContents(reader);
	}
