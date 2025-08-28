DiskStoreImpl createOwnedByRegion(String name, boolean isOwnedByPR,
InternalRegionArguments internalRegionArgs) {
    this.attrs.name = name;
    synchronized (this.cache) {
        DiskStoreImpl ds = new DiskStoreImpl(this.cache, this.attrs, true, /* ownedByRegion */
        internalRegionArgs);
        if (isOwnedByPR) {
            initializeDiskStore(ds);
        }
        this.cache.addRegionOwnedDiskStore(ds);
        return ds;
    }
}