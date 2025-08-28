  public Set<Device> getDevicesFromPath(String path) throws IOException {
    MutableInt counter = new MutableInt(0);

    return Files.walk(Paths.get(path), 1)
      .filter(p -> p.toFile().getName().startsWith("veslot"))
      .map(p -> toDevice(p, counter))
      .collect(Collectors.toSet());
  }
