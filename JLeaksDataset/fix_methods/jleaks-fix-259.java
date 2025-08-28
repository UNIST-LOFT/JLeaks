
	public void destroy() {
		if (m_fileMetadata.raf != null && m_fileMetadata.raf.getChannel().isOpen()) {
			try {
				m_fileMetadata.raf.close();
			} catch (IOException e) {
				log.warn("Problem with file close", e);
			}
		}
		
		deleteResources();
	}