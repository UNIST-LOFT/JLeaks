	public int autoDeploy(AutoDeploymentContext autoDeploymentContext)
		throws AutoDeployException {

		AutoDeployer cloneAutoDeployer = _autoDeployer.cloneAutoDeployer();

		return cloneAutoDeployer.autoDeploy(autoDeploymentContext);
	}
