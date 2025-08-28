public void updateRepositoryModel(String repositoryName, RepositoryModel repository,
			boolean isCreate) throws GitBlitException {
    Repository r = null;
    if (isCreate) {
        // ensure created repository name ends with .git
        if (!repository.name.toLowerCase().endsWith(org.eclipse.jgit.lib.Constants.DOT_GIT_EXT)) {
            repository.name += org.eclipse.jgit.lib.Constants.DOT_GIT_EXT;
        }
        if (new File(repositoriesFolder, repository.name).exists()) {
            throw new GitBlitException(MessageFormat.format("Can not create repository ''{0}'' because it already exists.", repository.name));
        }
        // create repository
        logger.info("create repository " + repository.name);
        r = JGitUtils.createRepository(repositoriesFolder, repository.name);
    } else {
        // rename repository
        if (!repositoryName.equalsIgnoreCase(repository.name)) {
            closeRepository(repositoryName);
            File folder = new File(repositoriesFolder, repositoryName);
            File destFolder = new File(repositoriesFolder, repository.name);
            if (destFolder.exists()) {
                throw new GitBlitException(MessageFormat.format("Can not rename repository ''{0}'' to ''{1}'' because ''{1}'' already exists.", repositoryName, repository.name));
            }
            if (!folder.renameTo(destFolder)) {
                throw new GitBlitException(MessageFormat.format("Failed to rename repository ''{0}'' to ''{1}''.", repositoryName, repository.name));
            }
            // rename the roles
            if (!userService.renameRepositoryRole(repositoryName, repository.name)) {
                throw new GitBlitException(MessageFormat.format("Failed to rename repository permissions ''{0}'' to ''{1}''.", repositoryName, repository.name));
            }
        }
        // load repository
        logger.info("edit repository " + repository.name);
        try {
            r = repositoryResolver.open(null, repository.name);
        } catch (RepositoryNotFoundException e) {
            logger.error("Repository not found", e);
        } catch (ServiceNotAuthorizedException e) {
            logger.error("Service not authorized", e);
        } catch (ServiceNotEnabledException e) {
            logger.error("Service not enabled", e);
        }
    }
    // update settings
    if (r != null) {
        StoredConfig config = JGitUtils.readConfig(r);
        config.setString("gitblit", null, "description", repository.description);
        config.setString("gitblit", null, "owner", repository.owner);
        config.setBoolean("gitblit", null, "useTickets", repository.useTickets);
        config.setBoolean("gitblit", null, "useDocs", repository.useDocs);
        config.setString("gitblit", null, "accessRestriction", repository.accessRestriction.name());
        config.setBoolean("gitblit", null, "showRemoteBranches", repository.showRemoteBranches);
        config.setBoolean("gitblit", null, "isFrozen", repository.isFrozen);
        config.setBoolean("gitblit", null, "showReadme", repository.showReadme);
        try {
            config.save();
        } catch (IOException e) {
            logger.error("Failed to save repository config!", e);
        }
        r.close();
    }
}