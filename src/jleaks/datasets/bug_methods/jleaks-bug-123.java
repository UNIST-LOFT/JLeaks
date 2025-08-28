  protected HRegion openHRegion(final CancelableProgressable reporter)
  throws IOException {
    // Refuse to open the region if we are missing local compression support
    checkCompressionCodecs();
    LOG.debug("checking encryption for " + this.getRegionInfo().getEncodedName());
    // Refuse to open the region if encryption configuration is incorrect or
    // codec support is missing
    checkEncryption();
    // Refuse to open the region if a required class cannot be loaded
    LOG.debug("checking classloading for " + this.getRegionInfo().getEncodedName());
    checkClassLoading();
    this.openSeqNum = initialize(reporter);
    this.mvcc.advanceTo(openSeqNum);
    // The openSeqNum must be increased every time when a region is assigned, as we rely on it to
    // determine whether a region has been successfully reopened. So here we always write open
    // marker, even if the table is read only.
    if (wal != null && getRegionServerServices() != null &&
      RegionReplicaUtil.isDefaultReplica(getRegionInfo())) {
      writeRegionOpenMarker(wal, openSeqNum);
    }
    return this;
  }
