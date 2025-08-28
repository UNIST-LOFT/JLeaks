	public static HttpTunnelPayload get(HttpInputMessage message) throws IOException {
		long length = message.getHeaders().getContentLength();
		if (length <= 0) {
			return null;
		}
		String seqHeader = message.getHeaders().getFirst(SEQ_HEADER);
		Assert.state(StringUtils.hasLength(seqHeader), "Missing sequence header");
		ReadableByteChannel body = Channels.newChannel(message.getBody());
		ByteBuffer payload = ByteBuffer.allocate((int) length);
		while (payload.hasRemaining()) {
			body.read(payload);
		}
		body.close();
		payload.flip();
		return new HttpTunnelPayload(Long.valueOf(seqHeader), payload);
	}
