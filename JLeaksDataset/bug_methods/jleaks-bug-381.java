    public UserInfo getUserInfo (String userName)
        throws Exception
    {
        Connection connection = null;

        try
        {
            connection = getConnection();

            //query for credential
            PreparedStatement statement = connection.prepareStatement (userQuery);
            statement.setString (1, userName);
            ResultSet results = statement.executeQuery();
            String dbCredential = null;
            if (results.next())
            {
                dbCredential = results.getString(1);
            }
            results.close();
            statement.close();

            //query for role names
            statement = connection.prepareStatement (rolesQuery);
            statement.setString (1, userName);
            results = statement.executeQuery();
            List<String> roles = new ArrayList<String>();

            while (results.next())
            {
                String roleName = results.getString (1);
                roles.add (roleName);
            }

            results.close();
            statement.close();

            return dbCredential==null ? null : new UserInfo (userName,
                    Credential.getCredential(dbCredential), roles);
        }
        finally
        {
            if (connection != null) connection.close();
        }
    }
