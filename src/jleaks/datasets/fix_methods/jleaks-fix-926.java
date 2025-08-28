private JarFile(RandomAccessDataFile rootFile, String pathFromRoot, RandomAccessData data, JarEntryFilter filter,
			JarFileType type, Supplier<Manifest> manifestSupplier) throws IOException {
		super(rootFile.getFile());
		this.rootFile = rootFile;
		this.pathFromRoot = pathFromRoot;
		CentralDirectoryParser parser = new CentralDirectoryParser();
		this.entries = parser.addVisitor(new JarFileEntries(this, filter));
		this.type = type;
		parser.addVisitor(centralDirectoryVisitor());
		try {
			this.data = parser.parse(data, filter == null);
		}
		catch (RuntimeException ex) {
			close();
			throw ex;
		}
		this.manifestSupplier = (manifestSupplier != null) ? manifestSupplier : () -> {
			try (InputStream inputStream = getInputStream(MANIFEST_NAME)) {
				if (inputStream == null) {
					return null;
				}
				return new Manifest(inputStream);
			}
			catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		};
	}