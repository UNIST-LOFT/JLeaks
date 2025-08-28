        DistributorSelectionLogic(Parameters params, SlobrokPolicy policy) {
            this.hostFetcher = params.createHostFetcher(policy, params.getRequiredUpPercentageToSendToKnownGoodNodes());
            this.distribution = params.createDistribution(policy);
            persistentFailureChecker = new InstabilityChecker(params.getAttemptRandomOnFailuresLimit());
            maxOldClusterVersionBeforeSendingRandom = params.maxOldClusterStatesSeenBeforeThrowingCachedState();
        }
