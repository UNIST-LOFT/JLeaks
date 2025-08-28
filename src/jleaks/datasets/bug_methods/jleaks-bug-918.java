    public List<String> getClassesUnder(String aPath) {
		List<String> classes = new ArrayList<String>();
		ClassSourceType cst = getClassSourceType(aPath);
		
		// Get the dex file from an apk
		if (cst == ClassSourceType.apk) {
			try {
				ZipFile archive = new ZipFile(aPath);
				for (Enumeration<? extends ZipEntry> entries = archive.entries(); entries.hasMoreElements();) {
					ZipEntry entry = entries.nextElement();
					String entryName = entry.getName();
					// We are dealing with an apk file
					if (entryName.endsWith(".dex"))
						classes.addAll(DexClassProvider.classesOfDex(new File(aPath)));
				}
				archive.close();			
			} catch (IOException e) {
				G.v().out.println("Error reading " + aPath + ": " + e.toString());
				throw new CompilationDeathException(CompilationDeathException.COMPILATION_ABORTED);
			}
		}
		// Directly load a dex file
		else if (cst == ClassSourceType.dex) {
			try {
				classes.addAll(DexClassProvider.classesOfDex(new File(aPath)));
			} catch (IOException e) {
				G.v().out.println("Error reading " + aPath + ": " + e.toString());
				throw new CompilationDeathException(CompilationDeathException.COMPILATION_ABORTED);
			}
		}
		// load Java class files from ZIP and JAR
		else if (cst == ClassSourceType.jar || cst == ClassSourceType.zip) {
			List<String> inputExtensions = new ArrayList<String>(3);
			inputExtensions.add(".class");
			inputExtensions.add(".jimple");

			try {
				ZipFile archive = new ZipFile(aPath);				
				for (Enumeration<? extends ZipEntry> entries = archive.entries(); entries.hasMoreElements();) {
					ZipEntry entry = entries.nextElement();
					String entryName = entry.getName();
					int extensionIndex = entryName.lastIndexOf('.');
					if (extensionIndex >= 0) {
						String entryExtension = entryName.substring(extensionIndex);
						if (inputExtensions.contains(entryExtension)) {
							entryName = entryName.substring(0, extensionIndex);
							entryName = entryName.replace('/', '.');
							classes.add(entryName);
						}
					}
				}
				archive.close();
			} catch (IOException e) {
				G.v().out.println("Error reading " + aPath + ": " + e.toString());
				throw new CompilationDeathException(CompilationDeathException.COMPILATION_ABORTED);
			}
		}
		else if (cst == ClassSourceType.directory) {
			File file = new File(aPath);

			File[] files = file.listFiles();
			if (files == null) {
				files = new File[1];
				files[0] = file;
			}

			for (File element : files) {
				if (element.isDirectory()) {
					List<String> l = getClassesUnder(aPath + File.separatorChar + element.getName());
					Iterator<String> it = l.iterator();
					while (it.hasNext()) {
						String s = it.next();
						classes.add(element.getName() + "." + s);
					}
				} else {
					String fileName = element.getName();

					if (fileName.endsWith(".class")) {
						int index = fileName.lastIndexOf(".class");
						classes.add(fileName.substring(0, index));
					}

					if (fileName.endsWith(".jimple")) {
						int index = fileName.lastIndexOf(".jimple");
						classes.add(fileName.substring(0, index));
					}

					if (fileName.endsWith(".java")) {
						int index = fileName.lastIndexOf(".java");
						classes.add(fileName.substring(0, index));
					}
					if (fileName.endsWith(".dex")) {
						try {
							classes.addAll(DexClassProvider.classesOfDex(element));
						} catch (IOException e) { /* Ignore unreadable files */
						}
					}
				}
			}
		}
		else
			throw new RuntimeException("Invalid class source type");
		return classes;
	}
