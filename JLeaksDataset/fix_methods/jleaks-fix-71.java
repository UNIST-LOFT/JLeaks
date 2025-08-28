private void addExtractTemplateAndVolumeColumns(Connection conn) 
{
    try (PreparedStatement selectTemplateInfostmt = conn.prepareStatement("SELECT *  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud' AND TABLE_NAME = 'template_store_ref' AND COLUMN_NAME = 'download_url_created'");
        ResultSet templateInfoResults = selectTemplateInfostmt.executeQuery();
        PreparedStatement addDownloadUrlCreatedToTemplateStorerefstatement = conn.prepareStatement("ALTER TABLE `cloud`.`template_store_ref` ADD COLUMN `download_url_created` datetime");
        PreparedStatement addDownloadUrlToTemplateStorerefstatement = conn.prepareStatement("ALTER TABLE `cloud`.`template_store_ref` ADD COLUMN `download_url` varchar(255)");
        PreparedStatement selectVolumeInfostmt = conn.prepareStatement("SELECT *  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud' AND TABLE_NAME = 'volume_store_ref' AND COLUMN_NAME = 'download_url_created'");
        ResultSet volumeInfoResults = selectVolumeInfostmt.executeQuery();
        PreparedStatement addDownloadUrlCreatedToVolumeStorerefstatement = conn.prepareStatement("ALTER TABLE `cloud`.`volume_store_ref` ADD COLUMN `download_url_created` datetime")) {
        // Add download_url_created, download_url to template_store_ref
        if (!templateInfoResults.next()) {
            addDownloadUrlCreatedToTemplateStorerefstatement.executeUpdate();
            addDownloadUrlToTemplateStorerefstatement.executeUpdate();
        }
        // Add download_url_created to volume_store_ref - note download_url already exists
        if (!volumeInfoResults.next()) {
            addDownloadUrlCreatedToVolumeStorerefstatement.executeUpdate();
        }
    } catch (SQLException e) {
        throw new CloudRuntimeException("Adding columns for Extract Template And Volume functionality failed");
    }
}