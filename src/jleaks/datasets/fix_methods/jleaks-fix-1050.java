    private long filesInApplicationPackage() {
        return uncheck(() -> { try (var files = Files.list(appDir.toPath())) { return files.count(); } });
    }
