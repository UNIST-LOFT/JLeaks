    private static void upgradeResourceCountforAccount(Connection conn, Long accountId, Long domainId, String type, Long resourceCount) throws SQLException {
        //update or insert into resource_count table.
        PreparedStatement pstmt = null;
        pstmt =
                conn.prepareStatement("INSERT INTO `cloud`.`resource_count` (account_id, type, count) VALUES (?,?,?) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), count=?");
        pstmt.setLong(1, accountId);
        pstmt.setString(2, type);
        pstmt.setLong(3, resourceCount);
        pstmt.setLong(4, resourceCount);
        pstmt.executeUpdate();
        pstmt.close();
    }
