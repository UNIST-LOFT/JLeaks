    RxDocumentClientImpl(URI serviceEndpoint,
                         String masterKeyOrResourceToken,
                         ConnectionPolicy connectionPolicy,
                         ConsistencyLevel consistencyLevel,
                         Configs configs,
                         AzureKeyCredential credential,
                         boolean sessionCapturingOverrideEnabled,
                         boolean connectionSharingAcrossClientsEnabled,
                         boolean contentResponseOnWriteEnabled) {

        logger.info(
            "Initializing DocumentClient with"
                + " serviceEndpoint [{}], connectionPolicy [{}], consistencyLevel [{}], directModeProtocol [{}]",
            serviceEndpoint, connectionPolicy, consistencyLevel, configs.getProtocol());

        this.connectionSharingAcrossClientsEnabled = connectionSharingAcrossClientsEnabled;
        this.configs = configs;
        this.masterKeyOrResourceToken = masterKeyOrResourceToken;
        this.serviceEndpoint = serviceEndpoint;
        this.credential = credential;
        this.contentResponseOnWriteEnabled = contentResponseOnWriteEnabled;

        if (this.credential != null) {
            hasAuthKeyResourceToken = false;
            this.authorizationTokenProvider = new BaseAuthorizationTokenProvider(this.credential);
        } else if (masterKeyOrResourceToken != null && ResourceTokenAuthorizationHelper.isResourceToken(masterKeyOrResourceToken)) {
            this.authorizationTokenProvider = null;
            hasAuthKeyResourceToken = true;
        } else if(masterKeyOrResourceToken != null && !ResourceTokenAuthorizationHelper.isResourceToken(masterKeyOrResourceToken)){
            this.credential = new AzureKeyCredential(this.masterKeyOrResourceToken);
            hasAuthKeyResourceToken = false;
            this.authorizationTokenProvider = new BaseAuthorizationTokenProvider(this.credential);
        } else {
            hasAuthKeyResourceToken = false;
            this.authorizationTokenProvider = null;
        }

        if (connectionPolicy != null) {
            this.connectionPolicy = connectionPolicy;
        } else {
            this.connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        }

        boolean disableSessionCapturing = (ConsistencyLevel.SESSION != consistencyLevel && !sessionCapturingOverrideEnabled);

        this.sessionContainer = new SessionContainer(this.serviceEndpoint.getHost(), disableSessionCapturing);
        this.consistencyLevel = consistencyLevel;

        this.userAgentContainer = new UserAgentContainer();

        String userAgentSuffix = this.connectionPolicy.getUserAgentSuffix();
        if (userAgentSuffix != null && userAgentSuffix.length() > 0) {
            userAgentContainer.setSuffix(userAgentSuffix);
        }

        this.reactorHttpClient = httpClient();
        this.globalEndpointManager = new GlobalEndpointManager(asDatabaseAccountManagerInternal(), this.connectionPolicy, /**/configs);
        this.retryPolicy = new RetryPolicy(this.globalEndpointManager, this.connectionPolicy);
        this.resetSessionTokenRetryPolicy = retryPolicy;
    }
