    private String createTempContextXml(String docBase, Context ctx) throws IOException {
        File tmpContextXml = File.createTempFile("context", ".xml"); // NOI18N
        tmpContextXml.deleteOnExit();
        if (!docBase.equals (ctx.getAttributeValue ("docBase"))) { //NOI18N
            ctx.setAttributeValue ("docBase", docBase); //NOI18N
            FileOutputStream fos = new FileOutputStream (tmpContextXml);
            ctx.write (fos);
            fos.close ();
        }
        // http://www.netbeans.org/issues/show_bug.cgi?id=167139
        URL url = tmpContextXml.toURI().toURL();
        String ret = URLEncoder.encode(url.toString(), "UTF-8"); // NOI18N
        return ret;
    }
    
    /** Lists web modules.
     * This method runs synchronously.
     * @param target server target
     * @param state one of ENUM_ constants.
     *
     * @throws IllegalStateException when access to tomcat manager has not been
     * authorized and therefore list of target modules could not been retrieved
     */
    TargetModuleID[] list (Target t, int state) throws IllegalStateException {
        command = "list"; // NOI18N
        run ();
        if (!authorized) {
            // connection to tomcat manager has not been authorized
            String errMsg = NbBundle.getMessage(TomcatManagerImpl.class, "MSG_AuthorizationFailed",
                    tm.isAboveTomcat70() ? "manager-script" : "manager");
            IllegalStateException ise = new IllegalStateException(errMsg);
            throw (IllegalStateException)ise.initCause(new AuthorizationException());
        }
        // PENDING : error check
        java.util.List modules = new java.util.ArrayList ();
        boolean first = true;
        StringTokenizer stok = new StringTokenizer (output, "\r\n");    // NOI18N
        while (stok.hasMoreTokens ()) {
            String line = stok.nextToken ();
            if (first) {
                first = false;
            }
            else {
                StringTokenizer ltok = new StringTokenizer (line, ":"); // NOI18N
                try {
                    String ctx = ltok.nextToken ();
                    String s = ltok.nextToken ();
                    String tag = ltok.nextToken ();
                    String path = null;
                    //take the rest of line as path (it can contain ':')
                    // #50410 - path information is missing in the Japanese localization of Tomcat Manager
                    if (ltok.hasMoreTokens()) {
                        path = line.substring (ctx.length () + s.length () + tag.length () + 3);
                    }
                    if ("running".equals (s)
                    &&  (state == TomcatManager.ENUM_AVAILABLE || state == TomcatManager.ENUM_RUNNING)) {
                        modules.add (new TomcatModule (t, ctx, path));
                    }
                    if ("stopped".equals (s)
                    &&  (state == TomcatManager.ENUM_AVAILABLE || state == TomcatManager.ENUM_NONRUNNING)) {
                        modules.add (new TomcatModule (t, ctx, path));
                    }
                } catch (java.util.NoSuchElementException e) {
                    // invalid value
                    LOGGER.log(Level.FINE, line, e);
                    System.err.println(line);
                    e.printStackTrace();
                }
            }
        }
        return (TargetModuleID []) modules.toArray (new TargetModuleID[modules.size ()]);
    }
