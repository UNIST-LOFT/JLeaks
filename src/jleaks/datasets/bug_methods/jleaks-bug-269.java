  private void deleteFlowConfigs(Id.Flow flowId) throws Exception {
    // It's a bit hacky here since we know how the HBaseConsumerStateStore works.
    // Maybe we need another Dataset set that works across all queues.
    final QueueName prefixName = QueueName.from(URI.create(
      QueueName.prefixForFlow(flowId)));

    Id.DatasetInstance stateStoreId = getStateStoreId(flowId.getNamespaceId());
    Map<String, String> args = ImmutableMap.of(HBaseQueueDatasetModule.PROPERTY_QUEUE_NAME, prefixName.toString());
    HBaseConsumerStateStore stateStore = datasetFramework.getDataset(stateStoreId, args, null);
    if (stateStore == null) {
      // If the state store doesn't exists, meaning there is no queue, hence nothing to do.
      return;
    }

    final Table table = stateStore.getInternalTable();
    Transactions.createTransactionExecutor(txExecutorFactory, (TransactionAware) table)
      .execute(new TransactionExecutor.Subroutine() {
        @Override
        public void apply() throws Exception {
          // Prefix name is "/" terminated ("queue:///namespace/app/flow/"), hence the scan is unique for the flow
          byte[] startRow = Bytes.toBytes(prefixName.toString());
          Scanner scanner = table.scan(startRow, Bytes.stopKeyForPrefix(startRow));
          try {
            Row row = scanner.next();
            while (row != null) {
              table.delete(row.getRow());
              row = scanner.next();
            }
          } finally {
            scanner.close();
          }
        }
      });
  }
