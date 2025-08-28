    public List<PlayerAuth> getLoggedPlayers() {
        List<PlayerAuth> auths = new ArrayList<>();
        try (Connection con = getConnection()) {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + tableName + " WHERE " + col.IS_LOGGED + "=1;");
            PreparedStatement pst = con.prepareStatement("SELECT data FROM xf_user_authenticate WHERE " + col.ID + "=?;");
            while (rs.next()) {
                PlayerAuth pAuth = buildAuthFromResultSet(rs);
                if (hashAlgorithm == HashAlgorithm.XFBCRYPT) {
                    int id = rs.getInt(col.ID);
                    pst.setInt(1, id);
                    ResultSet rs2 = pst.executeQuery();
                    if (rs2.next()) {
                        Blob blob = rs2.getBlob("data");
                        byte[] bytes = blob.getBytes(1, (int) blob.length());
                        pAuth.setPassword(new HashedPassword(XFBCRYPT.getHashFromBlob(bytes)));
                    }
                    rs2.close();
                }
                auths.add(pAuth);
            }
        } catch (SQLException ex) {
            logSqlException(ex);
        }
        return auths;
    }
