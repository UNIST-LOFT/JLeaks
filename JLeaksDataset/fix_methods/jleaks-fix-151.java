public Future<FragInstanceDispatchResult> dispatch(List<FragmentInstance> instances) 
{
    return executor.submit(() -> {
        TSendFragmentInstanceResp resp = new TSendFragmentInstanceResp(false);
        for (FragmentInstance instance : instances) {
            SyncDataNodeInternalServiceClient client = null;
            TEndPoint endPoint = new TEndPoint(instance.getHostEndpoint().getIp(), IoTDBDescriptor.getInstance().getConfig().getInternalPort());
            try {
                // TODO: (jackie tien) change the port
                client = internalServiceClientManager.borrowClient(endPoint);
                if (client == null) {
                    throw new TException("Can't get client for node " + endPoint);
                }
                // TODO: (xingtanzjr) consider how to handle the buffer here
                ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
                instance.serializeRequest(buffer);
                buffer.flip();
                TConsensusGroupId groupId = instance.getRegionReplicaSet().getRegionId();
                TSendFragmentInstanceReq req = new TSendFragmentInstanceReq(new TFragmentInstance(buffer), groupId, instance.getType().toString());
                resp = client.sendFragmentInstance(req);
            } catch (IOException e) {
                LOGGER.error("can't connect to node {}", endPoint, e);
                throw e;
            } catch (TException e) {
                LOGGER.error("sendFragmentInstance failed for node {}", endPoint, e);
                if (client != null) {
                    client.close();
                }
                throw e;
            } finally {
                if (client != null) {
                    client.returnSelf();
                }
            }
            if (!resp.accepted) {
                break;
            }
        }
        return new FragInstanceDispatchResult(resp.accepted);
    });
}