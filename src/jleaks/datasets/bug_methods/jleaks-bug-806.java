	public static GPXFile loadGPXFile(ClientContext ctx, File f, boolean convertCloudmadeSource) {
		try {
			GPXFile file = loadGPXFile(ctx, new FileInputStream(f), convertCloudmadeSource);
			file.path = f.getAbsolutePath();
			return file;
		} catch (FileNotFoundException e) {
			GPXFile res = new GPXFile();
			res.path = f.getAbsolutePath();
			log.error("Error reading gpx", e); //$NON-NLS-1$
			res.warning = ctx.getString(R.string.error_reading_gpx);
			return res;
		}
	}
