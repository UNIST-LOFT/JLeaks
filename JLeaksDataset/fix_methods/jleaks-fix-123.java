protected HRegion openHRegion(final CancelableProgressable reporter){
    try {
        // Refuse to open the region if we are missing local compression support
        checkCompressionCodecs();
        // Refuse to open the region if encryption configuration is incorrect or
        // codec support is missing
        LOG.debug("checking encryption for " + this.getRegionInfo().getEncodedName());
        checkEncryption();
        // Refuse to open the region if a required class cannot be loaded
        LOG.debug("checking classloading for " + this.getRegionInfo().getEncodedName());
        checkClassLoading();
        this.openSeqNum = initialize(reporter);
        this.mvcc.advanceTo(openSeqNum);
        // The openSeqNum must be increased every time when a region is assigned, as we rely on it to
        // determine whether a region has been successfully reopened. So here we always write open
        // marker, even if the table is read only.
        if (wal != null && getRegionServerServices() != null && RegionReplicaUtil.isDefaultReplica(getRegionInfo())) {
            writeRegionOpenMarker(wal, openSeqNum);
        }
    } catch (Throwable t) {
        // By coprocessor path wrong region will open failed,
        // MetricsRegionWrapperImpl is already init and not close,
        // add region close when open failed
        this.close();
        throw t;
    }
    return this;
}