private void updateOverprovisioningPerVm(Connection conn) 
{
    PreparedStatement pstmt1 = null;
    PreparedStatement pstmt2 = null;
    PreparedStatement pstmt3 = null;
    ResultSet result1 = null;
    ResultSet result2 = null;
    // Get cpu overprovisioning factor from global setting and update user vm details table for all the vms if factor > 1
    try {
        pstmt1 = conn.prepareStatement("select value from `cloud`.`configuration` where name='cpu.overprovisioning.factor'");
        result1 = pstmt1.executeQuery();
        String cpuoverprov = "1";
        if (result1.next()) {
            cpuoverprov = result1.getString(1);
        }
        result1.close();
        pstmt1.close();
        pstmt1 = conn.prepareStatement("select value from `cloud`.`configuration` where name='mem.overprovisioning.factor'");
        result1 = pstmt1.executeQuery();
        String memoverprov = "1";
        if (result1.next()) {
            memoverprov = result1.getString(1);
        }
        result1.close();
        pstmt1.close();
        // Need to populate only when overprovisioning factor doesn't pre exist.
        s_logger.debug("Starting updating user_vm_details with cpu/memory overprovisioning factors");
        pstmt2 = conn.prepareStatement("select id, hypervisor_type from `cloud`.`vm_instance` where removed is null and id not in (select vm_id from  `cloud`.`user_vm_details` where name='cpuOvercommitRatio')");
        pstmt3 = conn.prepareStatement("INSERT IGNORE INTO cloud.user_vm_details (vm_id, name, value) VALUES (?, ?, ?)");
        result2 = pstmt2.executeQuery();
        while (result2.next()) {
            String hypervisor_type = result2.getString(2);
            if (hypervisor_type.equalsIgnoreCase(Hypervisor.HypervisorType.VMware.name())) {
                // For cpu
                pstmt3.setLong(1, result2.getLong(1));
                pstmt3.setString(2, "cpuOvercommitRatio");
                pstmt3.setString(3, cpuoverprov);
                pstmt3.executeUpdate();
                // For memory
                pstmt3.setLong(1, result2.getLong(1));
                pstmt3.setString(2, "memoryOvercommitRatio");
                // memory overprovisioning was used to reserve memory in case of VMware.
                pstmt3.setString(3, memoverprov);
                pstmt3.executeUpdate();
            } else {
                // For cpu
                pstmt3.setLong(1, result2.getLong(1));
                pstmt3.setString(2, "cpuOvercommitRatio");
                pstmt3.setString(3, cpuoverprov);
                pstmt3.executeUpdate();
                // For memory
                pstmt3.setLong(1, result2.getLong(1));
                pstmt3.setString(2, "memoryOvercommitRatio");
                // memory overprovisioning didn't exist earlier.
                pstmt3.setString(3, "1");
                pstmt3.executeUpdate();
            }
        }
        s_logger.debug("Done updating user_vm_details with cpu/memory overprovisioning factors");
    } catch (SQLException e) {
        throw new CloudRuntimeException("Unable to update cpu/memory overprovisioning factors", e);
    } finally {
        try {
            if (pstmt1 != null && !pstmt1.isClosed()) {
                pstmt1.close();
            }
        } catch (SQLException e) {
        }
        try {
            if (pstmt2 != null && !pstmt2.isClosed()) {
                pstmt2.close();
            }
        } catch (SQLException e) {
        }
        try {
            if (pstmt3 != null && !pstmt3.isClosed()) {
                pstmt3.close();
            }
        } catch (SQLException e) {
        }
        try {
            if (result1 != null) {
                result1.close();
            }
        } catch (SQLException e) {
        }
        try {
            if (result2 != null) {
                result2.close();
            }
        } catch (SQLException e) {
        }
    }
}