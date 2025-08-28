private void gatherDescendants(LockedInodePath inodePath, List<LockedInodePath> descendants) 
{
    Inode inode = inodePath.getInodeOrNull();
    if (inode == null || inode.isFile()) {
        return;
    }
    try (CloseableIterator<? extends Inode> it = mInodeStore.getChildren(inode.asDirectory())) {
        while (it.hasNext()) {
            Inode child = it.next();
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
}