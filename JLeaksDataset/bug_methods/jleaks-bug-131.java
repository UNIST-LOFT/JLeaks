  HTableDescriptor[] getHTableDescriptors(List<TableName> tableNames) {
    HTableDescriptor[] htd = new HTableDescriptor[0];
    try {
      LOG.info("getHTableDescriptors == tableNames => " + tableNames);
      htd = new HBaseAdmin(getConf()).getTableDescriptorsByTableName(tableNames);
    } catch (IOException e) {
      LOG.debug("Exception getting table descriptors", e);
    }
    return htd;
  }
