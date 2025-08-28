protected void importFromLDAPByUser(LDAPImportContext ldapImportContext){
    byte[] cookie = new byte[0];
    SafeLdapContext safeLdapContext = _safePortalLDAP.getSafeLdapContext(ldapImportContext.getLdapServerId(), ldapImportContext.getCompanyId());
    try {
        while (cookie != null) {
            List<SearchResult> searchResults = new ArrayList<>();
            Properties userMappings = ldapImportContext.getUserMappings();
            String userMappingsScreenName = GetterUtil.getString(userMappings.getProperty("screenName"));
            userMappingsScreenName = StringUtil.toLowerCase(userMappingsScreenName);
            cookie = _safePortalLDAP.getUsers(ldapImportContext.getLdapServerId(), ldapImportContext.getCompanyId(), safeLdapContext, cookie, 0, new String[] { userMappingsScreenName }, searchResults);
            for (SearchResult searchResult : searchResults) {
                String fullUserDN = searchResult.getNameInNamespace();
                if (ldapImportContext.containsImportedUser(fullUserDN)) {
                    continue;
                }
                try {
                    Attributes userAttributes = _safePortalLDAP.getUserAttributes(ldapImportContext.getLdapServerId(), ldapImportContext.getCompanyId(), ldapImportContext.getSafeLdapContext(), SafeLdapNameFactory.from(searchResult));
                    User user = importUser(ldapImportContext, fullUserDN, userAttributes, null);
                    importGroups(ldapImportContext, userAttributes, user);
                } catch (GroupFriendlyURLException groupFriendlyURLException) {
                    int type = groupFriendlyURLException.getType();
                    if (type == GroupFriendlyURLException.DUPLICATE) {
                        _log.error(StringBundler.concat("Unable to import user ", fullUserDN, " because of a duplicate group friendly ", "URL"), groupFriendlyURLException);
                    } else {
                        _log.error("Unable to import user " + fullUserDN, groupFriendlyURLException);
                    }
                } catch (Exception exception) {
                    _log.error("Unable to import user " + fullUserDN, exception);
                }
            }
        }
    } finally {
        if (safeLdapContext != null) {
            safeLdapContext.close();
        }
    }
}