    private NamingEnumeration<SearchResult> searchForGroups(String searchFilter,
            String[] returningAttributes) {
        if (manager.isDebugEnabled()) {
            Log.debug("Trying to find all groups in the system.");
        }
        DirContext ctx;
        NamingEnumeration<SearchResult> answer = null;
        try {
            ctx = manager.getContext();
            if (manager.isDebugEnabled()) {
                Log.debug("Starting LDAP search...");
                Log.debug("Using groupSearchFilter: " + searchFilter);
            }

            // Search for the dn based on the groupname.
            SearchControls searchControls = new SearchControls();
            searchControls.setReturningAttributes(returningAttributes);
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            answer = ctx.search("", searchFilter, searchControls);

            if (manager.isDebugEnabled()) {
                Log.debug("... search finished");
            }
        }
        catch (Exception e) {
            if (manager.isDebugEnabled()) {
                Log.debug("Error while searching for groups.", e);
            }
        }
        return answer;
    }
