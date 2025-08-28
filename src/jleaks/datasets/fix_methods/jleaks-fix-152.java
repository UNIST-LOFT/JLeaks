public Future<Boolean> terminate() 
{
    List<TEndPoint> relatedHost = getRelatedHost(fragmentInstances);
    return executor.submit(() -> {
        for (TEndPoint endPoint : relatedHost) {
            // TODO (jackie tien) change the port
            SyncDataNodeInternalServiceClient client = null;
            try {
                client = internalServiceClientManager.borrowClient(new TEndPoint(endPoint.getIp(), IoTDBDescriptor.getInstance().getConfig().getInternalPort()));
                if (client == null) {
                    throw new TException("Can't get client for node " + endPoint);
                }
                client.cancelQuery(new TCancelQueryReq(queryId.getId()));
            } catch (IOException e) {
                LOGGER.error("can't connect to node {}", endPoint, e);
                return false;
            } catch (TException e) {
                LOGGER.error("cancelQuery failed for node {}", endPoint, e);
                if (client != null) {
                    client.close();
                }
                return false;
            } finally {
                if (client != null) {
                    client.returnSelf();
                }
            }
        }
        return true;
    });
}