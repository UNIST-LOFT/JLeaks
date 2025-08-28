  protected ControlFlowGraph generateMethodCFG(String file, String clas, final String method) {

    CFGProcessor cfgProcessor = new CFGProcessor(clas, method);

    Context context = new Context();
    Options.instance(context).put("compilePolicy", "ATTR_ONLY");
    JavaCompiler javac = new JavaCompiler(context);

    JavacFileManager fileManager = (JavacFileManager) context.get(JavaFileManager.class);

    JavaFileObject l = fileManager.getJavaFileObjectsFromStrings(List.of(file)).iterator().next();

    PrintStream err = System.err;
    try {
      // Redirect syserr to nothing (and prevent the compiler from issuing
      // warnings about our exception).
      System.setErr(
          new PrintStream(
              // In JDK 11+, this can be just "OutputStream.nullOutputStream()".
              new OutputStream() {
                @Override
                public void write(int b) throws IOException {}
              }));
      javac.compile(List.of(l), List.of(clas), List.of(cfgProcessor), List.nil());
    } catch (Throwable e) {
      // ok
    } finally {
      System.setErr(err);
    }

    CFGProcessResult res = cfgProcessor.getCFGProcessResult();

    if (res == null) {
      printError("internal error in type processor! method typeProcessOver() doesn't get called.");
      System.exit(1);
    }

    if (!res.isSuccess()) {
      printError(res.getErrMsg());
      System.exit(1);
    }

    return res.getCFG();
  }
