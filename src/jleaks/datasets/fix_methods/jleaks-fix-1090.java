
    protected void reconnect(Connection conn) throws SQLException {
        psRead = conn.prepareStatement("SELECT TOP 1 * FROM HISTORY WHERE " + HISTORYID + " = ?");
        // updatable recordset does not work in hsqldb jdbc impelementation!
        //psWrite = mConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        psDelete = conn.prepareStatement("DELETE FROM HISTORY WHERE " + HISTORYID + " = ?");
        psDeleteTemp = conn.prepareStatement("DELETE FROM HISTORY WHERE " + HISTTYPE + " = " + HistoryReference.TYPE_TEMPORARY);
        psContainsURI = conn.prepareStatement("SELECT TOP 1 HISTORYID FROM HISTORY WHERE URI = ? AND  METHOD = ? AND REQBODY = ? AND SESSIONID = ? AND HISTTYPE = ?");

        isExistStatusCode = false;
        ResultSet rs = conn.getMetaData().getColumns(null, null, "HISTORY", "STATUSCODE");
        if (rs.next()) {
            isExistStatusCode = true;
        }
        rs.close();
        // ZAP: Added support for the tag when creating a history record
        if (isExistStatusCode) {
            psWrite1= conn.prepareStatement("INSERT INTO HISTORY ("
                    + SESSIONID + "," + HISTTYPE + "," + TIMESENTMILLIS + "," + 
                    TIMEELAPSEDMILLIS + "," + METHOD + "," + URI + "," + REQHEADER + "," + 
                    REQBODY + "," + RESHEADER + "," + RESBODY + "," + TAG + ", " + STATUSCODE
                    + ") VALUES (?, ? ,?, ?, ?, ?, ?, ? ,? , ?, ?, ?)");
        } else {
            psWrite1= conn.prepareStatement("INSERT INTO HISTORY ("
                    + SESSIONID + "," + HISTTYPE + "," + TIMESENTMILLIS + "," + 
                    TIMEELAPSEDMILLIS + "," + METHOD + "," + URI + "," + REQHEADER + "," + 
                    REQBODY + "," + RESHEADER + "," + RESBODY + "," + TAG
                    + ") VALUES (?, ? ,?, ?, ?, ?, ?, ? ,? , ? , ?)");
            
        }
        psWrite2 = conn.prepareCall("CALL IDENTITY();");

        rs = conn.getMetaData().getColumns(null, null, "HISTORY", "TAG");
        if (!rs.next()) {
            PreparedStatement stmt = conn.prepareStatement("ALTER TABLE HISTORY ADD COLUMN TAG VARCHAR DEFAULT ''");
            stmt.execute();
            stmt.close();
        }
        rs.close();
        
        psUpdateTag = conn.prepareStatement("UPDATE HISTORY SET TAG = ? WHERE HISTORYID = ?");

        // ZAP: Add the NOTE column to the db if necessary
        rs = conn.getMetaData().getColumns(null, null, "HISTORY", "NOTE");
        if (!rs.next()) {
            PreparedStatement stmt = conn.prepareStatement("ALTER TABLE HISTORY ADD COLUMN NOTE VARCHAR DEFAULT ''");
            stmt.execute();
            stmt.close();
        }
        rs.close();

       	psUpdateNote = conn.prepareStatement("UPDATE HISTORY SET NOTE = ? WHERE HISTORYID = ?");
       	psLastIndex = conn.prepareStatement("SELECT TOP 1 HISTORYID FROM HISTORY ORDER BY HISTORYID DESC");
    }
    
    