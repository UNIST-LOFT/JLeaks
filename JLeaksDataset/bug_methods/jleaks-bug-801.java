  public OExecutionStream internalStart(OCommandContext ctx) throws OTimeoutException {
    getPrev().ifPresent(x -> x.start(ctx));
    Stream<OResult[]> stream = null;
    OResult[] productTuple = new OResult[this.subPlans.size()];

    for (int i = 0; i < this.subPlans.size(); i++) {
      OInternalExecutionPlan ep = this.subPlans.get(i);
      final int pos = i;
      if (stream == null) {
        stream =
            ep.start().stream(ctx)
                .map(
                    (value) -> {
                      productTuple[pos] = value;
                      return productTuple;
                    });
      } else {
        stream =
            stream.flatMap(
                (val) -> {
                  return ep.start().stream(ctx)
                      .map(
                          (value) -> {
                            val[pos] = value;
                            return val;
                          });
                });
      }
    }
    Stream<OResult> finalStream = stream.map(this::produceResult);
    return OExecutionStream.resultIterator(finalStream.iterator());
  }
