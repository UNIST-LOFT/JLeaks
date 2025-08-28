protected Mono<AmqpReceiveLink> createConsumer(String linkName, String entityPath, Duration timeout,
        AmqpRetryPolicy retry, Map<Symbol, Object> sourceFilters, Map<Symbol, Object> receiverProperties,
        Symbol[] receiverDesiredCapabilities, SenderSettleMode senderSettleMode,
        ReceiverSettleMode receiverSettleMode) {
        if (isDisposed()) {
            return Mono.error(logger.logExceptionAsError(new AmqpException(true, String.format(
                "connectionId[%s] sessionName[%s] entityPath[%s] linkName[%s] Cannot create receive link from a closed"
                + " session.", sessionHandler.getConnectionId(), sessionName, entityPath, linkName),
                sessionHandler.getErrorContext())));
        }
        final LinkSubscription<AmqpReceiveLink> existingLink = openReceiveLinks.get(linkName);
        if (existingLink != null) {
            logger.info("linkName[{}] entityPath[{}] Returning existing receive link.", linkName, entityPath);
            return Mono.just(existingLink.getLink());
        }

        final TokenManager tokenManager = tokenManagerProvider.getTokenManager(cbsNodeSupplier, entityPath);
        return Mono.when(onActiveEndpoint(), tokenManager.authorize())
            .then(Mono.create((Consumer<MonoSink<AmqpReceiveLink>>) sink -> {
                try {
                    // This has to be executed using reactor dispatcher because it's possible to run into race
                    // conditions with proton-j.
                    provider.getReactorDispatcher().invoke(() -> {
                        final LinkSubscription<AmqpReceiveLink> computed = openReceiveLinks.compute(linkName,
                            (linkNameKey, existing) -> {
                                if (existing != null) {
                                    logger.info("linkName[{}]: Another receive link exists. Disposing of new one.",
                                        linkName);
                                    tokenManager.close();

                                    return existing;
                                }

                                logger.info("connectionId[{}] sessionId[{}] linkName[{}] Creating a new receiver link.",
                                    sessionHandler.getConnectionId(), sessionName, linkName);
                                return getSubscription(linkNameKey, entityPath, sourceFilters, receiverProperties,
                                    receiverDesiredCapabilities, senderSettleMode, receiverSettleMode, tokenManager);
                            });

                        sink.success(computed.getLink());
                    });
                } catch (IOException | RejectedExecutionException e) {
                    sink.error(e);
                }
            }))
            .onErrorResume(t -> Mono.defer(() -> {
                tokenManager.close();
                return Mono.error(t);
            }));
    }