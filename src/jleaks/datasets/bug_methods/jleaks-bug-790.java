    private String getFirstRevision(String filePath) {
        String revision = null;
        try (org.eclipse.jgit.lib.Repository repository = getJGitRepository(getDirectoryName())) {
            Iterable<RevCommit> commits = new Git(repository).log().
                    addPath(getGitFilePath(filePath)).
                    setMaxCount(1).
                    call();
            RevCommit commit = commits.iterator().next();
            if (commit != null) {
                revision = commit.getId().getName();
            } else {
                LOGGER.log(Level.WARNING, "cannot get first revision of ''{0}'' in repository ''{1}''",
                        new Object[] {filePath, getDirectoryName()});
            }
        } catch (IOException | GitAPIException e) {
            LOGGER.log(Level.WARNING,
                    String.format("cannot get first revision of '%s' in repository '%s'",
                            filePath, getDirectoryName()), e);
        }
        return revision;
    }
