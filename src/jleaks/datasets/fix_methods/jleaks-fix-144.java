public void syncTableMetadata() 
{
    // Open up the metadata table again, for syncing
    try (HoodieTableMetadataWriter writer = SparkHoodieBackedTableMetadataWriter.create(hadoopConf, config, context)) {
        LOG.info("Successfully synced to metadata table");
    } catch (Exception e) {
        throw new HoodieMetadataException("Error syncing to metadata table.", e);
    }
}