protected FragmentInstanceState fetchState(FragmentInstance instance) throws TException 
{
    SyncDataNodeInternalServiceClient client = null;
    try {
        // TODO: (jackie tien) change the port
        TEndPoint endPoint = new TEndPoint(instance.getHostEndpoint().getIp(), IoTDBDescriptor.getInstance().getConfig().getInternalPort());
        client = internalServiceClientManager.borrowClient(endPoint);
        if (client == null) {
            throw new TException("Can't get client for node " + endPoint);
        }
        TFragmentInstanceStateResp resp = client.fetchFragmentInstanceState(new TFetchFragmentInstanceStateReq(getTId(instance)));
        return FragmentInstanceState.valueOf(resp.state);
    } catch (Throwable t) {
        if (t instanceof TException && client != null) {
            client.close();
        }
        throw t;
    } finally {
        if (client != null) {
            client.returnSelf();
        }
    }
}