public int autoDeploy(AutoDeploymentContext autoDeploymentContext){
    try (AutoDeployer cloneAutoDeployer = _autoDeployer.cloneAutoDeployer()) {
        return cloneAutoDeployer.autoDeploy(autoDeploymentContext);
    } catch (IOException ioe) {
        throw new AutoDeployException(ioe);
    }
}