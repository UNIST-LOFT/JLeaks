  private static List<Token<?>> addDelegationTokens(Configuration config,
                                                    LocationFactory locationFactory,
                                                    Credentials credentials) throws IOException {
    if (!UserGroupInformation.isSecurityEnabled()) {
      LOG.debug("Security is not enabled");
      return ImmutableList.of();
    }

    FileSystem fileSystem = getFileSystem(locationFactory, config);

    if (fileSystem == null) {
      LOG.warn("Unexpected: LocationFactory is not HDFS. Not getting delegation tokens.");
      return ImmutableList.of();
    }

    String renewer = YarnUtils.getYarnTokenRenewer(config);

    Token<?>[] tokens = fileSystem.addDelegationTokens(renewer, credentials);
    LOG.debug("Added HDFS DelegationTokens: {}", Arrays.toString(tokens));

    return tokens == null ? ImmutableList.<Token<?>>of() : ImmutableList.copyOf(tokens);
  }
