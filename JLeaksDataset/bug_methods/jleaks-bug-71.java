    private void addExtractTemplateAndVolumeColumns(Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            // Add download_url_created, download_url to template_store_ref
            pstmt = conn.prepareStatement("SELECT *  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud' AND TABLE_NAME = 'template_store_ref' AND COLUMN_NAME = 'download_url_created'");
            rs = pstmt.executeQuery();
            if (!rs.next()) {
                pstmt = conn.prepareStatement("ALTER TABLE `cloud`.`template_store_ref` ADD COLUMN `download_url_created` datetime");
                pstmt.executeUpdate();

                pstmt = conn.prepareStatement("ALTER TABLE `cloud`.`template_store_ref` ADD COLUMN `download_url` varchar(255)");
                pstmt.executeUpdate();
            }

            // Add download_url_created to volume_store_ref - note download_url already exists
            pstmt = conn.prepareStatement("SELECT *  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud' AND TABLE_NAME = 'volume_store_ref' AND COLUMN_NAME = 'download_url_created'");
            rs = pstmt.executeQuery();
            if (!rs.next()) {
                pstmt = conn.prepareStatement("ALTER TABLE `cloud`.`volume_store_ref` ADD COLUMN `download_url_created` datetime");
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new CloudRuntimeException("Adding columns for Extract Template And Volume functionality failed");
        }
    }
