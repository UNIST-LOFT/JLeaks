public static <T extends XMLSerializable> void write(final T object, final OutputStream out) 
		throws XMLStreamException 
	{
		Validator.notNull(out, "Output stream");
		Validator.notNull(object, "Object");

		XMLObjectWriter writer = null;
		try {
			writer = XMLObjectWriter.newInstance(
				new NonClosableOutputStream(out)
			);
			writer.setIndentation("\t");
			writer.write(object);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}