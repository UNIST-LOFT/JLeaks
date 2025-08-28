public synchronized void sync() 
{
    final boolean enabled = settings.getBoolean(Keys.realm.ldap.synchronize, false);
    if (enabled) {
        logger.info("Synchronizing with LDAP @ " + settings.getRequiredString(Keys.realm.ldap.server));
        final boolean deleteRemovedLdapUsers = settings.getBoolean(Keys.realm.ldap.removeDeletedUsers, true);
        LdapConnection ldapConnection = new LdapConnection();
        if (ldapConnection.connect()) {
            if (ldapConnection.bind() == null) {
                ldapConnection.close();
                logger.error("Cannot synchronize with LDAP.");
                return;
            }
            try {
                String accountBase = settings.getString(Keys.realm.ldap.accountBase, "");
                String uidAttribute = settings.getString(Keys.realm.ldap.uid, "uid");
                String accountPattern = settings.getString(Keys.realm.ldap.accountPattern, "(&(objectClass=person)(sAMAccountName=${username}))");
                accountPattern = StringUtils.replace(accountPattern, "${username}", "*");
                SearchResult result = doSearch(ldapConnection, accountBase, accountPattern);
                if (result != null && result.getEntryCount() > 0) {
                    final Map<String, UserModel> ldapUsers = new HashMap<String, UserModel>();
                    for (SearchResultEntry loggingInUser : result.getSearchEntries()) {
                        Attribute uid = loggingInUser.getAttribute(uidAttribute);
                        if (uid == null) {
                            logger.error("Can not synchronize with LDAP, missing \"{}\" attribute", uidAttribute);
                            continue;
                        }
                        final String username = uid.getValue();
                        logger.debug("LDAP synchronizing: " + username);
                        UserModel user = userManager.getUserModel(username);
                        if (user == null) {
                            user = new UserModel(username);
                        }
                        if (!supportsTeamMembershipChanges()) {
                            getTeamsFromLdap(ldapConnection, username, loggingInUser, user);
                        }
                        // Get User Attributes
                        setUserAttributes(user, loggingInUser);
                        // store in map
                        ldapUsers.put(username.toLowerCase(), user);
                    }
                    if (deleteRemovedLdapUsers) {
                        logger.debug("detecting removed LDAP users...");
                        for (UserModel userModel : userManager.getAllUsers()) {
                            if (AccountType.LDAP == userModel.accountType) {
                                if (!ldapUsers.containsKey(userModel.username)) {
                                    logger.info("deleting removed LDAP user " + userModel.username + " from user service");
                                    userManager.deleteUser(userModel.username);
                                }
                            }
                        }
                    }
                    userManager.updateUserModels(ldapUsers.values());
                    if (!supportsTeamMembershipChanges()) {
                        final Map<String, TeamModel> userTeams = new HashMap<String, TeamModel>();
                        for (UserModel user : ldapUsers.values()) {
                            for (TeamModel userTeam : user.teams) {
                                userTeams.put(userTeam.name, userTeam);
                            }
                        }
                        userManager.updateTeamModels(userTeams.values());
                    }
                }
                if (!supportsTeamMembershipChanges()) {
                    getEmptyTeamsFromLdap(ldapConnection);
                }
            } finally {
                ldapConnection.close();
            }
        }
    }
}