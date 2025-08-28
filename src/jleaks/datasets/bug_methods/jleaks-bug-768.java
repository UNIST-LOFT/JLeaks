
	public long checkin(long poid, String comment, long deserializerOid, boolean merge, Flow flow, URL url) throws UserException, ServerException, IOException {
		InputStream openStream = url.openStream();
		if (flow == Flow.SYNC) {
			long topicId = channel.checkin(baseAddress, token, poid, comment, deserializerOid, merge, flow, -1, url.toString(), openStream);
			openStream.close();
			
			SLongActionState progress = getNotificationRegistryInterface().getProgress(topicId);
			if (progress.getState() == SActionState.AS_ERROR) {
				throw new UserException(Joiner.on(", ").join(progress.getErrors()));
			} else {
				return topicId;
			}
		} else {
			long topicId = channel.checkin(baseAddress, token, poid, comment, deserializerOid, merge, flow, -1, url.toString(), openStream);
			return topicId;
		}
	}
