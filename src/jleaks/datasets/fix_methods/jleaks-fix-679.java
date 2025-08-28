	public byte[] scale(byte[] bytes, String mimeType, int width, int height)
		throws Exception {

		if ((width == 0) && (height == 0)) {
			return bytes;
		}

		File imageFile = null;
		File scaledImageFile = null;

		try {
			imageFile = _file.createTempFile(bytes);

			scaledImageFile = _file.createTempFile(mimeType);

			List<String> arguments = new ArrayList<>();

			arguments.add(imageFile.getAbsolutePath());
			arguments.add("-resize");

			if (height == 0) {
				height = width;
			}

			if (width == 0) {
				width = height;
			}

			arguments.add(StringBundler.concat(width, "x", height, ">"));
			arguments.add(scaledImageFile.getAbsolutePath());

			Future<?> future = convert(arguments);

			future.get();

			return _file.getBytes(scaledImageFile);
		}
		finally {
			if (imageFile != null) {
				imageFile.delete();
			}

			if (scaledImageFile != null) {
				scaledImageFile.delete();
			}
		}
	}
