    public List<Pair<Pair<String, VirtualMachine.Type>, Pair<Long, String>>> getVmsDetailByNames(Set<String> vmNames, String detail) {
        TransactionLegacy txn = TransactionLegacy.currentTxn();
        List<Pair<Pair<String, VirtualMachine.Type>, Pair<Long, String>>> vmsDetailByNames = new ArrayList<Pair<Pair<String, VirtualMachine.Type>, Pair<Long, String>>>();

        PreparedStatement pstmt = null;
        try {
            pstmt = txn.prepareStatement(VMS_DETAIL_BY_NAME + getQueryBatchAppender(vmNames.size()));
            pstmt.setString(1, detail);
            int i = 2;
            for(String name : vmNames) {
                pstmt.setString(i, name);
                i++;
            }
            try {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    vmsDetailByNames.add(new Pair<Pair<String, VirtualMachine.Type>, Pair<Long, String>>(new Pair<String, VirtualMachine.Type>(
                            rs.getString("vm_instance.instance_name"), VirtualMachine.Type.valueOf(rs.getString("vm_type"))),
                            new Pair<Long, String>(rs.getLong("vm_instance.id"), rs.getString("user_vm_details.value"))));
                }
                rs.close();
            } catch (Exception e) {
                s_logger.error("GetVmsDetailsByNames: Exception: " + e.getMessage());
                throw new CloudRuntimeException("GetVmsDetailsByNames: Exception: " + e.getMessage());
            }
            if(pstmt != null) {
                pstmt.close();
            }
        } catch (Exception e) {
            s_logger.error("GetVmsDetailsByNames: Exception in sql: " + e.getMessage());
            throw new CloudRuntimeException("GetVmsDetailsByNames: Exception: " + e.getMessage());
        }

        return vmsDetailByNames;
    }
