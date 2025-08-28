	public static void main(String[] args) {
		ToolDependencies.wireDeployers();

		List<String> wars = new ArrayList<>();
		List<String> jars = new ArrayList<>();

		for (String arg : args) {
			String fileName = StringUtil.toLowerCase(arg);

			if (fileName.endsWith(".war")) {
				wars.add(arg);
			}
			else if (fileName.endsWith(".jar")) {
				jars.add(arg);
			}
		}

		new BaseDeployer(wars, jars);
	}
