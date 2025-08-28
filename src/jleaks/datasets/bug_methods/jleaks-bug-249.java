  public ExitCode exec(CommandEnvironment env, OptionsProvider options) {
    BlazeRuntime runtime = env.getRuntime();
    QueryOptions queryOptions = options.getOptions(QueryOptions.class);

    try {
      env.setupPackageCache(
          options.getOptions(PackageCacheOptions.class),
          runtime.getDefaultsPackageContent());
    } catch (InterruptedException e) {
      env.getReporter().handle(Event.error("query interrupted"));
      return ExitCode.INTERRUPTED;
    } catch (AbruptExitException e) {
      env.getReporter().handle(Event.error(null, "Unknown error: " + e.getMessage()));
      return e.getExitCode();
    }

    String query;
    if (!options.getResidue().isEmpty()) {
      if (!queryOptions.queryFile.isEmpty()) {
        env.getReporter()
            .handle(Event.error("Command-line query and --query_file cannot both be specified"));
        return ExitCode.COMMAND_LINE_ERROR;
      }
      query = Joiner.on(' ').join(options.getResidue());
    } else if (!queryOptions.queryFile.isEmpty()) {
      // Works for absolute or relative query file.
      Path residuePath = env.getWorkingDirectory().getRelative(queryOptions.queryFile);
      try {
        query = new String(FileSystemUtils.readContent(residuePath), StandardCharsets.UTF_8);
      } catch (IOException e) {
        env.getReporter()
            .handle(Event.error("I/O error reading from " + residuePath.getPathString()));
        return ExitCode.COMMAND_LINE_ERROR;
      }
    } else {
      env.getReporter().handle(Event.error(String.format(
          "missing query expression. Type '%s help query' for syntax and help",
          runtime.getProductName())));
      return ExitCode.COMMAND_LINE_ERROR;
    }

    Iterable<OutputFormatter> formatters = runtime.getQueryOutputFormatters();
    OutputFormatter formatter =
        OutputFormatter.getFormatter(formatters, queryOptions.outputFormat);
    if (formatter == null) {
      env.getReporter().handle(Event.error(
          String.format("Invalid output format '%s'. Valid values are: %s",
              queryOptions.outputFormat, OutputFormatter.formatterNames(formatters))));
      return ExitCode.COMMAND_LINE_ERROR;
    }

    Set<Setting> settings = queryOptions.toSettings();
    boolean streamResults = QueryOutputUtils.shouldStreamResults(queryOptions, formatter);
    AbstractBlazeQueryEnvironment<Target> queryEnv = newQueryEnvironment(
        env,
        queryOptions.keepGoing,
        !streamResults,
        queryOptions.universeScope, queryOptions.loadingPhaseThreads,
        settings);

    // 1. Parse and transform query:
    QueryExpression expr;
    try {
      expr = QueryExpression.parse(query, queryEnv);
    } catch (QueryException e) {
      env.getReporter().handle(Event.error(
          null, "Error while parsing '" + query + "': " + e.getMessage()));
      return ExitCode.COMMAND_LINE_ERROR;
    }
    expr = queryEnv.transformParsedQuery(expr);

    QueryEvalResult result;
    PrintStream output = null;
    OutputFormatterCallback<Target> callback;
    if (streamResults) {
      disableAnsiCharactersFiltering(env);
      output = new PrintStream(env.getReporter().getOutErr().getOutputStream());
      // 2. Evaluate expression:
      StreamedFormatter streamedFormatter = ((StreamedFormatter) formatter);
      streamedFormatter.setOptions(queryOptions, queryOptions.aspectDeps.createResolver(
          env.getPackageManager(), env.getReporter()));
      callback = streamedFormatter.createStreamCallback(output);
    } else {
      callback = new AggregateAllOutputFormatterCallback<>();
    }
    boolean catastrophe = true;
    try {
      callback.start();
      result = queryEnv.evaluateQuery(expr, callback);
      catastrophe = false;
    } catch (QueryException e) {
      catastrophe = false;
      // Keep consistent with reportBuildFileError()
      env.getReporter()
         // TODO(bazel-team): this is a kludge to fix a bug observed in the wild. We should make
         // sure no null error messages ever get in.
         .handle(Event.error(e.getMessage() == null ? e.toString() : e.getMessage()));
      return ExitCode.ANALYSIS_FAILURE;
    } catch (InterruptedException e) {
      catastrophe = false;
      IOException ioException = callback.getIoException();
      if (ioException == null || ioException instanceof ClosedByInterruptException) {
        env.getReporter().handle(Event.error("query interrupted"));
        return ExitCode.INTERRUPTED;
      } else {
        env.getReporter().handle(Event.error("I/O error: " + e.getMessage()));
        return ExitCode.LOCAL_ENVIRONMENTAL_ERROR;
      }
    } catch (IOException e) {
      catastrophe = false;
      env.getReporter().handle(Event.error("I/O error: " + e.getMessage()));
      return ExitCode.LOCAL_ENVIRONMENTAL_ERROR;
    } finally {
      if (!catastrophe) {
        if (streamResults) {
          output.flush();
          queryEnv.afterCommand();
        }
        try {
          callback.close();
        } catch (IOException e) {
          env.getReporter().handle(Event.error("I/O error: " + e.getMessage()));
          return ExitCode.LOCAL_ENVIRONMENTAL_ERROR;
        }
      }
    }

    env.getEventBus().post(new NoBuildEvent());
    if (!streamResults) {
      disableAnsiCharactersFiltering(env);
      output = new PrintStream(env.getReporter().getOutErr().getOutputStream());

      // 3. Output results:
      try {
        Set<Target> targets = ((AggregateAllOutputFormatterCallback<Target>) callback).getOutput();
        QueryOutputUtils.output(queryOptions, result,
            targets, formatter, output,
            queryOptions.aspectDeps.createResolver(
                env.getPackageManager(), env.getReporter()));
      } catch (ClosedByInterruptException | InterruptedException e) {
        env.getReporter().handle(Event.error("query interrupted"));
        return ExitCode.INTERRUPTED;
      } catch (IOException e) {
        env.getReporter().handle(Event.error("I/O error: " + e.getMessage()));
        return ExitCode.LOCAL_ENVIRONMENTAL_ERROR;
      } finally {
        queryEnv.afterCommand();
        // Note that PrintStream#checkError first flushes and then returns whether any
        // error was ever encountered.
        if (output.checkError()) {
          // Unfortunately, there's no way to check the current error status of PrintStream
          // without forcing a flush, so we don't know whether this error happened before or after
          // timewise the one we already have from above. Neither choice is always correct, so we
          // arbitrarily choose the exit code corresponding to the PrintStream's error.
          env.getReporter().handle(Event.error("I/O error while writing query output"));
          return ExitCode.LOCAL_ENVIRONMENTAL_ERROR;
        }
      }
    }
  }
