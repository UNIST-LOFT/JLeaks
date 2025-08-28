private static void upgradeResourceCountforDomain(Connection conn, Long domainId, String type, Long resourceCount) throws SQLException 
{
    // update or insert into resource_count table.
    String sqlInsertResourceCount = "INSERT INTO `cloud`.`resource_count` (domain_id, type, count) VALUES (?,?,?) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), count=?";
    try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertResourceCount)) {
        pstmt.setLong(1, domainId);
        pstmt.setString(2, type);
        pstmt.setLong(3, resourceCount);
        pstmt.setLong(4, resourceCount);
        pstmt.executeUpdate();
    }
}