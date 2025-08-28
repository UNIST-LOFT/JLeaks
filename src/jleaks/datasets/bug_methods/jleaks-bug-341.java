    protected Applet createApplet(final AppletClassLoader loader) throws ClassNotFoundException,
                                                                         IllegalAccessException, IOException, InstantiationException, InterruptedException {
        final String serName = getSerializedObject();
        String code = getCode();

        if (code != null && serName != null) {
            System.err.println(amh.getMessage("runloader.err"));
//          return null;
            throw new InstantiationException("Either \"code\" or \"object\" should be specified, but not both.");
        }
        if (code == null && serName == null) {
            String msg = "nocode";
            status = APPLET_ERROR;
            showAppletStatus(msg);
            showAppletLog(msg);
            repaint();
        }
        if (code != null) {
            applet = (Applet)loader.loadCode(code).newInstance();
            doInit = true;
        } else {
            // serName is not null;
            InputStream is = (InputStream)
                java.security.AccessController.doPrivileged(
                                                            new java.security.PrivilegedAction() {
                                                                public Object run() {
                                                                    return loader.getResourceAsStream(serName);
                                                                }
                                                            });
            ObjectInputStream ois =
                new AppletObjectInputStream(is, loader);
            Object serObject = ois.readObject();
            applet = (Applet) serObject;
            doInit = false; // skip over the first init
        }

        // Determine the JDK level that the applet targets.
        // This is critical for enabling certain backward
        // compatibility switch if an applet is a JDK 1.1
        // applet. [stanley.ho]
        findAppletJDKLevel(applet);

        if (Thread.interrupted()) {
            try {
                status = APPLET_DISPOSE; // APPLET_ERROR?
                applet = null;
                // REMIND: This may not be exactly the right thing: the
                // status is set by the stop button and not necessarily
                // here.
                showAppletStatus("death");
            } finally {
                Thread.currentThread().interrupt(); // resignal interrupt
            }
            return null;
        }
        return applet;
    }
