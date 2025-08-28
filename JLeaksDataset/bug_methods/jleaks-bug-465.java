  private static void initPlugins(String[] pluginPaths, String pluginOptionString)
      throws IOException {
    JarFileLoader classLoader = new JarFileLoader();
    for (String path : pluginPaths) {
      if (path.endsWith(".jar")) {
        JarInputStream jarStream = new JarInputStream(new FileInputStream(path));
        classLoader.addJarFile(new File(path).getAbsolutePath());

        JarEntry entry;
        while ((entry = jarStream.getNextJarEntry()) != null) {
          String entryName = entry.getName();
          if (!entryName.endsWith(".class")) {
            continue;
          }

          String className =
              entryName.replaceAll("/", "\\.").substring(0, entryName.length() - ".class".length());

          try {
            Class<?> clazz = classLoader.loadClass(className);
            if (Plugin.class.isAssignableFrom(clazz)) {
              Constructor<?> cons = clazz.getDeclaredConstructor();
              Plugin plugin = (Plugin) cons.newInstance();
              plugin.initPlugin(pluginOptionString);
              Options.getPlugins().add(plugin);
            }
          } catch (Exception e) {
            throw new IOException("plugin exception: ", e);
          }
        }
      } else {
        logger.warning("Don't understand plugin path entry: " + path);
      }
    }
  }
