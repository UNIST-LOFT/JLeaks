  public List<Module> findScriptsInDir(Path scriptDir) throws IOException {
    List<Path> jsFiles =
        Files.walk(scriptDir)
            .filter(p -> p.toString().toLowerCase().endsWith(".js"))
            .collect(Collectors.toList());
    List<Module> scripts = new ArrayList<>();
    // we can't do this loop as a map() operation on the previous stream because toURL() throws
    // a checked exception
    for (Path p : jsFiles) {
      scripts.add(new SourceURLModule(p.toUri().toURL()));
    }
    scripts.add(JSCallGraphUtil.getPrologueFile("prologue.js"));
    return scripts;
  }
