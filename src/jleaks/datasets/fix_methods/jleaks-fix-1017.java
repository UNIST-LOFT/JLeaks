  private Map<String, String> buildLookup() {
    Map<String, String> result = new HashMap<>();

    if (checker.hasOption("propfiles")) {
      String names = checker.getOption("propfiles");
      String[] namesArr = names.split(":");

      if (namesArr == null) {
        System.err.println("Couldn't parse the properties files: <" + names + ">");
      } else {
        for (String name : namesArr) {
          try {
            Properties prop = new Properties();

            ClassLoader cl = this.getClass().getClassLoader();
            if (cl == null) {
              // The class loader is null if the system class loader was used.
              cl = ClassLoader.getSystemClassLoader();
            }
            try (InputStream in = cl.getResourceAsStream(name)) {

              if (in != null) {
                prop.load(in);
              } else {
                // If the classloader didn't manage to load the file, try whether a
                // FileInputStream works. For absolute paths this might help.
                try (InputStream fis = new FileInputStream(name)) {
                  prop.load(fis);
                } catch (FileNotFoundException e) {
                  System.err.println("Couldn't find the properties file: " + name);
                  // report(null, "propertykeychecker.filenotfound", name);
                  // return Collections.emptySet();
                  continue;
                }
              }

              for (String key : prop.stringPropertyNames()) {
                result.put(key, prop.getProperty(key));
              }
            }
          } catch (Exception e) {
            // TODO: is there a nicer way to report messages, that are not connected to
            // an AST node?  One cannot use `report`, because it needs a node.
            System.err.println(
                "Exception in PropertyKeyChecker.keysOfPropertyFile while processing "
                    + name
                    + ": "
                    + e);
            e.printStackTrace();
          }
        }
      }
    }

    if (checker.hasOption("bundlenames")) {
      String bundleNames = checker.getOption("bundlenames");
      String[] namesArr = bundleNames.split(":");

      if (namesArr == null) {
        System.err.println("Couldn't parse the resource bundles: <" + bundleNames + ">");
      } else {
        for (String bundleName : namesArr) {
          if (!Signatures.isBinaryName(bundleName)) {
            System.err.println(
                "Malformed resource bundle: <" + bundleName + "> should be a binary name.");
            continue;
          }
          ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
          if (bundle == null) {
            System.err.println(
                "Couldn't find the resource bundle: <"
                    + bundleName
                    + "> for locale <"
                    + Locale.getDefault()
                    + ">.");
            continue;
          }

          for (String key : bundle.keySet()) {
            result.put(key, bundle.getString(key));
          }
        }
      }
    }

    return result;
  }
