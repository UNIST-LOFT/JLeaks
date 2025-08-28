    public File prepareConfiguration() throws MojoExecutionException
    {
        try
        {   
            //work out the configuration based on what is configured in the pom
            File propsFile = new File (target, "fork.props");
            if (propsFile.exists())
                propsFile.delete();   

            propsFile.createNewFile();
            //propsFile.deleteOnExit();

            Properties props = new Properties();


            //web.xml
            if (webXml != null)
                props.put("web.xml", webXml);

            //sort out the context path
            if (contextPath != null)
                props.put("context.path", contextPath);

            //sort out the tmp directory (make it if it doesn't exist)
            if (tmpDirectory != null)
            {
                if (!tmpDirectory.exists())
                    tmpDirectory.mkdirs();
                props.put("tmp.dir", tmpDirectory.getAbsolutePath());
            }

            //sort out base dir of webapp
            if (webAppSourceDirectory == null || !webAppSourceDirectory.exists())
            {
                webAppSourceDirectory = new File (project.getBasedir(), DEFAULT_WEBAPP_SRC);       
                if (!webAppSourceDirectory.exists())
                {
                    //try last resort of making a fake empty dir
                    File target = new File(project.getBuild().getDirectory());
                    webAppSourceDirectory = new File(target, FAKE_WEBAPP);
                    if (!webAppSourceDirectory.exists())
                        webAppSourceDirectory.mkdirs();  
                }
            }
            props.put("base.dir", webAppSourceDirectory.getAbsolutePath());

            //sort out the resource base directories of the webapp
            StringBuilder builder = new StringBuilder();
            props.put("base.first", Boolean.toString(baseAppFirst));

            //web-inf classes
            List<File> classDirs = getClassesDirs();
            StringBuffer strbuff = new StringBuffer();
            for (int i=0; i<classDirs.size(); i++)
            {
                File f = classDirs.get(i);
                strbuff.append(f.getAbsolutePath());
                if (i < classDirs.size()-1)
                    strbuff.append(",");
            }

            if (classesDirectory != null)
            {
                props.put("classes.dir", classesDirectory.getAbsolutePath());
            }
            
            if (useTestScope && testClassesDirectory != null)
            {
                props.put("testClasses.dir", testClassesDirectory.getAbsolutePath());
            }

            //web-inf lib
            List<File> deps = getDependencyFiles();
            strbuff.setLength(0);
            for (int i=0; i<deps.size(); i++)
            {
                File d = deps.get(i);
                strbuff.append(d.getAbsolutePath());
                if (i < deps.size()-1)
                    strbuff.append(",");
            }
            props.put("lib.jars", strbuff.toString());

            //any war files
            List<Artifact> warArtifacts = getWarArtifacts(); 
            for (int i=0; i<warArtifacts.size(); i++)
            {
                strbuff.setLength(0);           
                Artifact a  = warArtifacts.get(i);
                strbuff.append(a.getGroupId()+",");
                strbuff.append(a.getArtifactId()+",");
                strbuff.append(a.getFile().getAbsolutePath());
                props.put("maven.war.artifact."+i, strbuff.toString());
            }
          
            
            //any overlay configuration
            WarPluginInfo warPlugin = new WarPluginInfo(project);
            
            //add in the war plugins default includes and excludes
            props.put("maven.war.includes", toCSV(warPlugin.getDependentMavenWarIncludes()));
            props.put("maven.war.excludes", toCSV(warPlugin.getDependentMavenWarExcludes()));
            
            
            List<OverlayConfig> configs = warPlugin.getMavenWarOverlayConfigs();
            int i=0;
            for (OverlayConfig c:configs)
            {
                props.put("maven.war.overlay."+(i++), c.toString());
            }
            
            props.store(new BufferedOutputStream(new FileOutputStream(propsFile)), "properties for forked webapp");
            return propsFile;
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Prepare webapp configuration", e);
        }
    }
    

    
    
    /**
     * @return
     */
    private List<File> getClassesDirs ()
    {
        List<File> classesDirs = new ArrayList<File>();
        
        //if using the test classes, make sure they are first
        //on the list
        if (useTestScope && (testClassesDirectory != null))
            classesDirs.add(testClassesDirectory);
        
        if (classesDirectory != null)
            classesDirs.add(classesDirectory);
        
        return classesDirs;
    }
  
    
  
    
    /**
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    private List<Artifact> getWarArtifacts()
    throws MalformedURLException, IOException
    {
        List<Artifact> warArtifacts = new ArrayList<Artifact>();
        for ( Iterator<Artifact> iter = project.getArtifacts().iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();  
            
            if (artifact.getType().equals("war"))
                warArtifacts.add(artifact);
        }

        return warArtifacts;
    }
    
    
    
    
    /**
     * @return
     */
    private List<File> getDependencyFiles ()
    {
        List<File> dependencyFiles = new ArrayList<File>();
    
        for ( Iterator<Artifact> iter = project.getArtifacts().iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            
            if (((!Artifact.SCOPE_PROVIDED.equals(artifact.getScope())) && (!Artifact.SCOPE_TEST.equals( artifact.getScope()))) 
                    ||
                (useTestScope && Artifact.SCOPE_TEST.equals( artifact.getScope())))
            {
                dependencyFiles.add(artifact.getFile());
                getLog().debug( "Adding artifact " + artifact.getFile().getName() + " for WEB-INF/lib " );   
            }
        }
        
        return dependencyFiles; 
    }
    
    
    
    
    /**
     * @param artifact
     * @return
     */
    public boolean isPluginArtifact(Artifact artifact)
    {
        if (pluginArtifacts == null || pluginArtifacts.isEmpty())
            return false;
        
        boolean isPluginArtifact = false;
        for (Iterator<Artifact> iter = pluginArtifacts.iterator(); iter.hasNext() && !isPluginArtifact; )
        {
            Artifact pluginArtifact = iter.next();
            if (getLog().isDebugEnabled()) { getLog().debug("Checking "+pluginArtifact);}
            if (pluginArtifact.getGroupId().equals(artifact.getGroupId()) && pluginArtifact.getArtifactId().equals(artifact.getArtifactId()))
                isPluginArtifact = true;
        }
        
        return isPluginArtifact;
    }
    
    
    
    
    /**
     * @return
     * @throws Exception
     */
    private Set<Artifact> getExtraJars()
    throws Exception
    {
        Set<Artifact> extraJars = new HashSet<Artifact>();
  
        
        List l = pluginArtifacts;
        Artifact pluginArtifact = null;

        if (l != null)
        {
            Iterator itor = l.iterator();
            while (itor.hasNext() && pluginArtifact == null)
            {              
                Artifact a = (Artifact)itor.next();
                if (a.getArtifactId().equals(plugin.getArtifactId())) //get the jetty-maven-plugin jar
                {
                    extraJars.add(a);
                }
            }
        }

        return extraJars;
    }

    

    
    /**
     * @throws MojoExecutionException
     */
    public void startJettyRunner() throws MojoExecutionException
    {      
        try
        {
        
            File props = prepareConfiguration();
            
            List<String> cmd = new ArrayList<String>();
            cmd.add(getJavaBin());
            
            if (jvmArgs != null)
            {
                String[] args = jvmArgs.split(" ");
                for (int i=0;args != null && i<args.length;i++)
                {
                    if (args[i] !=null && !"".equals(args[i]))
                        cmd.add(args[i].trim());
                }
            }
            
            String classPath = getClassPath();
            if (classPath != null && classPath.length() > 0)
            {
                cmd.add("-cp");
                cmd.add(classPath);
            }
            cmd.add(Starter.class.getCanonicalName());
            
            if (stopPort > 0 && stopKey != null)
            {
                cmd.add("--stop-port");
                cmd.add(Integer.toString(stopPort));
                cmd.add("--stop-key");
                cmd.add(stopKey);
            }
            if (jettyXml != null)
            {
                cmd.add("--jetty-xml");
                cmd.add(jettyXml);
            }
        
            if (contextXml != null)
            {
                cmd.add("--context-xml");
                cmd.add(contextXml);
            }
            
            cmd.add("--props");
            cmd.add(props.getAbsolutePath());
            
            String token = createToken();
            cmd.add("--token");
            cmd.add(token);
            
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.directory(project.getBasedir());
            
            if (PluginLog.getLog().isDebugEnabled())
                PluginLog.getLog().debug(Arrays.toString(cmd.toArray()));
            
            forkedProcess = builder.start();
            PluginLog.getLog().info("Forked process starting");

            if (waitForChild)
            {
                startPump("STDOUT",forkedProcess.getInputStream());
                startPump("STDERR",forkedProcess.getErrorStream());
                int exitcode = forkedProcess.waitFor();            
                PluginLog.getLog().info("Forked execution exit: "+exitcode);
            }
            else
            {   //we're not going to be reading the stderr as we're not waiting for the child to finish
                forkedProcess.getErrorStream().close();

                //wait for the child to be ready before terminating.
                //child indicates it has finished starting by printing on stdout the token passed to it
                try
                {
                    LineNumberReader reader = new LineNumberReader(new InputStreamReader(forkedProcess.getInputStream()));
                    String line = "";
                    int attempts = maxStartupLines; //max lines we'll read trying to get token
                    while (attempts>0 && line != null)
                    {
                        --attempts;
                        line = reader.readLine();
                        if (line != null && line.startsWith(token))
                            break;
                    }

                    reader.close();

                    if (line != null && line.trim().equals(token))
                        PluginLog.getLog().info("Forked process started.");
                    else
                    {
                        String err = (line == null?"":(line.startsWith(token)?line.substring(token.length()):line));
                        PluginLog.getLog().info("Forked process startup errors"+(!"".equals(err)?", received: "+err:""));
                    }
                }
                catch (Exception e)
                {
                    throw new MojoExecutionException ("Problem determining if forked process is ready: "+e.getMessage());
                }
            }
        }
        catch (InterruptedException ex)
        {
            if (forkedProcess != null && waitForChild)
                forkedProcess.destroy();
            
            throw new MojoExecutionException("Failed to start Jetty within time limit");
        }
        catch (Exception ex)
        {
            if (forkedProcess != null && waitForChild)
                forkedProcess.destroy();
            
            throw new MojoExecutionException("Failed to create Jetty process", ex);
        }
    }
