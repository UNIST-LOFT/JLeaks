public static void updateWidgetSet(final String widgetset){
    boolean changed = false;
    Map<String, URL> availableWidgetSets = ClassPathExplorer.getAvailableWidgetSets();
    String widgetsetFileName = widgetset.replace(".", "/") + ".gwt.xml";
    URL sourceUrl = availableWidgetSets.get(widgetset);
    if (sourceUrl == null) {
        // find first/default source directory
        sourceUrl = ClassPathExplorer.getWidgetsetSourceDirectory(widgetsetFileName);
    }
    File widgetsetFile = new File(new File(sourceUrl.toURI()), widgetsetFileName);
    if (!widgetsetFile.exists()) {
        // create empty gwt module file
        File parent = widgetsetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("Could not create directory for the widgetset: " + parent.getPath());
            }
        }
        widgetsetFile.createNewFile();
        try (PrintStream printStream = new PrintStream(new FileOutputStream(widgetsetFile))) {
            printStream.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<!DOCTYPE module PUBLIC \"-//Google Inc.//DTD Google Web Toolkit 2.5.1//EN\" \"http://google-web-toolkit.googlecode.com/svn/tags/2.5.1/distro-source/core/src/gwt-module.dtd\">\n");
            printStream.print("<module>\n");
            printStream.print("    <!--\n" + "     Uncomment the following to compile the widgetset for one browser only.\n\n" + "     Multiple browsers can be specified as a comma separated list. The\n" + "     supported user agents at the moment of writing were:\n" + "     ie8,ie9,gecko1_8,safari,opera\n\n" + "     The value gecko1_8 is used for Firefox and safari is used for webkit\n" + "     based browsers including Google Chrome.\n" + "    -->\n" + "    <!-- <set-property name=\"user.agent\" value=\"safari\"/> -->\n\n" + "    <!--\n" + "    To enable SuperDevMode, uncomment this line.\n\n" + "    See https://vaadin.com/wiki/-/wiki/Main/Using%20SuperDevMode for more\n" + "    information and instructions.\n" + "    -->\n" + "    <!-- <set-configuration-property name=\"devModeRedirectEnabled\" value=\"true\" /> -->\n\n");
            printStream.print("\n</module>\n");
        }
        changed = true;
    }
    String content = readFile(widgetsetFile);
    if (isEditable(content)) {
        String originalContent = content;
        Collection<String> oldInheritedWidgetsets = getCurrentInheritedWidgetsets(content);
        // add widgetsets that do not exist
        Iterator<String> i = availableWidgetSets.keySet().iterator();
        while (i.hasNext()) {
            String ws = i.next();
            if (ws.equals(widgetset)) {
                // do not inherit the module itself
                continue;
            }
            if (!oldInheritedWidgetsets.contains(ws)) {
                content = addWidgetSet(ws, content);
            }
        }
        for (String ws : oldInheritedWidgetsets) {
            if (!availableWidgetSets.containsKey(ws)) {
                // widgetset not available in classpath
                content = removeWidgetSet(ws, content);
            }
        }
        changed = changed || !content.equals(originalContent);
        if (changed) {
            commitChanges(widgetsetFile, content);
        }
    } else {
        System.out.println("Widgetset is manually edited. Skipping updates.");
    }
}