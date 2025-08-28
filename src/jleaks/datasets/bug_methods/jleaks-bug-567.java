	public static <T extends XMLSerializable> void write(final T object, final OutputStream out) 
		throws XMLStreamException 
	{
		Validator.notNull(out, "Output stream");
		Validator.notNull(object, "Object");
		
		final XMLObjectWriter writer = XMLObjectWriter.newInstance(
			new NonClosableOutputStream(out)
		);
		writer.setIndentation("\t");
		writer.write(object);
		writer.close();
	}
