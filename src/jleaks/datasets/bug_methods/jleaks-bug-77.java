    private void encryptLdapConfigParams(Connection conn) {
        PreparedStatement pstmt = null;

        String[][] ldapParams = { {"ldap.user.object", "inetOrgPerson", "Sets the object type of users within LDAP"},
                {"ldap.username.attribute", "uid", "Sets the username attribute used within LDAP"}, {"ldap.email.attribute", "mail", "Sets the email attribute used within LDAP"},
                {"ldap.firstname.attribute", "givenname", "Sets the firstname attribute used within LDAP"},
                {"ldap.lastname.attribute", "sn", "Sets the lastname attribute used within LDAP"},
                {"ldap.group.object", "groupOfUniqueNames", "Sets the object type of groups within LDAP"},
                {"ldap.group.user.uniquemember", "uniquemember", "Sets the attribute for uniquemembers within a group"}};

        String insertSql = "INSERT INTO `cloud`.`configuration`(category, instance, component, name, value, description) VALUES ('Secure', 'DEFAULT', 'management-server', ?, ?, "
                + "?) ON DUPLICATE KEY UPDATE category='Secure';";

        try {

            for (String[] ldapParam : ldapParams) {
                String name = ldapParam[0];
                String value = ldapParam[1];
                String desc = ldapParam[2];
                String encryptedValue = DBEncryptionUtil.encrypt(value);
                pstmt = conn.prepareStatement(insertSql);
                pstmt.setString(1, name);
                pstmt.setBytes(2, encryptedValue.getBytes("UTF-8"));
                pstmt.setString(3, desc);
                pstmt.executeUpdate();
            }

            /**
             * if encrypted, decrypt the ldap hostname and port and then update as they are not encrypted now.
             */
            pstmt = conn.prepareStatement("SELECT conf.value FROM `cloud`.`configuration` conf WHERE conf.name='ldap.hostname'");
            ResultSet resultSet = pstmt.executeQuery();
            String hostname = null;
            String port;
            int portNumber = 0;
            if (resultSet.next()) {
                hostname = DBEncryptionUtil.decrypt(resultSet.getString(1));
            }

            pstmt = conn.prepareStatement("SELECT conf.value FROM `cloud`.`configuration` conf WHERE conf.name='ldap.port'");
            resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                port = DBEncryptionUtil.decrypt(resultSet.getString(1));
                if (StringUtils.isNotBlank(port)) {
                    portNumber = Integer.valueOf(port);
                }
            }

            if (StringUtils.isNotBlank(hostname)) {
                pstmt = conn.prepareStatement("INSERT INTO `cloud`.`ldap_configuration`(hostname, port) VALUES(?,?)");
                pstmt.setString(1, hostname);
                if (portNumber != 0) {
                    pstmt.setInt(2, portNumber);
                } else {
                    pstmt.setNull(2, Types.INTEGER);
                }
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new CloudRuntimeException("Unable to insert ldap configuration values ", e);
        } catch (UnsupportedEncodingException e) {
            throw new CloudRuntimeException("Unable to insert ldap configuration values ", e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {
            }
        }
        s_logger.debug("Done encrypting ldap Config values");

    }
