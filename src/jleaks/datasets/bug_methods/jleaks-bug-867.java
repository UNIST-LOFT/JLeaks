  private void addArchive(String path, File file, int type, boolean isOwn) throws IOException {

    ZipFile archive;

    if (type == ContextUnit.TYPE_JAR) {  // jar
      archive = new JarFile(file);
    }
    else {  // zip
      archive = new ZipFile(file);
    }

    Enumeration<? extends ZipEntry> en = archive.entries();
    while (en.hasMoreElements()) {
      ZipEntry entr = en.nextElement();

      ContextUnit unit = units.get(path + "/" + file.getName());
      if (unit == null) {
        unit = new ContextUnit(type, path, file.getName(), isOwn, saver, decdata);
        if (type == ContextUnit.TYPE_JAR) {
          unit.setManifest(((JarFile)archive).getManifest());
        }
        units.put(path + "/" + file.getName(), unit);
      }

      String name = entr.getName();
      if (!entr.isDirectory()) {
        if (name.endsWith(".class")) {
          StructClass cl = new StructClass(archive.getInputStream(entr), isOwn, loader);
          classes.put(cl.qualifiedName, cl);

          unit.addClass(cl, name);

          if (loader != null) {
            loader.addClassLink(cl.qualifiedName, new LazyLoader.Link(LazyLoader.Link.ENTRY, file.getAbsolutePath(), name));
          }
        }
        else {
          unit.addOtherEntry(file.getAbsolutePath(), name);
        }
      }
      else if (entr.isDirectory()) {
        unit.addDirEntry(name);
      }
    }
  }
