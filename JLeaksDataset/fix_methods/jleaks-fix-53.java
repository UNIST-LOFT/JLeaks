
  public static List<File> getYarnDeployDependencies() throws IOException {
    try (InputStream dependencyTree = ApexRunner.class.getResourceAsStream("dependency-tree")) {
      try (BufferedReader br = new BufferedReader(new InputStreamReader(dependencyTree))) {
        String line;
        List<String> excludes = new ArrayList<>();
        int excludeLevel = Integer.MAX_VALUE;
        while ((line = br.readLine()) != null) {
          for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (Character.isLetter(c)) {
              if (i > excludeLevel) {
                excludes.add(line.substring(i));
              } else {
                if (line.substring(i).startsWith("org.apache.hadoop")) {
                  excludeLevel = i;
                  excludes.add(line.substring(i));
                } else {
                  excludeLevel = Integer.MAX_VALUE;
                }
              }
              break;
            }
          }
        }

        Set<String> excludeJarFileNames = Sets.newHashSet();
        for (String exclude : excludes) {
          String[] mvnc = exclude.split(":");
          String fileName = mvnc[1] + "-";
          if (mvnc.length == 6) {
            fileName += mvnc[4] + "-" + mvnc[3]; // with classifier
          } else {
            fileName += mvnc[3];
          }
          fileName += ".jar";
          excludeJarFileNames.add(fileName);
        }

        ClassLoader classLoader = ApexYarnLauncher.class.getClassLoader();
        URL[] urls = ((URLClassLoader) classLoader).getURLs();
        List<File> dependencyJars = new ArrayList<>();
        for (int i = 0; i < urls.length; i++) {
          File f = new File(urls[i].getFile());
          // dependencies can also be directories in the build reactor,
          // the Apex client will automatically create jar files for those.
          if (f.exists() && !excludeJarFileNames.contains(f.getName())) {
            dependencyJars.add(f);
          }
        }
        return dependencyJars;
      }
    }
  }