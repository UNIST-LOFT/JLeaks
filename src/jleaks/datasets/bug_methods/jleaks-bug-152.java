  public Future<Boolean> terminate() {
    List<TEndPoint> relatedHost = getRelatedHost(fragmentInstances);

    return executor.submit(
        () -> {
          try {
            for (TEndPoint endpoint : relatedHost) {
              // TODO (jackie tien) change the port
              InternalService.Iface client =
                  InternalServiceClientFactory.getInternalServiceClient(
                      new TEndPoint(
                          endpoint.getIp(),
                          IoTDBDescriptor.getInstance().getConfig().getInternalPort()));
              client.cancelQuery(new TCancelQueryReq(queryId.getId()));
            }
          } catch (TException e) {
            return false;
          }
          return true;
        });
  }
