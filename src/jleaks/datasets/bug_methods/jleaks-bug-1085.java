	public static boolean isKali() {
		if (onKali == null) {
	    	onKali = false;
    		File osReleaseFile = new File ("/etc/os-release");
	    	if (isLinux() && ! isDailyBuild() && osReleaseFile.exists()) {
	    		// Ignore the fact we're on Kali if this is a daily build - they will only have been installed manually
		    	try {
		    		InputStream in = null;
		    		Properties osProps = new Properties();    		
		    		in = new FileInputStream(osReleaseFile);   
		    		osProps.load(in);
		    		String osLikeValue = osProps.getProperty("ID");
		    		if (osLikeValue != null) { 
			    		String [] oSLikes = osLikeValue.split(" ");
			    		for (String osLike: oSLikes) {
			    			if (osLike.toLowerCase().equals("kali")) {    				
			    				onKali = true;
			    				break;
			    			}
			    		}
		    		}
		    		in.close();
		    	} catch (Exception e) {
		    		// Ignore
		    	}
	    	}
		}
    	return onKali;
	}
