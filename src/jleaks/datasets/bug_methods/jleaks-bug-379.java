	private void generateClasses() {
		String fileName = opts.get("-f");
		String supersetFileName = opts.get("-s");

		try {
			J9DDRStructureStore store = new J9DDRStructureStore(fileName, supersetFileName);
			System.out.println("superset directory name : " + fileName);
			System.out.println("superset file name : " + store.getSuperSetFileName());
			InputStream inputStream = store.getSuperset();
			structureReader = new StructureReader(inputStream);
			inputStream.close();
		} catch (IOException e) {
			errorCount += 1;
			System.out.println("Problem with file: " + fileName);
			e.printStackTrace();
			return;
		}

		outputDir = getOutputDir("-p");
		if (opts.get("-h") != null) {
			// where to write the helpers to if the option is set
			outputDirHelpers = getOutputDir("-h");
		}

		typeManager = new StructureTypeManager(structureReader.getStructures());

		for (StructureDescriptor structure : structureReader.getStructures()) {
			try {
				if (FlagStructureList.isFlagsStructure(structure.getName())) {
					generateBuildFlags(structure);
				} else {
					generateClass(structure);
				}
			} catch (FileNotFoundException e) {
				errorCount += 1;
				String error = String.format("File not found processing: %s: %s", structure.getPointerName(), e.getMessage());
				System.out.println(error);
			} catch (IOException e) {
				errorCount += 1;
				String error = String.format("IOException processing: %s: %s", structure.getPointerName(), e.getMessage());
				System.out.println(error);
			}
		}
	}

	private void generateBuildFlags(StructureDescriptor structure) throws IOException {
		File javaFile = new File(outputDir, structure.getName() + ".java");
		List<String> userImports = new ArrayList<>();
		List<String> userCode = new ArrayList<>();
		collectMergeData(javaFile, userImports, userCode);

		byte[] original = null;
		int length = 0;
		if (javaFile.exists()) {
			length = (int) javaFile.length();
			original = new byte[length];
			FileInputStream fis = new FileInputStream(javaFile);
			fis.read(original);
			fis.close();
		}

		ByteArrayOutputStream newContents = new ByteArrayOutputStream(length);
		PrintWriter writer = new PrintWriter(newContents);
		String className = structure.getName();
		Map<String, String> constants = BytecodeGenerator.getConstantsAndAliases(structure);

		writeCopyright(writer);
		writer.format("package %s;%n", opts.get("-p"));
		writeBuildFlagImports(writer);
		writer.println();
		writeClassComment(writer, className);
		writer.format("public final class %s {%n", className);
		writer.println();
		writer.println("\t// Do not instantiate constant classes");
		writer.format("\tprivate %s() {%n", className);
		writer.format("\t}%n");
		writer.println();
		writeBuildFlags(writer, constants.keySet());
		writer.println();
		writeBuildFlagsStaticInitializer(writer, className, constants);
		writer.println("}");
		writer.close();

		byte[] newContentsBytes = newContents.toByteArray();
		if (null == original || !Arrays.equals(original, newContentsBytes)) {
			FileOutputStream fos = new FileOutputStream(javaFile);
			fos.write(newContentsBytes);
			fos.close();
		}
	}
