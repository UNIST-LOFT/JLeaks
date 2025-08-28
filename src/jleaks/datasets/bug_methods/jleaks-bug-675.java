	private void _initializeJasperVersion() {
		try {
			Class<?> clazz = getClass();

			URL url = clazz.getResource(
				"/org/apache/jasper/JasperException.class");

			if (url == null) {
				return;
			}

			String path = url.getPath();

			int pos = path.indexOf(CharPool.EXCLAMATION);

			if (pos == -1) {
				return;
			}

			URI jarFileURI = new URI(path.substring(0, pos));

			JarFile jarFile = new JarFile(new File(jarFileURI));

			Manifest manifest = jarFile.getManifest();

			Attributes attributes = manifest.getMainAttributes();

			if (attributes.containsKey(Attributes.Name.SPECIFICATION_VERSION)) {
				_jasperVersion = GetterUtil.getString(
					attributes.getValue(Attributes.Name.SPECIFICATION_VERSION));

				if (_isValidJasperVersion(_jasperVersion)) {
					return;
				}
			}

			if (attributes.containsKey(
					Attributes.Name.IMPLEMENTATION_VERSION)) {

				_jasperVersion = GetterUtil.getString(
					attributes.get(Attributes.Name.IMPLEMENTATION_VERSION));

				if (_isValidJasperVersion(_jasperVersion)) {
					return;
				}
			}

			Attributes.Name bundleVersionAttributesName = new Attributes.Name(
				"Bundle-Version");

			if (attributes.containsKey(bundleVersionAttributesName)) {
				_jasperVersion = GetterUtil.getString(
					attributes.get(bundleVersionAttributesName));

				if (_isValidJasperVersion(_jasperVersion)) {
					return;
				}

				_jasperVersion = StringPool.BLANK;
			}
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}
