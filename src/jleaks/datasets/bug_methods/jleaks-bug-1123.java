    private static IVersionInfo getFromClasspath(List<String> classpath, final String propFileName) {
        IVersionInfo ret = null;
        for (String part: classpath) {
            Path p = Paths.get(part);
            if (Files.isDirectory(p)) {
                Path child = p.resolve(propFileName);
                if (Files.exists(child) && !Files.isDirectory(child)) {
                    try (FileReader reader = new FileReader(child.toFile())) {
                        Properties info = new Properties();
                        info.load(reader);
                        ret = new VersionInfoImpl(info);
                        break;
                    } catch (IOException e) {
                        LOG.error("Skipping {}; got an error while trying to parse the file.", part, e);
                    }
                }
            } else if (part.toLowerCase().endsWith(".jar")
                || part.toLowerCase().endsWith(".zip")) {
                //Treat it like a jar
                try (JarFile jf = new JarFile(p.toFile())) {
                    Enumeration<? extends ZipEntry> zipEnums = jf.entries();
                    while (zipEnums.hasMoreElements()) {
                        ZipEntry entry = zipEnums.nextElement();
                        if (!entry.isDirectory() && entry.getName().equals(propFileName)) {
                            try (InputStreamReader reader = new InputStreamReader(jf.getInputStream(entry))) {
                                Properties info = new Properties();
                                info.load(reader);
                                ret = new VersionInfoImpl(info);
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    LOG.error("Skipping {}; got an error while trying to parse the jar file.", part, e);
                }
            } else if (p.endsWith("*")) {
                //for a path like /<parent-path>/*
                try {
                    Path parent = p.getParent();
                    List<String> children = new ArrayList<>();
                    Files.list(parent)
                        //avoid infinite recursion
                        .filter(path -> !path.endsWith("*"))
                        .forEach(path -> children.add(path.toString()));
                    IVersionInfo resFromChildren = getFromClasspath(children, propFileName);
                    if (resFromChildren != null) {
                        ret = resFromChildren;
                        break;
                    }
                } catch (NullPointerException | IOException e) {
                    LOG.error("Skipping {}; got an error while trying to parse it", part, e);
                }
            } else {
                LOG.warn("Skipping {}; don't know what to do with it.", part);
            }
        }
        return ret;
    }
