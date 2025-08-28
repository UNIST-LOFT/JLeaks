  public JournalContext createJournalContext() throws UnavailableException {
    // Use the state change lock for the journal context, since all modifications to journaled
    // state must happen inside of a journal context.
    LockResource sharedLockResource;
    try {
      sharedLockResource = mMasterContext.getStateLockManager().lockShared();
    } catch (InterruptedException e) {
      throw new UnavailableException(
          "Failed to acquire state-lock due to ongoing backup activity.");
    }

    return new StateChangeJournalContext(mJournal.createJournalContext(), sharedLockResource);
  }
