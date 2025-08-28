    protected void doApply(Plugin.Engine.Source source, Plugin.Engine.Target target) throws IOException {
        if (source().equals(target())) {
            throw new IllegalStateException("Source and target cannot be equal: " + source());
        }
        List<Transformation> transformations = new ArrayList<Transformation>(getTransformations());
        ClassLoader classLoader = ByteBuddySkippingUrlClassLoader.of(getClass().getClassLoader(), discoverySet(), classPath());
        if (discovery.isDiscover(transformations)) {
            Set<String> undiscoverable = new HashSet<String>();
            if (discovery.isRecordConfiguration()) {
                for (Transformation transformation : transformations) {
                    undiscoverable.add(transformation.toPluginName());
                }
            }
            for (String name : Plugin.Engine.Default.scan(classLoader)) {
                if (undiscoverable.add(name)) {
                    try {
                        @SuppressWarnings("unchecked")
                        Class<? extends Plugin> plugin = (Class<? extends Plugin>) Class.forName(name);
                        Transformation transformation = new Transformation();
                        transformation.setPlugin(plugin);
                        transformations.add(transformation);
                    } catch (ClassNotFoundException exception) {
                        throw new IllegalStateException("Discovered plugin is not available: " + name, exception);
                    }
                    getLogger().debug("Registered discovered plugin: {}", name);
                } else {
                    getLogger().info("Skipping discovered plugin {} which was previously discovered or registered", name);
                }
            }
        }
        if (transformations.isEmpty()) {
            getLogger().warn("No transformations are specified or discovered. Skipping plugin application.");
        } else {
            getLogger().debug("{} plugins are being applied via configuration and discovery", transformations.size());
        }
        List<Plugin.Factory> factories = new ArrayList<Plugin.Factory>(transformations.size());
        for (Transformation transformation : transformations) {
            try {
                factories.add(new Plugin.Factory.UsingReflection(transformation.toPlugin(classLoader))
                        .with(transformation.makeArgumentResolvers())
                        .with(Plugin.Factory.UsingReflection.ArgumentResolver.ForType.of(File.class, source()),
                                Plugin.Factory.UsingReflection.ArgumentResolver.ForType.of(Logger.class, getLogger()),
                                Plugin.Factory.UsingReflection.ArgumentResolver.ForType.of(org.slf4j.Logger.class, getLogger()),
                                Plugin.Factory.UsingReflection.ArgumentResolver.ForType.of(BuildLogger.class, new GradleBuildLogger(getLogger()))));
                getLogger().info("Resolved plugin: {}", transformation.toPluginName());
            } catch (Throwable throwable) {
                throw new IllegalStateException("Cannot resolve plugin: " + transformation.toPluginName(), throwable);
            }
        }
        List<ClassFileLocator> classFileLocators = new ArrayList<ClassFileLocator>();
        for (File artifact : classPath()) {
            classFileLocators.add(artifact.isFile()
                    ? ClassFileLocator.ForJarFile.of(artifact)
                    : new ClassFileLocator.ForFolder(artifact));
        }
        ClassFileLocator classFileLocator = new ClassFileLocator.Compound(classFileLocators);
        Plugin.Engine.Summary summary;
        try {
            getLogger().info("Processing class files located in in: {}", source());
            Plugin.Engine pluginEngine;
            try {
                ClassFileVersion classFileVersion;
                if (this.classFileVersion == null) {
                    classFileVersion = ClassFileVersion.ofThisVm();
                    getLogger().warn("Could not locate Java target version, build is JDK dependant: {}", classFileVersion.getJavaVersion());
                } else {
                    classFileVersion = this.classFileVersion;
                    getLogger().debug("Java version was configured: {}", classFileVersion.getJavaVersion());
                }
                pluginEngine = Plugin.Engine.Default.of(getEntryPoint(), classFileVersion, getSuffix().length() == 0
                        ? MethodNameTransformer.Suffixing.withRandomSuffix()
                        : new MethodNameTransformer.Suffixing(getSuffix()));
            } catch (Throwable throwable) {
                throw new IllegalStateException("Cannot create plugin engine", throwable);
            }
            try {
                summary = pluginEngine
                        .with(isExtendedParsing()
                                ? Plugin.Engine.PoolStrategy.Default.EXTENDED
                                : Plugin.Engine.PoolStrategy.Default.FAST)
                        .with(classFileLocator)
                        .with(new TransformationLogger(getLogger()))
                        .withErrorHandlers(Plugin.Engine.ErrorHandler.Enforcing.ALL_TYPES_RESOLVED, isFailOnLiveInitializer()
                                ? Plugin.Engine.ErrorHandler.Enforcing.NO_LIVE_INITIALIZERS
                                : Plugin.Engine.Listener.NoOp.INSTANCE, isFailFast()
                                ? Plugin.Engine.ErrorHandler.Failing.FAIL_FAST
                                : Plugin.Engine.ErrorHandler.Failing.FAIL_LAST)
                        .with(getThreads() == 0
                                ? Plugin.Engine.Dispatcher.ForSerialTransformation.Factory.INSTANCE
                                : new Plugin.Engine.Dispatcher.ForParallelTransformation.WithThrowawayExecutorService.Factory(getThreads()))
                        .apply(source, target, factories);
            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to transform class files in " + source(), throwable);
            }
        } finally {
            classFileLocator.close();
        }
        if (!summary.getFailed().isEmpty()) {
            throw new IllegalStateException(summary.getFailed() + " type transformations have failed");
        } else if (isWarnOnEmptyTypeSet() && summary.getTransformed().isEmpty()) {
            getLogger().warn("No types were transformed during plugin execution");
        } else {
            getLogger().info("Transformed {} type(s)", summary.getTransformed().size());
        }
    }
