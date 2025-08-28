private void createStorageIpEntires(TransactionLegacy txn, long rangeId, String startIp, String endIp, long zoneId) throws SQLException 
{
    long startIPLong = NetUtils.ip2Long(startIp);
    long endIPLong = NetUtils.ip2Long(endIp);
    String insertSql = "INSERT INTO `cloud`.`op_dc_storage_network_ip_address` (range_id, ip_address, mac_address, taken) VALUES (?, ?, (select mac_address from `cloud`.`data_center` where id=?), ?)";
    String updateSql = "UPDATE `cloud`.`data_center` set mac_address = mac_address+1 where id=?";
    try (Connection conn = txn.getConnection()) {
        while (startIPLong <= endIPLong) {
            try (PreparedStatement stmt_insert = conn.prepareStatement(insertSql)) {
                stmt_insert.setLong(1, rangeId);
                stmt_insert.setString(2, NetUtils.long2Ip(startIPLong++));
                stmt_insert.setLong(3, zoneId);
                stmt_insert.setNull(4, java.sql.Types.DATE);
                stmt_insert.executeUpdate();
            }
            try (PreparedStatement stmt_update = txn.prepareStatement(updateSql)) {
                stmt_update.setLong(1, zoneId);
                stmt_update.executeUpdate();
            }
        }
    }
}