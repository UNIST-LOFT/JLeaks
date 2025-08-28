	private void write_seed(File filename) {
		synchronized(this) {
			long now = System.currentTimeMillis();
			if(now - timeLastWroteSeed <= 60*1000) {
				return;
			} else
				timeLastWroteSeed = now;
		}
		
		try {
			DataOutputStream dos =
				new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
			for (int i = 0; i < 32; i++)
				dos.writeLong(nextLong());
			dos.close();
		} catch (Exception e) {
		}
		
	}
