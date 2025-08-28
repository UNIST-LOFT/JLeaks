	public void encode(final Object object, OutputStream outputStream) {
		Assert.notNull(object, "cannot encode a null object");
		Assert.notNull(outputStream, "'outputSteam' cannot be null");
		final Output output = (outputStream instanceof Output ? (Output) outputStream : new Output(outputStream));
		this.pool.run(kryo -> {
			doEncode(kryo, object, output);
			return Void.class;
		});
		output.close();
	}
