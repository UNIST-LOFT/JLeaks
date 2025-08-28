private void generateClasses() 
{
    try (Connection connection = getConnection()) {
        // query for credential
        String dbCredential = null;
        try (PreparedStatement statement = connection.prepareStatement(userQuery)) {
            statement.setString(1, userName);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    dbCredential = results.getString(1);
                }
            }
        }
        if (dbCredential == null) {
            return null;
        }
        // query for role names
        List<String> roles = new ArrayList<String>();
        try (PreparedStatement statement = connection.prepareStatement(rolesQuery)) {
            statement.setString(1, userName);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    String roleName = results.getString(1);
                    roles.add(roleName);
                }
            }
        }
        return new UserInfo(userName, Credential.getCredential(dbCredential), roles);
    }
}