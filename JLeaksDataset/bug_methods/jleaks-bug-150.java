  protected FragmentInstanceState fetchState(FragmentInstance instance) throws TException {
    // TODO (jackie tien) change the port
    InternalService.Iface client =
        InternalServiceClientFactory.getInternalServiceClient(
            new TEndPoint(
                instance.getHostEndpoint().getIp(),
                IoTDBDescriptor.getInstance().getConfig().getInternalPort()));
    TFragmentInstanceStateResp resp =
        client.fetchFragmentInstanceState(new TFetchFragmentInstanceStateReq(getTId(instance)));
    return FragmentInstanceState.valueOf(resp.state);
  }
