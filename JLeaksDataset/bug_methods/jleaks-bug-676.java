	public boolean isSameContent(File file, byte[] bytes, int length) {
		FileChannel fileChannel = null;

		try {
			FileInputStream fileInputStream = new FileInputStream(file);

			fileChannel = fileInputStream.getChannel();

			if (fileChannel.size() != length) {
				return false;
			}

			byte[] buffer = new byte[1024];

			ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

			int bufferIndex = 0;
			int bufferLength = -1;

			while (((bufferLength = fileChannel.read(byteBuffer)) > 0) &&
				   (bufferIndex < length)) {

				for (int i = 0; i < bufferLength; i++) {
					if (buffer[i] != bytes[bufferIndex++]) {
						return false;
					}
				}

				byteBuffer.clear();
			}

			if ((bufferIndex != length) || (bufferLength != -1)) {
				return false;
			}
			else {
				return true;
			}
		}
		catch (Exception e) {
			return false;
		}
		finally {
			if (fileChannel != null) {
				try {
					fileChannel.close();
				}
				catch (IOException ioe) {
				}
			}
		}
	}
