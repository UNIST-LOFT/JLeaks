
	public void assignTo(HttpOutputMessage message) throws IOException {
		Assert.notNull(message, "Message must not be null");
		HttpHeaders headers = message.getHeaders();
		headers.setContentLength(this.data.remaining());
		headers.add(SEQ_HEADER, Long.toString(getSequence()));
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		try (WritableByteChannel body = Channels.newChannel(message.getBody())) {
			while (this.data.hasRemaining()) {
				body.write(this.data);
			}
		}
	}