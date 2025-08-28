    public LdapNetworkConnection bindAs(Dn userDN, String password)
            throws GuacamoleException {

        // Obtain appropriately-configured LdapNetworkConnection instance
        LdapNetworkConnection ldapConnection = createLDAPConnection();

        try {

            // Connect to LDAP server
            ldapConnection.connect();

            // Explicitly start TLS if requested
            if (confService.getEncryptionMethod() == EncryptionMethod.STARTTLS)
                ldapConnection.startTls();

        }
        catch (LdapException e) {
            throw new GuacamoleServerException("Error connecting to LDAP server.", e);
        }

        // Bind using provided credentials
        try {

            BindRequest bindRequest = new BindRequestImpl();
            bindRequest.setDn(userDN);
            bindRequest.setCredentials(password);
            BindResponse bindResponse = ldapConnection.bind(bindRequest);
            if (bindResponse.getLdapResult().getResultCode() == ResultCodeEnum.SUCCESS)
                return ldapConnection;
            
            else
                throw new GuacamoleInvalidCredentialsException("Error binding"
                        + " to server: " + bindResponse.toString(),
                        CredentialsInfo.USERNAME_PASSWORD);

        }

        // Disconnect if an error occurs during bind
        catch (LdapException e) {
            logger.debug("Unable to bind to LDAP server.", e);
            disconnect(ldapConnection);
            throw new GuacamoleInvalidCredentialsException(
                    "Unable to bind to the LDAP server.",
                    CredentialsInfo.USERNAME_PASSWORD);
        }

    }
