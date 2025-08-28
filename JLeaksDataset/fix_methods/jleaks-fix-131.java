HTableDescriptor[] getHTableDescriptors(List<TableName> tableNames) 
{
    HTableDescriptor[] htd = new HTableDescriptor[0];
    HBaseAdmin admin = null;
    try {
        LOG.info("getHTableDescriptors == tableNames => " + tableNames);
        admin = new HBaseAdmin(getConf());
        htd = admin.getTableDescriptorsByTableName(tableNames);
    } catch (IOException e) {
        LOG.debug("Exception getting table descriptors", e);
    } finally {
        if (admin != null) {
            try {
                admin.close();
            } catch (IOException e) {
                LOG.debug("Exception closing HBaseAdmin", e);
            }
        }
    }
    return htd;
}