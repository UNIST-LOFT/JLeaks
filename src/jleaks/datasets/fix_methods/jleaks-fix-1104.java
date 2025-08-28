public Set<Device> getDevicesFromPath(String path) throws IOException {
    MutableInt counter = new MutableInt(0);
    try (Stream<Path> stream = Files.walk(Paths.get(path), 1)) {
      return stream.filter(p -> p.toFile().getName().startsWith("veslot"))
            .map(p -> toDevice(p, counter))
            .collect(Collectors.toSet());
    }
  }