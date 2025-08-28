  private void gatherDescendants(LockedInodePath inodePath, List<LockedInodePath> descendants) {
    Inode inode = inodePath.getInodeOrNull();
    if (inode == null || inode.isFile()) {
      return;
    }
    for (Inode child : mInodeStore.getChildren(inode.asDirectory())) {
      LockedInodePath childPath;
      try {
        childPath = inodePath.lockChild(child, LockPattern.WRITE_EDGE);
      } catch (InvalidPathException e) {
        // Child does not exist.
        continue;
      }
      try {
        descendants.add(childPath);
      } catch (Error e) {
        // If adding to descendants fails due to OOM, this object
        // will not be tracked so we must close it manually
        childPath.close();
        throw e;
      }
      gatherDescendants(childPath, descendants);
    }
  }
