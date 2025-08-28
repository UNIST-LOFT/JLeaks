private static ConfigData get(String path, Predicate<Path> fileFilter) {
        Map<String, String> map = emptyMap();
        if (path != null && !path.isEmpty()) {
            Path dir = new File(path).toPath();
            if (!Files.isDirectory(dir)) {
                log.warn("The path {} is not a directory", path);
            } else {
                try (Stream<Path> stream = Files.list(dir)) {
                    map = stream
                        .filter(fileFilter)
                        .collect(Collectors.toMap(
                            p -> p.getFileName().toString(),
                            p -> read(p)));
                } catch (IOException e) {
                    throw new ConfigException("Could not list directory " + dir, e);
                }
            }
        }
        return new ConfigData(map);
    }