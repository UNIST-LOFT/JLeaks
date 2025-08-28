	protected void pageOut(JRVirtualizable o) throws IOException {
		// Store data to a file.
		String filename = makeFilename(o);
		File file = new File(directory, filename);
		
		if (file.createNewFile()) {
			boolean deleteOnExit = JRPropertiesUtil.getInstance(jasperReportsContext).getBooleanProperty(PROPERTY_TEMP_FILES_SET_DELETE_ON_EXIT);
			if (deleteOnExit) {
				file.deleteOnExit();
			}

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				BufferedOutputStream bufferedOut = new BufferedOutputStream(fos);
				writeData(o, bufferedOut);
			}
			catch (FileNotFoundException e) {
				log.error("Error virtualizing object", e);
				throw new JRRuntimeException(e);
			}
			finally {
				if (fos != null) {
					fos.close();
				}
			}
		} else {
			if (!isReadOnly(o)) {
				throw new IllegalStateException(
						"Cannot virtualize data because the file \"" + filename
								+ "\" already exists.");
			}
		}
	}
