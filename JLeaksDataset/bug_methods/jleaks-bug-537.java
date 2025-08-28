	public static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream so = new ObjectOutputStream(bo);
		so.writeObject(obj);
		so.flush();
		byte[] serializedObject = bo.toByteArray();
		so.close();
		return serializedObject;
	}
