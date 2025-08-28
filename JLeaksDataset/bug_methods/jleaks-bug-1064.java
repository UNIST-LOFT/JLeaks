  final public void addInputJar(File f) throws IOException {
    JarFile jf = new JarFile(f, false);
    for (Enumeration<JarEntry> e = jf.entries(); e.hasMoreElements();) {
      JarEntry entry = e.nextElement();
      String name = entry.getName();
      inputs.add(new JarInput(f, name));
    }
    jf.close();
  }
