	public String backupDatabase() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == false) {
			Log.w(cgSettings.tag, "Database wasn't backed up: no external memory");

			return null;
		}

		closeDb();

		try {
			final String directoryImg = cgSettings.cache;
			final String directoryTarget = Environment.getExternalStorageDirectory() + "/" + directoryImg + "/";
			final String fileTarget = directoryTarget + "cgeo.sqlite";
			final String fileSource = path;

			File directoryTargetFile = new File(directoryTarget);
			if (directoryTargetFile.exists() == false) {
				directoryTargetFile.mkdir();
			}

			InputStream input = new FileInputStream(fileSource);
			OutputStream output = new FileOutputStream(fileTarget);

			byte[] buffer = new byte[1024];
			int length;
			while ((length = input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}

			output.flush();
			output.close();
			input.close();

			Log.i(cgSettings.tag, "Database was copied to " + fileTarget);

			init();

			return fileTarget;
		} catch (Exception e) {
			Log.w(cgSettings.tag, "Database wasn't backed up: " + e.toString());
		}

		init();

		return null;
	}
