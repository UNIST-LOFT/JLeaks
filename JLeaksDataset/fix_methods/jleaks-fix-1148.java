public LdapNetworkConnection bindAs(Dn userDN, String password)
            throws GuacamoleException {

        // Get ldapConnection and try to connect and bind.
        try (LdapNetworkConnection ldapConnection = createLDAPConnection()) {

            // Connect to LDAP server
            ldapConnection.connect();
            // Explicitly start TLS if requested
            if (confService.getEncryptionMethod() == EncryptionMethod.STARTTLS)
                ldapConnection.startTls();

            // Bind using provided credentials
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
            throw new GuacamoleInvalidCredentialsException(
                    "Unable to bind to the LDAP server.",
                    CredentialsInfo.USERNAME_PASSWORD);
        }
    }