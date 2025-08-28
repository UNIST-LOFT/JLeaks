    private Message handleFetchFailure(VAdminProto.HandleFetchFailureRequest handleFetchFailure) {
        String storeName = handleFetchFailure.getStoreName();
        long pushVersion = handleFetchFailure.getPushVersion();
        String extraInfo = handleFetchFailure.getInfo();
        Properties extraInfoProperties = new Properties();
        try {
            extraInfoProperties.load(new StringReader(extraInfo));
        } catch (IOException e) {
            logger.error("Got IOException while trying to decipher a HandleFetchFailureRequest's info.", e);
        }

        logger.info("Received HandleFetchFailureRequest:\n" +
                    "\tstore_name: " + storeName + "\n" +
                    "\tpush_version: " + pushVersion + "\n" +
                    "\tinfo: " + extraInfoProperties.toString());

        VAdminProto.HandleFetchFailureResponse.Builder response =
                VAdminProto.HandleFetchFailureResponse.newBuilder();

        AdminClient adminClient = AdminClient.createTempAdminClient(voldemortConfig,
                                                                    metadataStore.getCluster(),
                                                                    1);
        // Get replica.factor for current store
        StoreDefinition storeDef = adminClient.metadataMgmtOps.getStoreDefinition(storeName);
        if (null == storeDef) {
            throw new StoreNotFoundException(storeName);
        }
        int replicaFactor = storeDef.getReplicationFactor();

        int maxNodeFailure = voldemortConfig.getHighAvailabilityPushMaxNodeFailures();
        // Considering replicaFactor could be smaller than maxNodeFailure configured in cluster level,
        // we need to compare the node failure number with the smaller number of (RF - 1, maxNodeFailure)
        // to make sure there is at least one replica running.
        maxNodeFailure = Math.min(maxNodeFailure, replicaFactor - 1);
        Set<Integer> nodesFailedInThisFetch = Sets.newHashSet(handleFetchFailure.getFailedNodesList());
        int failureCount = nodesFailedInThisFetch.size();
        boolean swapIsPossible = false;
        String responseMessage = "";
        if (failureCount > maxNodeFailure) {
            // Too many nodes failed to tolerate this strategy... let's bail out.
            responseMessage = "We cannot use pushHighAvailability because there is more than " + maxNodeFailure +
                    " nodes that failed their fetches and build.replica.factor is " + replicaFactor + "...";
            logger.error(responseMessage);
        } else {
            FailedFetchLock distributedLock = null;
            try {
                distributedLock = FailedFetchLock.getLock(voldemortConfig, new Props(extraInfoProperties));

                distributedLock.acquireLock();

                Set<Integer> alreadyDisabledNodes = distributedLock.getDisabledNodes();

                Set<Integer> allNodesToBeDisabled = Sets.newHashSet();
                allNodesToBeDisabled.addAll(alreadyDisabledNodes);
                allNodesToBeDisabled.addAll(nodesFailedInThisFetch);
                int disabledNodeSize = allNodesToBeDisabled.size();

                if (disabledNodeSize > maxNodeFailure) {
                    // Too many exceptions to tolerate this strategy... let's bail out.
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("We cannot use pushHighAvailability because it would bring the total ");
                    stringBuilder.append("number of nodes with disabled stores to more than ");
                    stringBuilder.append(maxNodeFailure);
                    stringBuilder.append("... alreadyDisabledNodes: [");
                    boolean firstItem = true;
                    for (Integer nodeId: alreadyDisabledNodes) {
                        if (firstItem) {
                            firstItem = false;
                        } else {
                            stringBuilder.append(", ");
                        }
                        stringBuilder.append(nodeId);
                    }
                    stringBuilder.append("], nodesFailedInThisFetch: [");
                    firstItem = true;
                    for (Integer nodeId: nodesFailedInThisFetch) {
                        if (firstItem) {
                            firstItem = false;
                        } else {
                            stringBuilder.append(", ");
                        }
                        stringBuilder.append(nodeId);
                    }
                    stringBuilder.append("]");
                    stringBuilder.append(", and build.replica.factor is ")
                        .append(replicaFactor);
                    responseMessage = stringBuilder.toString();
                    logger.error(responseMessage);
                } else {
                    String nodesString = "node";
                    if (nodesFailedInThisFetch.size() > 1) {
                        // Good grammar is important son
                        nodesString += "s";
                    }
                    nodesString += " [";
                    boolean firstNode = true;
                    for (Integer nodeId: nodesFailedInThisFetch) {
                        logger.warn("Will disable store '" + storeName + "' on node " + nodeId);
                        distributedLock.addDisabledNode(nodeId, storeName, pushVersion);
                        logger.warn("Store '" + storeName + "' is disabled on node " + nodeId);
                        if (firstNode) {
                            firstNode = false;
                        } else {
                            nodesString += ", ";
                        }
                        nodesString += nodeId;
                        response.addDisableStoreResponses(
                                adminClient.readonlyOps.disableStoreVersion(nodeId, storeName, pushVersion, extraInfo));
                    }
                    nodesString += "]";
                    swapIsPossible = true;
                    responseMessage = "Swap will be possible even though " + nodesString + " failed to fetch.";
                    logger.info(responseMessage);
                }
            } catch (ClassNotFoundException e) {
                String logMessage = "Failed to find requested FailedFetchLock implementation while setting up pushHighAvailability. ";
                logger.error(responseMessage, e);
                responseMessage = logMessage + "\n" + ExceptionUtils.stackTraceToString(e);
            } catch (Exception e) {
                String logMessage = "Got exception while trying to execute pushHighAvailability. ";
                logger.error(responseMessage, e);
                responseMessage = logMessage + "\n" + ExceptionUtils.stackTraceToString(e);
            } finally {
                if (distributedLock != null) {
                    try {
                        distributedLock.releaseLock();
                    } catch (Exception e) {
                        logger.error("Error while trying to release the shared lock used for pushHighAvailability!", e);
                    } finally {
                        try {
                            distributedLock.close();
                        } catch (Exception inception) {
                            logger.error("Error while trying to close the shared lock used for pushHighAvailability!",
                                         inception);
                        }
                    }
                }
            }
        }

        response.setSwapIsPossible(swapIsPossible);
        response.setInfo(responseMessage);

        return response.build();
    }
