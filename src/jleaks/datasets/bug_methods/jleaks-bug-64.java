    private void createStorageIpEntires(TransactionLegacy txn, long rangeId, String startIp, String endIp, long zoneId) throws SQLException {
        long startIPLong = NetUtils.ip2Long(startIp);
        long endIPLong = NetUtils.ip2Long(endIp);
        String insertSql =
            "INSERT INTO `cloud`.`op_dc_storage_network_ip_address` (range_id, ip_address, mac_address, taken) VALUES (?, ?, (select mac_address from `cloud`.`data_center` where id=?), ?)";
        String updateSql = "UPDATE `cloud`.`data_center` set mac_address = mac_address+1 where id=?";
        PreparedStatement stmt = null;
        Connection conn = txn.getConnection();

        while (startIPLong <= endIPLong) {
            stmt = conn.prepareStatement(insertSql);
            stmt.setLong(1, rangeId);
            stmt.setString(2, NetUtils.long2Ip(startIPLong++));
            stmt.setLong(3, zoneId);
            stmt.setNull(4, java.sql.Types.DATE);
            stmt.executeUpdate();
            stmt.close();

            stmt = txn.prepareStatement(updateSql);
            stmt.setLong(1, zoneId);
            stmt.executeUpdate();
            stmt.close();
        }
    }
