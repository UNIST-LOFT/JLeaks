    public List<MRL> getResources() {
        List<MRL> list = new ArrayList<>();
        try {
            Files.walk(path)
                    .forEach(
                            f -> {
                                if (f.endsWith("metadata.json") && Files.isRegularFile(f)) {
                                    Path relative = path.relativize(f);
                                    String type = relative.getName(0).toString();
                                    try (Reader reader = Files.newBufferedReader(f)) {
                                        Metadata metadata =
                                                JsonUtils.GSON.fromJson(reader, Metadata.class);
                                        Application application = metadata.getApplication();
                                        String groupId = metadata.getGroupId();
                                        String artifactId = metadata.getArtifactId();
                                        if ("dataset".equals(type)) {
                                            list.add(dataset(application, groupId, artifactId));
                                        } else if ("model".equals(type)) {
                                            list.add(model(application, groupId, artifactId));
                                        }
                                    } catch (IOException e) {
                                        logger.warn("Failed to read metadata.json", e);
                                    }
                                }
                            });
        } catch (IOException e) {
            logger.warn("", e);
        }
        return list;
    }
