	protected Object deserialize(byte[] in) {
		Object rv = null;
		try {
			if (in != null) {
				ByteArrayInputStream bis = new ByteArrayInputStream(in);
				ObjectInputStream is = new ObjectInputStream(bis);
				rv = is.readObject();
				is.close();
				bis.close();
			}
		} catch (IOException e) {
			log.error("Caught IOException decoding " + in.length
					+ " bytes of data", e);
		} catch (ClassNotFoundException e) {
			log
					.error("Caught CNFE decoding " + in.length
							+ " bytes of data", e);
		}
		return rv;
	}
