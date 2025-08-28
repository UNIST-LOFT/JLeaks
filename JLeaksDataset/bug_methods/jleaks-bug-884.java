    public static void batchDeleteUndoLog(Set<String> xids, Set<Long> branchIds, int limitSize, Connection conn) throws SQLException {
        if (CollectionUtils.isEmpty(xids) || CollectionUtils.isEmpty(branchIds)) {
            return;
        }
        int xidSize = xids.size();
        int branchIdSize = branchIds.size();
        String batchDeleteSql = toBatchDeleteUndoLogSql(xidSize, branchIdSize,limitSize);
        PreparedStatement deletePST = conn.prepareStatement(batchDeleteSql);
        int paramsIndex = 1;
        for (Long branchId : branchIds) {
            deletePST.setLong(paramsIndex++,branchId);
        }
        for (String xid: xids){
            deletePST.setString(paramsIndex++, xid);
        }
        int deleteRows = deletePST.executeUpdate();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("batch delete undo log size " + deleteRows);
        }
    }
