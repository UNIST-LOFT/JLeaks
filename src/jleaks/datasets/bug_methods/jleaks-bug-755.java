    private List<String> getAllSpecFilesInDirectory() {
        Path rootDirectory = new File(inputSpecRootDirectory).toPath();
        try {
            return Files.walk(rootDirectory)
                .filter(path -> !Files.isDirectory(path))
                .map(path -> rootDirectory.relativize(path).toString())
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Exception while listing files in spec root directory: " + inputSpecRootDirectory, e);
        }
    }
