public synchronized RepositoryDTO fetchInstallableApplications() 
{
    LOGGER.info(String.format("Begin fetching process of git-repository '%s' in '%s'", this.gitRepositoryUri, gitRepositoryLocation.getAbsolutePath()));
    boolean folderExists = gitRepositoryLocation.exists();
    // check that the repository folder exists
    if (!folderExists) {
        LOGGER.info(String.format("Creating new folder '%s' for git-repository '%s'", gitRepositoryLocation.getAbsolutePath(), this.gitRepositoryUri));
        if (!gitRepositoryLocation.mkdirs()) {
            LOGGER.error(String.format("Couldn't create folder for git repository '%s' at '%s'", this.gitRepositoryUri, gitRepositoryLocation.getAbsolutePath()));
            return new RepositoryDTO.Builder().build();
        }
    }
    RepositoryDTO result = null;
    Git gitRepository = null;
    try {
        /*
             * if the repository folder previously didn't exist, clone the
             * repository now
             */
        if (!folderExists) {
            LOGGER.info(String.format("Cloning git-repository '%s' to '%s'", this.gitRepositoryUri, gitRepositoryLocation.getAbsolutePath()));
            gitRepository = Git.cloneRepository().setURI(this.gitRepositoryUri.toString()).setDirectory(gitRepositoryLocation).call();
        } else /*
             * otherwise open the folder and pull the newest updates from the
             * repository
             */
        {
            LOGGER.info(String.format("Opening git-repository '%s' at '%s'", this.gitRepositoryUri, gitRepositoryLocation.getAbsolutePath()));
            gitRepository = Git.open(gitRepositoryLocation);
            LOGGER.info(String.format("Pulling new commits from git-repository '%s' to '%s'", this.gitRepositoryUri, gitRepositoryLocation.getAbsolutePath()));
            gitRepository.pull().call();
        }
        result = localRepositoryFactory.createInstance(this.gitRepositoryLocation, this.gitRepositoryUri).fetchInstallableApplications();
    } catch (RepositoryNotFoundException | GitAPIException e) {
        LOGGER.error(String.format("Folder '%s' is no git-repository", gitRepositoryLocation.getAbsolutePath()), e);
    } catch (IOException e) {
        LOGGER.error(String.format("An unknown error occured", e));
    } finally {
        // close repository to free resources
        if (gitRepository != null) {
            gitRepository.close();
        }
    }
    return result;
}