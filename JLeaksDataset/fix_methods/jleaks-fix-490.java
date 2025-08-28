protected void innerClose() throws IOException 
{
    if (altoChannels != null) {
        for (Channel altoChannel : altoChannels) {
            try {
                altoChannel.close();
            } catch (Exception e) {
                logger.warning("Exception while closing Alto channel " + e.getMessage());
            }
        }
    }
    channel.close();
}