    public void run() {
        boolean spoolOutput = getSpoolOutput().get();
        if (spoolOutput && getCaptureOutput().get()) {
            throw new GradleException("Capturing output is not supported when spoolOutput is true.");
        }
        if (getCaptureOutput().get() && getIndentingConsoleOutput().isPresent()) {
            throw new GradleException("Capturing output is not supported when indentingConsoleOutput is configured.");
        }
        Consumer<Logger> outputLogger;
        OutputStream out;
        if (spoolOutput) {
            File spoolFile = new File(projectLayout.getBuildDirectory().dir("buffered-output").get().getAsFile(), this.getName());
            out = new LazyFileOutputStream(spoolFile);
            outputLogger = logger -> {
                try {
                    // the file may not exist if the command never output anything
                    if (Files.exists(spoolFile.toPath())) {
                        try (var lines = Files.lines(spoolFile.toPath())) {
                            lines.forEach(logger::error);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException("could not log", e);
                }
            };
        } else {
            out = new ByteArrayOutputStream();
            outputLogger = getIndentingConsoleOutput().isPresent() ? logger -> {} : logger -> logger.error(byteStreamToString(out));
        }

        OutputStream finalOutputStream = getIndentingConsoleOutput().isPresent()
            ? new IndentingOutputStream(System.out, getIndentingConsoleOutput().get())
            : out;
        ExecResult execResult = execOperations.exec(execSpec -> {
            execSpec.setIgnoreExitValue(true);
            execSpec.setStandardOutput(finalOutputStream);
            execSpec.setErrorOutput(finalOutputStream);
            execSpec.setExecutable(getExecutable().get());
            execSpec.setEnvironment(getEnvironment().get());
            if (getArgs().isPresent()) {
                execSpec.setArgs(getArgs().get());
            }
            if (getWorkingDir().isPresent()) {
                execSpec.setWorkingDir(getWorkingDir().get());
            }
            if (getStandardInput().isPresent()) {
                try {
                    execSpec.setStandardInput(new ByteArrayInputStream(getStandardInput().get().getBytes("UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    throw new GradleException("Cannot set standard input", e);
                }
            }
        });
        int exitValue = execResult.getExitValue();

        if (exitValue == 0 && getCaptureOutput().get()) {
            output = byteStreamToString(out);
        }
        if (getLogger().isInfoEnabled() == false) {
            if (exitValue != 0) {
                try {
                    if (getIndentingConsoleOutput().isPresent() == false) {
                        getLogger().error("Output for " + getExecutable().get() + ":");
                    }
                    outputLogger.accept(getLogger());
                } catch (Exception e) {
                    throw new GradleException("Failed to read exec output", e);
                }
                throw new GradleException(
                    String.format("Process '%s %s' finished with non-zero exit value %d", getExecutable().get(), getArgs().get(), exitValue)
                );
            }
        }

    }
