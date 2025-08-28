protected void expandCGIScript() 
{
    StringBuilder srcPath = new StringBuilder();
    StringBuilder destPath = new StringBuilder();
    InputStream is = null;
    try {
        // paths depend on mapping
        if (cgiPathPrefix == null) {
            srcPath.append(pathInfo);
            is = context.getResourceAsStream(srcPath.toString());
            destPath.append(tmpDir);
            destPath.append(pathInfo);
        } else {
            // essentially same search algorithm as findCGI()
            srcPath.append(cgiPathPrefix);
            StringTokenizer pathWalker = new StringTokenizer(pathInfo, "/");
            // start with first element
            while (pathWalker.hasMoreElements() && (is == null)) {
                srcPath.append("/");
                srcPath.append(pathWalker.nextElement());
                is = context.getResourceAsStream(srcPath.toString());
            }
            destPath.append(tmpDir);
            destPath.append("/");
            destPath.append(srcPath);
        }
        if (is == null) {
            // didn't find anything, give up now
            if (debug >= 2) {
                log("expandCGIScript: source '" + srcPath + "' not found");
            }
            return;
        }
        File f = new File(destPath.toString());
        if (f.exists()) {
            // Don't need to expand if it already exists
            return;
        }
        // create directories
        String dirPath = destPath.toString().substring(0, destPath.toString().lastIndexOf("/"));
        File dir = new File(dirPath);
        if (!dir.mkdirs() && debug >= 2) {
            log("expandCGIScript: failed to create directories for '" + dir.getAbsolutePath() + "'");
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(f)) {
            synchronized (expandFileLock) {
                // make sure file doesn't exist
                if (f.exists()) {
                    return;
                }
                // create file
                if (!f.createNewFile()) {
                    return;
                }
                // copy data
                IOTools.flow(is, fos);
                if (debug >= 2) {
                    log("expandCGIScript: expanded '" + srcPath + "' to '" + destPath + "'");
                }
            }
        } catch (IOException ioe) {
            // delete in case file is corrupted
            if (f.exists()) {
                if (!f.delete() && debug >= 2) {
                    log("expandCGIScript: failed to delete '" + f.getAbsolutePath() + "'");
                }
            }
        }
    } finally {
        if (is != null) {
            try {
                is.close();
            } catch (IOException ioe) {
                // ignore
            }
        }
    }
}