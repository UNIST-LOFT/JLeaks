    public LdapCtx(String dn, String host, int port_number, Hashtable props,
            boolean useSsl) throws NamingException {

        this.useSsl = this.hasLdapsScheme = useSsl;

        if (props != null) {
            envprops = (Hashtable) props.clone();

            // SSL env prop overrides the useSsl argument
            if ("ssl".equals(envprops.get(Context.SECURITY_PROTOCOL))) {
                this.useSsl = true;
            }

            // %%% These are only examined when the context is created
            // %%% because they are only for debugging or workaround purposes.
            trace = (OutputStream)envprops.get(TRACE_BER);

            if (props.get(NETSCAPE_SCHEMA_BUG) != null ||
                props.get(OLD_NETSCAPE_SCHEMA_BUG) != null) {
                netscapeSchemaBug = true;
            }
        }

        currentDN = (dn != null) ? dn : "";
        currentParsedDN = parser.parse(currentDN);

        hostname = (host != null && host.length() > 0) ? host : DEFAULT_HOST;
        if (hostname.charAt(0) == '[') {
            hostname = hostname.substring(1, hostname.length() - 1);
        }

        if (port_number > 0) {
            this.port_number = port_number;
        } else {
            this.port_number = this.useSsl ? DEFAULT_SSL_PORT : DEFAULT_PORT;
            this.useDefaultPortNumber = true;
        }

        schemaTrees = new Hashtable(11, 0.75f);
        initEnv();
        connect(false);
    }
