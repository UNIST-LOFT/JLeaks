static FeedImage getFeedImage(PodDBAdapter adapter, final long id) 
{
    if (verbose)
        System.out.println("STGroupDir.load(" + name + ")");
    // must have parent; it's fully-qualified
    String parent = Misc.getParent(name);
    String prefix = Misc.getPrefix(name);
    // if (parent.isEmpty()) {
    // // no need to check for a group file as name has no parent
    // return loadTemplateFile("/", name+TEMPLATE_FILE_EXTENSION); // load t.st file
    // }
    URL groupFileURL = null;
    try {
        // see if parent of template name is a group file
        groupFileURL = new URL(root + parent + GROUP_FILE_EXTENSION);
    } catch (MalformedURLException e) {
        errMgr.internalError(null, "bad URL: " + root + parent + GROUP_FILE_EXTENSION, e);
        return null;
    }
    InputStream is = null;
    try {
        is = groupFileURL.openStream();
    } catch (IOException ioe) {
        // must not be in a group file
        String unqualifiedName = Misc.getFileName(name);
        // load t.st file
        return loadTemplateFile(prefix, unqualifiedName + TEMPLATE_FILE_EXTENSION);
    } finally {
        // clean up
        try {
            if (is != null)
                is.close();
        } catch (IOException ioe) {
            errMgr.internalError(null, "can't close template file stream " + name, ioe);
        }
    }
    loadGroupFile(prefix, root + parent + GROUP_FILE_EXTENSION);
    return rawGetTemplate(name);
}