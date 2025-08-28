public JavacErrorDetail[] compile(String className, Nodes pageNodes){
    final String source = charArrayWriter.toString();
    classFiles = new ArrayList<>();
    JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
    if (javaCompiler == null) {
        errDispatcher.jspError("jsp.error.nojdk");
        throw new JasperException("Unable to find java compiler");
    }
    DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
    StandardJavaFileManager standardJavaFileManager = javaCompiler.getStandardFileManager(diagnosticCollector, null, null);
    String simpleName = className.substring(className.lastIndexOf('.') + 1);
    JavaFileObject[] javaFileObjects = { new SimpleJavaFileObject(URI.create("string:///" + simpleName.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE) {

        @Override
        public CharSequence getCharContent(boolean ignore) {
            return source;
        }
    } };
    try {
        standardJavaFileManager.setLocation(StandardLocation.CLASS_PATH, cpath);
    } catch (IOException ioe) {
        throw new JasperException(ioe);
    }
    try (JavaFileManager javaFileManager = getJavaFileManager(standardJavaFileManager)) {
        CompilationTask compilationTask = javaCompiler.getTask(null, javaFileManager, diagnosticCollector, options, null, Arrays.asList(javaFileObjects));
        if (compilationTask.call()) {
            for (BytecodeFile bytecodeFile : classFiles) {
                rtctxt.setBytecode(bytecodeFile.getClassName(), bytecodeFile.getBytecode());
            }
            return null;
        }
    } catch (IOException ioe) {
        throw new JasperException(ioe);
    }
    List<JavacErrorDetail> javacErrorDetails = new ArrayList<>();
    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnosticCollector.getDiagnostics()) {
        javacErrorDetails.add(ErrorDispatcher.createJavacError(javaFileName, pageNodes, new StringBuilder(diagnostic.getMessage(null)), (int) diagnostic.getLineNumber()));
    }
    return javacErrorDetails.toArray(new JavacErrorDetail[javacErrorDetails.size()]);
}