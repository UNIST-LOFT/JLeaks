    private long filesInApplicationPackage() {
        return uncheck(() -> Files.list(appDir.toPath()).count());
    }
