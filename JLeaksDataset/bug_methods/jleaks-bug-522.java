	public void seedFromExternalStuff(boolean canBlock) {
		byte[] buf = new byte[32];
	    if(File.separatorChar == '/') {
	        FileInputStream fis = null;
	        File hwrng = new File("/dev/hwrng");
	        if(hwrng.exists() && hwrng.canRead()) {
		        try {
		            fis = new FileInputStream(hwrng);
		            DataInputStream dis = new DataInputStream(fis);
		            dis.readFully(buf);
		            consumeBytes(buf);
		            dis.readFully(buf);
		            consumeBytes(buf);
		        } catch (Throwable t) {
		            Logger.normal(this, "Can't read /dev/hwrng even though exists and is readable: "+t, t);
		        } finally {
		            if(fis != null) try {
		                fis.close();
		            } catch (IOException e) {
		                // Ignore
		            }
		        }
	        }
	        // Read some bits from /dev/urandom
	        try {
	            fis = new FileInputStream("/dev/urandom");
	            DataInputStream dis = new DataInputStream(fis);
	            dis.readFully(buf);
	            consumeBytes(buf);
	            dis.readFully(buf);
	            consumeBytes(buf);
	        } catch (Throwable t) {
	            Logger.normal(this, "Can't read /dev/urandom: "+t, t);
	        } finally {
	            if(fis != null) try {
                    fis.close();
                } catch (IOException e) {
                    // Ignore
                }
	        }
	        if(canBlock) {
	            // Read some bits from /dev/random
	            try {
	                fis = new FileInputStream("/dev/random");
	                DataInputStream dis = new DataInputStream(fis);
	                dis.readFully(buf);
	                consumeBytes(buf);
	                dis.readFully(buf);
	                consumeBytes(buf);
	            } catch (Throwable t) {
	                Logger.normal(this, "Can't read /dev/random: "+t, t);
	            } finally {
	                if(fis != null) try {
	                    fis.close();
	                } catch (IOException e) {
	                    // Ignore
	                }
	            }
	        }
	        fis = null;
	    } else {
	    	// Force generateSeed(), since we can't read random data from anywhere else.
	    	// Anyway, Windows's CAPI won't block.
	    	canBlock = true;
	    }
	    if(canBlock) {
		    // SecureRandom hopefully acts as a proxy for CAPI on Windows
		    buf = sr.generateSeed(32);
		    consumeBytes(buf);
		    buf = sr.generateSeed(32);
		    consumeBytes(buf);
	    }
	    // A few more bits
	    consumeString(Long.toHexString(Runtime.getRuntime().freeMemory()));
	    consumeString(Long.toHexString(Runtime.getRuntime().totalMemory()));
	}
