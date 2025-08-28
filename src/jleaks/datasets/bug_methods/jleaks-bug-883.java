	private void restoreFiles(List<ZipEntry> uploadFiles) {
		String appPath = Common.ctx.getServletContext().getRealPath(
				FILE_SEPARATOR);

		byte[] buf = new byte[1024];
		try {
			for (ZipEntry zipEntry : uploadFiles) {
				InputStream zipinputstream;

				zipinputstream = this.zipFile.getInputStream(zipEntry);

				String entryName = zipEntry.getName();

				int n;

				String fileName = zipEntry.getName();

				File f = new File(appPath + fileName);

				File newFile = new File(entryName);

				String directory = newFile.getParent();

				if (directory != null) {
					if (newFile.isDirectory()) {
						break;
					}
					File dirFile = new File(appPath + directory);
					dirFile.mkdir();
				}

				FileOutputStream out = new FileOutputStream(f);

				while ((n = zipinputstream.read(buf, 0, 1024)) > -1)
					out.write(buf, 0, n);

				out.close();
				zipinputstream.close();
			}

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
