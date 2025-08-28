  public Future<FragInstanceDispatchResult> dispatch(List<FragmentInstance> instances) {
    return executor.submit(
        () -> {
          TSendFragmentInstanceResp resp = new TSendFragmentInstanceResp(false);
          for (FragmentInstance instance : instances) {
            // TODO: (jackie tien) change the port
            InternalService.Iface client =
                InternalServiceClientFactory.getInternalServiceClient(
                    new TEndPoint(
                        instance.getHostEndpoint().getIp(),
                        IoTDBDescriptor.getInstance().getConfig().getInternalPort()));
            // TODO: (xingtanzjr) consider how to handle the buffer here
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            instance.serializeRequest(buffer);
            buffer.flip();
            TConsensusGroupId groupId = instance.getRegionReplicaSet().getRegionId();
            TSendFragmentInstanceReq req =
                new TSendFragmentInstanceReq(
                    new TFragmentInstance(buffer), groupId, instance.getType().toString());
            resp = client.sendFragmentInstance(req);
            if (!resp.accepted) {
              break;
            }
          }
          return new FragInstanceDispatchResult(resp.accepted);
        });
  }
