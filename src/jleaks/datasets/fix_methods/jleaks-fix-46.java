private boolean buildDefaultActivator(Path bindir, String activatorClassName, Writer writer)
			throws IOException {
		Path activatorSourceFileName = bindir.resolve(activatorClassName + ".java");
		try (PrintWriter activatorWriter = new PrintWriter(
			Files.newBufferedWriter(activatorSourceFileName, Charset.forName("UTF-8")))) {
			activatorWriter.println("import " + GhidraBundleActivator.class.getName() + ";");
			activatorWriter.println("import org.osgi.framework.BundleActivator;");
			activatorWriter.println("import org.osgi.framework.BundleContext;");
			activatorWriter.println("public class " + GENERATED_ACTIVATOR_CLASSNAME +
				" extends GhidraBundleActivator {");
			activatorWriter.println("  protected void start(BundleContext bc, Object api) {");
			activatorWriter.println("    // TODO: stuff to do on bundle start");
			activatorWriter.println("  }");
			activatorWriter.println("  protected void stop(BundleContext bc, Object api) {");
			activatorWriter.println("    // TODO: stuff to do on bundle stop");
			activatorWriter.println("  }");
			activatorWriter.println();
			activatorWriter.println("}");
		}
		List<String> options = new ArrayList<>();
		options.add("-g");
		options.add("-d");
		options.add(bindir.toString());
		options.add("-sourcepath");
		options.add(bindir.toString());
		options.add("-classpath");
		options.add(System.getProperty("java.class.path"));
		options.add("-proc:none");

		try (StandardJavaFileManager javaFileManager =
			compiler.getStandardFileManager(null, null, null);
				BundleJavaManager bundleJavaManager = new BundleJavaManager(
					bundleHost.getHostFramework(), javaFileManager, options);) {
			Iterable<? extends JavaFileObject> sourceFiles =
				javaFileManager.getJavaFileObjectsFromPaths(List.of(activatorSourceFileName));
			DiagnosticCollector<JavaFileObject> diagnostics =
				new DiagnosticCollector<JavaFileObject>();
			JavaCompiler.CompilationTask task = compiler.getTask(writer, bundleJavaManager,
				diagnostics, options, null, sourceFiles);
			if (!task.call()) {
				for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics
						.getDiagnostics()) {
					writer.write(diagnostic.getSource().toString() + ": " +
						diagnostic.getMessage(null) + "\n");
				}
				return false;
			}
			return true;
		}
	}