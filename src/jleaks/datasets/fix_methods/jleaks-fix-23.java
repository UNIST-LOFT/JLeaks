public void close() throws IOException 
{
    if (mClosed) {
        return;
    }
    Protocol.LocalBlockCloseRequest request = Protocol.LocalBlockCloseRequest.newBuilder().setBlockId(mBlockId).build();
    try {
        NettyRPC.call(NettyRPCContext.defaults().setChannel(mChannel).setTimeout(READ_TIMEOUT_MS), new ProtoMessage(request));
    } catch (Exception e) {
        mChannel.close();
        throw e;
    } finally {
        mClosed = true;
        mContext.releaseNettyChannel(mAddress, mChannel);
    }
}