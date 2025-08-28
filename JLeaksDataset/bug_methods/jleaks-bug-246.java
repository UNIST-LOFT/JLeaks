  public void execute() throws IOException {
    out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(jarFile)));

    // Create the manifest entry in the Jar file
    writeManifestEntry(manifestContent());
    try {
      for (Map.Entry<String, Path> entry : jarEntries.entrySet()) {
        copyEntry(entry.getKey(), entry.getValue());
      }
    } finally {
      out.closeEntry();
      out.close();
    }
  }
