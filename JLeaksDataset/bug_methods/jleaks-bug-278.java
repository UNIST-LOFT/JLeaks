  public static void launch(String classPath, String mainClassLoaderName, String classLoaderName,
                            String mainClassName, String[] args) throws Exception {

    System.out.println("Launcher classpath: " + classPath);

    // Expands the classpath
    List<URL> urls = new ArrayList<>();
    for (String path : classPath.split("\\s*,\\s*")) {
      getClassPaths(path, urls);
    }

    // Get the current context classloader, default to system
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    if (contextClassLoader == null) {
      contextClassLoader = ClassLoader.getSystemClassLoader();
    }

    // ClassLoader mainClassLoader = new MainClassLoader(urls, contextClassLoader.getParent());
    Constructor<?> mainClassLoaderCons = contextClassLoader.loadClass(mainClassLoaderName)
      .getConstructor(URL[].class, ClassLoader.class);
    ClassLoader mainClassLoader = (ClassLoader) mainClassLoaderCons.newInstance(urls.toArray(new URL[urls.size()]),
                                                                                contextClassLoader.getParent());
    Thread.currentThread().setContextClassLoader(mainClassLoader);

    // Creates the MapReduceClassLoader.
    final ClassLoader classLoader = (ClassLoader) mainClassLoader.loadClass(classLoaderName).newInstance();
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        if (classLoader instanceof AutoCloseable) {
          try {
            ((AutoCloseable) classLoader).close();
          } catch (Exception e) {
            System.err.println("Failed to close ClassLoader " + classLoader);
            e.printStackTrace();
          }
        }
      }
    });

    Thread.currentThread().setContextClassLoader(classLoader);

    // Invoke MapReduceClassLoader.getTaskContextProvider()
    classLoader.getClass().getDeclaredMethod("getTaskContextProvider").invoke(classLoader);
    // Invoke StandardOutErrorRedirector.redirectToLogger()
    classLoader.loadClass("co.cask.cdap.common.logging.StandardOutErrorRedirector")
      .getDeclaredMethod("redirectToLogger", String.class)
      .invoke(null, mainClassName);

    Class<?> mainClass = classLoader.loadClass(mainClassName);
    Method mainMethod = mainClass.getMethod("main", String[].class);
    mainMethod.setAccessible(true);

    System.out.println("Launch main class " + mainClass + ".main(" + Arrays.toString(args) + ")");
    mainMethod.invoke(null, new Object[]{args});
    System.out.println("Main method returned " + mainClass);
  }
