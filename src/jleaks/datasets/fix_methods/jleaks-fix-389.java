
    public void releaseStore(Store<?, ?> resource) {
      SoftLockValueCombinedSerializerLifecycleHelper helper = createdStores.remove(resource);
      if (helper == null) {
        throw new IllegalArgumentException("Given store is not managed by this provider : " + resource);
      }
      if (resource instanceof XAStore) {
        XAStore<?, ?> xaStore = (XAStore<?, ?>) resource;

        xaStore.transactionManagerWrapper.unregisterXAResource(xaStore.uniqueXAResourceId, xaStore.recoveryXaResource);
        // release the underlying store first, as it may still need the serializer to flush down to lower tiers
        underlyingStoreProvider.releaseStore(xaStore.underlyingStore);
        try {
          Serializer<?> serializer = helper.softLockSerializerRef.getAndSet(null);
          if(serializer instanceof CompactJavaSerializer) {
            ((CompactJavaSerializer)serializer).close();
          } else if(serializer instanceof CompactPersistentJavaSerializer) {
            ((CompactPersistentJavaSerializer)serializer).close();
          }
          xaStore.journal.close();
        } catch (IOException ioe) {
          throw new RuntimeException(ioe);
        }
      } else {
        underlyingStoreProvider.releaseStore(resource);
      }
    }