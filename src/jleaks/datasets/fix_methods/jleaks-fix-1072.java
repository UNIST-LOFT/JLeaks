public void execute() throws MojoExecutionException, MojoFailureException 
{
    log = getLog();
    if (skip) {
        log.info("Bypass ProGuard processing because \"proguard.skip=true\"");
        return;
    }
    boolean mainIsJar = mavenProject.getPackaging().equals("jar");
    File inJarFile = new File(outputDirectory, injar);
    if (!inJarFile.exists()) {
        if (injarNotExistsSkip) {
            log.info("Bypass ProGuard processing because \"injar\" dos not exist");
            return;
        } else if (mainIsJar) {
            throw new MojoFailureException("Can't find file " + inJarFile);
        }
    }
    if (!outputDirectory.exists()) {
        if (!outputDirectory.mkdirs()) {
            throw new MojoFailureException("Can't create " + outputDirectory);
        }
    }
    File outJarFile;
    boolean sameArtifact;
    if (attach) {
        outjar = nameNoType(injar);
        if (useArtifactClassifier()) {
            outjar += "-" + attachArtifactClassifier;
        }
        outjar += "." + attachArtifactType;
    }
    if ((outjar != null) && (!outjar.equals(injar))) {
        sameArtifact = false;
        outJarFile = (new File(outputDirectory, outjar)).getAbsoluteFile();
        if (outJarFile.exists()) {
            if (!deleteFileOrDirectory(outJarFile)) {
                throw new MojoFailureException("Can't delete " + outJarFile);
            }
        }
    } else {
        sameArtifact = true;
        outJarFile = inJarFile.getAbsoluteFile();
        File baseFile;
        if (inJarFile.isDirectory()) {
            baseFile = new File(outputDirectory, nameNoType(injar) + "_proguard_base");
        } else {
            baseFile = new File(outputDirectory, nameNoType(injar) + "_proguard_base.jar");
        }
        if (baseFile.exists()) {
            if (!deleteFileOrDirectory(baseFile)) {
                throw new MojoFailureException("Can't delete " + baseFile);
            }
        }
        if (inJarFile.exists()) {
            if (!inJarFile.renameTo(baseFile)) {
                throw new MojoFailureException("Can't rename " + inJarFile);
            }
        }
        inJarFile = baseFile;
    }
    ArrayList<String> args = new ArrayList<String>();
    ArrayList<File> libraryJars = new ArrayList<File>();
    if (log.isDebugEnabled()) {
        @SuppressWarnings("unchecked")
        List<Artifact> dependancy = mavenProject.getCompileArtifacts();
        for (Artifact artifact : dependancy) {
            log.debug("--- compile artifact " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getType() + ":" + artifact.getClassifier() + " Scope:" + artifact.getScope());
        }
        @SuppressWarnings("unchecked")
        final Set<Artifact> artifacts = mavenProject.getArtifacts();
        for (Artifact artifact : artifacts) {
            log.debug("--- artifact " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getType() + ":" + artifact.getClassifier() + " Scope:" + artifact.getScope());
        }
        @SuppressWarnings("unchecked")
        final List<Dependency> dependencies = mavenProject.getDependencies();
        for (Dependency artifact : dependencies) {
            log.debug("--- dependency " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getType() + ":" + artifact.getClassifier() + " Scope:" + artifact.getScope());
        }
    }
    Set<String> inPath = new HashSet<String>();
    Map<Artifact, Inclusion> injars = new HashMap<Artifact, Inclusion>();
    Map<Artifact, Inclusion> libraryjars = new HashMap<Artifact, Inclusion>();
    boolean hasInclusionLibrary = false;
    if (assembly != null && assembly.inclusions != null) {
        for (Inclusion inc : assembly.inclusions) {
            for (Artifact artifact : getDependencies(inc, mavenProject)) {
                if (inc.library) {
                    if (!injars.containsKey(artifact)) {
                        libraryjars.put(artifact, inc);
                    }
                } else {
                    injars.put(artifact, inc);
                    if (libraryjars.containsKey(artifact)) {
                        libraryjars.remove(artifact);
                    }
                }
            }
        }
        for (Entry<Artifact, Inclusion> entry : injars.entrySet()) {
            log.info("--- ADD injars:" + entry.getKey().getArtifactId());
            File file = getClasspathElement(entry.getKey(), mavenProject);
            inPath.add(file.toString());
            StringBuilder filter = new StringBuilder(fileToString(file));
            filter.append("(!META-INF/MANIFEST.MF");
            if (!addMavenDescriptor) {
                filter.append(",");
                filter.append("!META-INF/maven");
            }
            if (entry.getValue().filter != null) {
                filter.append(",").append(entry.getValue().filter);
            }
            filter.append(")");
            args.add("-injars");
            args.add(filter.toString());
        }
        for (Entry<Artifact, Inclusion> entry : libraryjars.entrySet()) {
            log.info("--- ADD libraryjars:" + entry.getKey().getArtifactId());
            File file = getClasspathElement(entry.getKey(), mavenProject);
            hasInclusionLibrary = true;
            inPath.add(file.toString());
            if (putLibraryJarsInTempDir) {
                libraryJars.add(file);
            } else {
                args.add("-libraryjars");
                args.add(libFileToStringWithInLibsFilter(file));
            }
        }
    }
    if (inJarFile.exists()) {
        args.add("-injars");
        StringBuilder filter = new StringBuilder(fileToString(inJarFile));
        if ((inFilter != null) || (!addMavenDescriptor)) {
            filter.append("(");
            boolean coma = false;
            if (!addMavenDescriptor) {
                coma = true;
                filter.append("!META-INF/maven");
            }
            if (inFilter != null) {
                if (coma) {
                    filter.append(",");
                }
                filter.append(inFilter);
            }
            filter.append(")");
        }
        args.add(filter.toString());
    }
    if (includeDependency) {
        @SuppressWarnings("unchecked")
        List<Artifact> dependency = this.mavenProject.getCompileArtifacts();
        for (Artifact artifact : dependency) {
            // dependency filter
            if (isExclusion(artifact)) {
                continue;
            }
            File file = getClasspathElement(artifact, mavenProject);
            if (inPath.contains(file.toString())) {
                log.debug("--- ignore library since one in injar:" + artifact.getArtifactId());
                continue;
            }
            if (includeDependencyInjar) {
                log.debug("--- ADD library as injars:" + artifact.getArtifactId());
                args.add("-injars");
                args.add(fileToString(file));
            } else {
                log.debug("--- ADD libraryjars:" + artifact.getArtifactId());
                if (putLibraryJarsInTempDir) {
                    libraryJars.add(file);
                } else {
                    args.add("-libraryjars");
                    args.add(libFileToStringWithInLibsFilter(file));
                }
            }
        }
    }
    if (args.contains("-injars")) {
        args.add("-outjars");
        StringBuilder filter = new StringBuilder(fileToString(outJarFile));
        if (outFilter != null) {
            filter.append("(").append(outFilter).append(")");
        }
        args.add(filter.toString());
    }
    if (!obfuscate) {
        args.add("-dontobfuscate");
    }
    if (proguardInclude != null) {
        if (proguardInclude.exists()) {
            args.add("-include");
            args.add(fileToString(proguardInclude));
            log.debug("proguardInclude " + proguardInclude);
        } else {
            log.debug("proguardInclude config does not exists " + proguardInclude);
        }
    }
    if (libs != null) {
        for (String lib : libs) {
            if (putLibraryJarsInTempDir) {
                libraryJars.add(new File(lib));
            } else {
                args.add("-libraryjars");
                args.add(libFileToStringWithInLibsFilter(lib));
            }
        }
    }
    if (!libraryJars.isEmpty()) {
        log.debug("Copy libraryJars to temporary directory");
        log.debug("Temporary directory: " + tempLibraryjarsDir);
        if (tempLibraryjarsDir.exists()) {
            try {
                FileUtils.deleteDirectory(tempLibraryjarsDir);
            } catch (IOException ignored) {
                throw new MojoFailureException("Deleting failed libraryJars directory", ignored);
            }
        }
        tempLibraryjarsDir.mkdir();
        if (!tempLibraryjarsDir.exists()) {
            throw new MojoFailureException("Can't create temporary libraryJars directory: " + tempLibraryjarsDir.getAbsolutePath());
        }
        // Use this subdirectory for all libraries that are files, and not directories themselves
        File commonDir = new File(tempLibraryjarsDir, "0");
        commonDir.mkdir();
        int directoryIndex = 1;
        for (File libraryJar : libraryJars) {
            try {
                log.debug("Copying library: " + libraryJar);
                if (libraryJar.isFile()) {
                    FileUtils.copyFileToDirectory(libraryJar, commonDir);
                } else {
                    File subDir = new File(tempLibraryjarsDir, String.valueOf(directoryIndex));
                    FileUtils.copyDirectory(libraryJar, subDir);
                    args.add("-libraryjars");
                    args.add(libFileToStringWithInLibsFilter(subDir));
                }
            } catch (IOException e) {
                throw new MojoFailureException("Can't copy to temporary libraryJars directory", e);
            }
            directoryIndex++;
        }
        args.add("-libraryjars");
        args.add(libFileToStringWithInLibsFilter(commonDir));
    }
    File mappingFile = new File(outputDirectory, mappingFileName);
    args.add("-printmapping");
    args.add(fileToString(mappingFile.getAbsoluteFile()));
    args.add("-printseeds");
    args.add(fileToString((new File(outputDirectory, seedFileName).getAbsoluteFile())));
    if (incremental && applyMappingFile == null) {
        throw new MojoFailureException("applyMappingFile is required if incremental is true");
    }
    if (applyMappingFile != null && (!incremental || applyMappingFile.exists())) {
        args.add("-applymapping");
        args.add(fileToString(applyMappingFile.getAbsoluteFile()));
    }
    if (log.isDebugEnabled()) {
        args.add("-verbose");
    }
    if (options != null) {
        Collections.addAll(args, options);
    }
    if (generateTemporaryConfigurationFile) {
        log.info("building config file");
        StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) {
            if (arg.startsWith("-")) {
                stringBuilder.append("\n");
            } else {
                stringBuilder.append(" ");
            }
            stringBuilder.append(arg);
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(temporaryConfigurationFile);
            IOUtils.write(stringBuilder.toString(), writer);
        } catch (IOException e) {
            throw new MojoFailureException("cannot write to temporary configuration file " + temporaryConfigurationFile, e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        args = new ArrayList<String>();
        args.add("-include");
        args.add(fileToString(temporaryConfigurationFile));
    }
    log.info("execute ProGuard " + args.toString());
    proguardMain(getProguardJars(this), args, this);
    if (!libraryJars.isEmpty()) {
        deleteFileOrDirectory(tempLibraryjarsDir);
    }
    if ((assembly != null) && (hasInclusionLibrary)) {
        log.info("creating assembly");
        File baseFile = new File(outputDirectory, nameNoType(injar) + "_proguard_result.jar");
        if (baseFile.exists()) {
            if (!baseFile.delete()) {
                throw new MojoFailureException("Can't delete " + baseFile);
            }
        }
        File archiverFile = outJarFile.getAbsoluteFile();
        if (!outJarFile.renameTo(baseFile)) {
            throw new MojoFailureException("Can't rename " + outJarFile);
        }
        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(jarArchiver);
        archiver.setOutputFile(archiverFile);
        archive.setAddMavenDescriptor(addMavenDescriptor);
        try {
            jarArchiver.addArchivedFileSet(baseFile);
            for (Entry<Artifact, Inclusion> entry : libraryjars.entrySet()) {
                File file;
                file = getClasspathElement(entry.getKey(), mavenProject);
                if (file.isDirectory()) {
                    getLog().info("merge project: " + entry.getKey() + " " + file);
                    jarArchiver.addDirectory(file);
                } else {
                    getLog().info("merge artifact: " + entry.getKey());
                    // Respect filter if set
                    String filter = entry.getValue().filter;
                    if (filter == null) {
                        jarArchiver.addArchivedFileSet(file);
                    } else {
                        // Filter elements must be separated int two lists
                        List<String> includes = new ArrayList<String>();
                        List<String> excludes = new ArrayList<String>();
                        // Elements starting with ! should be excluded while others should be included
                        for (String element : filter.split(",")) {
                            if (element.startsWith("!")) {
                                excludes.add(element.substring(1));
                            } else {
                                includes.add(element);
                            }
                        }
                        // Null is important on empty includes otherwise nothing gets included
                        jarArchiver.addArchivedFileSet(file, (includes.isEmpty() ? null : includes.toArray(new String[0])), (excludes.isEmpty() ? null : excludes.toArray(new String[0])));
                    }
                }
            }
            archiver.createArchive(mavenProject, archive);
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to create jar", e);
        }
    }
    if (incremental) {
        log.info("Merging mapping file into " + applyMappingFile);
        try {
            FileInputStream mappingFileIn = new FileInputStream(mappingFile);
            try {
                applyMappingFile.getParentFile().mkdirs();
                FileOutputStream mappingFileOut = new FileOutputStream(applyMappingFile, true);
                try {
                    IOUtils.copy(mappingFileIn, mappingFileOut);
                } finally {
                    mappingFileOut.close();
                }
            } finally {
                mappingFileIn.close();
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to merge mapping file", e);
        }
    }
    if (attach) {
        if (!sameArtifact) {
            final String classifier;
            if (useArtifactClassifier()) {
                classifier = attachArtifactClassifier;
            } else {
                classifier = null;
            }
            projectHelper.attachArtifact(mavenProject, attachArtifactType, classifier, outJarFile);
        }
        final String mainClassifier = useArtifactClassifier() ? attachArtifactClassifier : null;
        final File buildOutput = new File(mavenProject.getBuild().getDirectory());
        if (attachMap) {
            attachTextFile(new File(buildOutput, mappingFileName), mainClassifier, "map");
        }
        if (attachSeed) {
            attachTextFile(new File(buildOutput, seedFileName), mainClassifier, "seed");
        }
    }
}