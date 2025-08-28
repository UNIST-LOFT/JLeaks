protected CompiledST load(String name) {
		if ( verbose ) System.out.println("STGroupDir.load("+name+")");
        String parent = Misc.getParent(name); // must have parent; it's fully-qualified
String prefix = Misc.getPrefix(name);
//    	if (parent.isEmpty()) {
//    		// no need to check for a group file as name has no parent
//            return loadTemplateFile("/", name+TEMPLATE_FILE_EXTENSION); // load t.st file
//    	}
        URL groupFileURL = null;
        try { // see if parent of template name is a group file
            groupFileURL = new URL(root+parent+GROUP_FILE_EXTENSION);
        }
        catch (MalformedURLException e) {
            errMgr.internalError(null, "bad URL: "+root+parent+GROUP_FILE_EXTENSION, e);
			return null;
        }

        InputStream is = null;
        try {
            is = groupFileURL.openStream();
        }
        catch (IOException ioe) {
            // must not be in a group file
			String unqualifiedName = Misc.getFileName(name);
            return loadTemplateFile(prefix, unqualifiedName+TEMPLATE_FILE_EXTENSION); // load t.st file
		}
        finally { // clean up
			try {
				if (is!=null ) is.close();
			}
			catch (IOException ioe) {
				errMgr.internalError(null, "can't close template file stream "+name, ioe);
			}
        }

        loadGroupFile(prefix, root+parent+GROUP_FILE_EXTENSION);
        return rawGetTemplate(name);
    }
	/** Load .st as relative file name relative to root by prefix */
	public CompiledST loadTemplateFile(String prefix, String unqualifiedFileName) {
		if ( verbose ) System.out.println("loadTemplateFile("+unqualifiedFileName+") in groupdir "+
										  "from "+root+" prefix="+prefix);
		URL f = null;
		try {
			f = new URL(root+prefix+unqualifiedFileName);
		}
		catch (MalformedURLException me) {
			errMgr.runTimeError(null, null, 0, ErrorType.INVALID_TEMPLATE_NAME,
								me, root + unqualifiedFileName);
			return null;
		}
ANTLRInputStream fs;
		try {
			fs = new ANTLRInputStream(f.openStream(), encoding);
			fs.name = unqualifiedFileName;
		}
		catch (IOException ioe) {
			if ( verbose ) System.out.println(root+"/"+unqualifiedFileName+" doesn't exist");
			//errMgr.IOError(null, ErrorType.NO_SUCH_TEMPLATE, ioe, unqualifiedFileName);
			return null;
		}

		return loadTemplateFile(prefix, unqualifiedFileName, fs);
	}