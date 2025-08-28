public UserInfo getUserInfo (String userName){
    try {
        // work out the configuration based on what is configured in the pom
        File propsFile = new File(target, "fork.props");
        if (propsFile.exists())
            propsFile.delete();
        propsFile.createNewFile();
        // propsFile.deleteOnExit();
        Properties props = new Properties();
        // web.xml
        if (webXml != null)
            props.put("web.xml", webXml);
        // sort out the context path
        if (contextPath != null)
            props.put("context.path", contextPath);
        // sort out the tmp directory (make it if it doesn't exist)
        if (tmpDirectory != null) {
            if (!tmpDirectory.exists())
                tmpDirectory.mkdirs();
            props.put("tmp.dir", tmpDirectory.getAbsolutePath());
        }
        // sort out base dir of webapp
        if (webAppSourceDirectory == null || !webAppSourceDirectory.exists()) {
            webAppSourceDirectory = new File(project.getBasedir(), DEFAULT_WEBAPP_SRC);
            if (!webAppSourceDirectory.exists()) {
                // try last resort of making a fake empty dir
                File target = new File(project.getBuild().getDirectory());
                webAppSourceDirectory = new File(target, FAKE_WEBAPP);
                if (!webAppSourceDirectory.exists())
                    webAppSourceDirectory.mkdirs();
            }
        }
        props.put("base.dir", webAppSourceDirectory.getAbsolutePath());
        // sort out the resource base directories of the webapp
        StringBuilder builder = new StringBuilder();
        props.put("base.first", Boolean.toString(baseAppFirst));
        // web-inf classes
        List<File> classDirs = getClassesDirs();
        StringBuffer strbuff = new StringBuffer();
        for (int i = 0; i < classDirs.size(); i++) {
            File f = classDirs.get(i);
            strbuff.append(f.getAbsolutePath());
            if (i < classDirs.size() - 1)
                strbuff.append(",");
        }
        if (classesDirectory != null) {
            props.put("classes.dir", classesDirectory.getAbsolutePath());
        }
        if (useTestScope && testClassesDirectory != null) {
            props.put("testClasses.dir", testClassesDirectory.getAbsolutePath());
        }
        // web-inf lib
        List<File> deps = getDependencyFiles();
        strbuff.setLength(0);
        for (int i = 0; i < deps.size(); i++) {
            File d = deps.get(i);
            strbuff.append(d.getAbsolutePath());
            if (i < deps.size() - 1)
                strbuff.append(",");
        }
        props.put("lib.jars", strbuff.toString());
        // any war files
        List<Artifact> warArtifacts = getWarArtifacts();
        for (int i = 0; i < warArtifacts.size(); i++) {
            strbuff.setLength(0);
            Artifact a = warArtifacts.get(i);
            strbuff.append(a.getGroupId() + ",");
            strbuff.append(a.getArtifactId() + ",");
            strbuff.append(a.getFile().getAbsolutePath());
            props.put("maven.war.artifact." + i, strbuff.toString());
        }
        // any overlay configuration
        WarPluginInfo warPlugin = new WarPluginInfo(project);
        // add in the war plugins default includes and excludes
        props.put("maven.war.includes", toCSV(warPlugin.getDependentMavenWarIncludes()));
        props.put("maven.war.excludes", toCSV(warPlugin.getDependentMavenWarExcludes()));
        List<OverlayConfig> configs = warPlugin.getMavenWarOverlayConfigs();
        int i = 0;
        for (OverlayConfig c : configs) {
            props.put("maven.war.overlay." + (i++), c.toString());
        }
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(propsFile))) {
            props.store(out, "properties for forked webapp");
        }
        return propsFile;
    } catch (Exception e) {
        throw new MojoExecutionException("Prepare webapp configuration", e);
    }
}