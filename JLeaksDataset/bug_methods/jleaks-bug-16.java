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
      descendants.add(childPath);
      gatherDescendants(childPath, descendants);
    }
  }
