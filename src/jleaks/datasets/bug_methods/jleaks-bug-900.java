	private void refreshSettings() {
		int size = cache.getJadxSettings().getSrhResourceSkipSize() * 10240;
		if (size != sizeLimit
				|| !cache.getJadxSettings().getSrhResourceFileExt().equals(fileExts)) {
			clear();
			sizeLimit = size;
			fileExts = cache.getJadxSettings().getSrhResourceFileExt();
			String[] exts = fileExts.split("\\|");
			for (String ext : exts) {
				ext = ext.trim();
				if (!ext.isEmpty()) {
					anyExt = ext.equals("*");
					if (anyExt) {
						break;
					}
					extSet.add(ext);
				}
			}
			try {
				ZipFile zipFile = getZipFile(cache.getJRoot());
				traverseTree(cache.getJRoot(), zipFile); // reindex
				if (zipFile != null) {
					zipFile.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
