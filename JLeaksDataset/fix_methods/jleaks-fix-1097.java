public void onSuccess(Object filePathOption) throws Throwable {
	String filePath = (String) filePathOption;
	File file = new File(filePath);
	final RandomAccessFile raf;
	try {
		raf = new RandomAccessFile(file, "r");
	} catch (FileNotFoundException e) {
		display(ctx, request, "Displaying TaskManager log failed.");
		LOG.error("Displaying TaskManager log failed.", e);
		return;
	}
	long fileLength;
	try {
		fileLength = raf.length();
	} catch (IOException ioe) {
		display(ctx, request, "Displaying TaskManager log failed.");
		LOG.error("Displaying TaskManager log failed.", ioe);
		raf.close();
		throw ioe;
	}
	final FileChannel fc = raf.getChannel();

	HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
	response.headers().set(CONTENT_TYPE, "text/plain");
	if (HttpHeaders.isKeepAlive(request)) {
		response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
	}
	HttpHeaders.setContentLength(response, fileLength);
	// write the initial line and the header.
	ctx.write(response);
	// write the content.
	ctx.write(new DefaultFileRegion(fc, 0, fileLength), ctx.newProgressivePromise())
		.addListener(new GenericFutureListener<io.netty.util.concurrent.Future<? super Void>>() {
			@Override
			public void operationComplete(io.netty.util.concurrent.Future<? super Void> future) throws Exception {
				lastRequestPending.remove(taskManagerID);
				fc.close();
				raf.close();
			}
		});
	ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
	// close the connection, if no keep-alive is needed
	if (!HttpHeaders.isKeepAlive(request)) {
		lastContentFuture.addListener(ChannelFutureListener.CLOSE);
	}
}