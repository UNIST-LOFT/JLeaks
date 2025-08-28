    public synchronized RecordAlert write(int scanId, int pluginId, String alert, 
            int risk, int reliability, String description, String uri, String param, String attack, 
            String otherInfo, String solution, String reference, String evidence, int cweId, int wascId, int historyId,
            int sourceHistoryId) throws SQLException {
        
        psInsert.setInt(1, scanId);
        psInsert.setInt(2, pluginId);
        psInsert.setString(3, alert);
        psInsert.setInt(4, risk);
        psInsert.setInt(5, reliability);
        psInsert.setString(6, description);
        psInsert.setString(7, uri);
        psInsert.setString(8, param);
        psInsert.setString(9, attack);
        psInsert.setString(10, otherInfo);
        psInsert.setString(11, solution);
        psInsert.setString(12, reference);
        psInsert.setString(13, evidence);
        psInsert.setInt(14, cweId);
        psInsert.setInt(15, wascId);
        psInsert.setInt(16, historyId);
        psInsert.setInt(17, sourceHistoryId);
        psInsert.executeUpdate();
        
        ResultSet rs = psGetIdLastInsert.executeQuery();
        rs.next();
        int id = rs.getInt(1);
        rs.close();
        return read(id);
    }
