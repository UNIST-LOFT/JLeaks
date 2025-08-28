
        DistributorSelectionLogic(Parameters params, SlobrokPolicy policy) {
            try {
                hostFetcher = params.createHostFetcher(policy, params.getRequiredUpPercentageToSendToKnownGoodNodes());
                distribution = params.createDistribution(policy);
                persistentFailureChecker = new InstabilityChecker(params.getAttemptRandomOnFailuresLimit());
                maxOldClusterVersionBeforeSendingRandom = params.maxOldClusterStatesSeenBeforeThrowingCachedState();
            } catch (Throwable e) {
                destroy();
                throw e;
            }
        }