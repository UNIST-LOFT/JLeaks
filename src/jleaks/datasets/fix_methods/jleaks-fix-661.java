	public void testInfiniteStream() throws Exception {
		executeHystrixCommand(); // Execute a Hystrix command so that metrics are initialized.
		EventInput stream = getStream(); // Invoke Stream API which returns a steady stream output.
		try {
			validateStream(stream, 1000); // Validate the stream.
			System.out.println("Validated Stream Output 1");
			executeHystrixCommand(); // Execute Hystrix Command again so that request count is updated.
			validateStream(stream, 1000); // Stream should show updated request count
			System.out.println("Validated Stream Output 2");
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}