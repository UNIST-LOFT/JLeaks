public long checkin(long poid, String comment, long deserializerOid, boolean merge, Flow flow, URL url) throws UserException, ServerException, IOException
{
    try {
        InputStream openStream = url.openStream();
        if (flow == Flow.SYNC) {
            try {
                long topicId = channel.checkin(baseAddress, token, poid, comment, deserializerOid, merge, flow, -1, url.toString(), openStream);
                SLongActionState progress = getNotificationRegistryInterface().getProgress(topicId);
                if (progress.getState() == SActionState.AS_ERROR) {
                    throw new UserException(Joiner.on(", ").join(progress.getErrors()));
                } else {
                    return topicId;
                }
            } finally {
                openStream.close();
            }
        } else {
            long topicId = channel.checkin(baseAddress, token, poid, comment, deserializerOid, merge, flow, -1, url.toString(), openStream);
            return topicId;
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    return -1;
}