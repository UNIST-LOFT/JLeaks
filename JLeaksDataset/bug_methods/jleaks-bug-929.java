	private boolean checkForDevtools() {
		try {
			URL[] urls = getClassPathUrls();
			URLClassLoader classLoader = new URLClassLoader(urls);
			return (classLoader.findResource(RESTARTER_CLASS_LOCATION) != null);
		}
		catch (Exception ex) {
			return false;
		}
	}

