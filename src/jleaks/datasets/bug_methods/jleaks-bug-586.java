    public void launch(final SlaveComputer computer, final TaskListener listener) throws IOException, InterruptedException {
        try {
            final PrintStream logger = listener.getLogger();
            final String name = determineHost(computer);

            logger.println(Messages.ManagedWindowsServiceLauncher_ConnectingTo(name));

            InetAddress host = InetAddress.getByName(name);

            /*
                Somehow this didn't work for me, so I'm disabling it.
             */
            // ping check
//            if (!host.isReachable(3000)) {
//                logger.println("Failed to ping "+name+". Is this a valid reachable host name?");
//                // continue anyway, just in case it's just ICMP that's getting filtered
//            }

            try {
                Socket s = new Socket();
                s.connect(new InetSocketAddress(host,135),5000);
                s.close();
            } catch (IOException e) {
                logger.println("Failed to connect to port 135 of "+name+". Is Windows firewall blocking this port? Or did you disable DCOM service?");
                // again, let it continue.
            }

            JIDefaultAuthInfoImpl auth = createAuth();
            JISession session = JISession.createSession(auth);
            session.setGlobalSocketTimeout(60000);
            SWbemServices services = WMI.connect(session, name);


            String path = computer.getNode().getRemoteFS();
            if (path.indexOf(':')==-1)   throw new IOException("Remote file system root path of the slave needs to be absolute: "+path);
            SmbFile remoteRoot = new SmbFile("smb://" + name + "/" + path.replace('\\', '/').replace(':', '$')+"/",createSmbAuth());

            if(!remoteRoot.exists())
                remoteRoot.mkdirs();

            try {// does Java exist?
                logger.println("Checking if Java exists");
                WindowsRemoteProcessLauncher wrpl = new WindowsRemoteProcessLauncher(name,auth);
                Process proc = wrpl.launch("java -fullversion","c:\\");
                proc.getOutputStream().close();
                IOUtils.copy(proc.getInputStream(),logger);
                proc.getInputStream().close();
                int exitCode = proc.waitFor();
                if (exitCode==1) {// we'll get this error code if Java is not found
                    logger.println("No Java found. Downloading JDK");
                    JDKInstaller jdki = new JDKInstaller("jdk-6u16-oth-JPR@CDS-CDS_Developer",true);
                    URL jdk = jdki.locate(listener, Platform.WINDOWS, CPU.i386);

                    listener.getLogger().println("Installing JDK");
                    copyStreamAndClose(jdk.openStream(), new SmbFile(remoteRoot, "jdk.exe").getOutputStream());

                    String javaDir = path + "\\jdk"; // this is where we install Java to

                    WindowsRemoteFileSystem fs = new WindowsRemoteFileSystem(name, createSmbAuth());
                    fs.mkdirs(javaDir);
                    
                    jdki.install(new WindowsRemoteLauncher(listener,wrpl), Platform.WINDOWS,
                            fs, listener, javaDir ,path+"\\jdk.exe");
                }
            } catch (Exception e) {
                e.printStackTrace(listener.error("Failed to prepare Java"));
            }

// this just doesn't work --- trying to obtain the type or check the existence of smb://server/C$/ results in "access denied"    
//            {// check if the administrative share exists
//                String fullpath = remoteRoot.getPath();
//                int idx = fullpath.indexOf("$/");
//                if (idx>=0) {// this must be true but be defensive since all we are trying to do here is a friendlier error check
//                    boolean exists;
//                    try {
//                        // SmbFile.exists() doesn't work on a share
//                        new SmbFile(fullpath.substring(0, idx + 2)).getType();
//                        exists = true;
//                    } catch (SmbException e) {
//                        // on Windows XP that I was using for the test, if the share doesn't exist I get this error
//                        // a thread in jcifs library ML confirms this, too:
//                        // http://old.nabble.com/"The-network-name-cannot-be-found"-after-30-seconds-td18859163.html
//                        if (e.getNtStatus()== NtStatus.NT_STATUS_BAD_NETWORK_NAME)
//                            exists = false;
//                        else
//                            throw e;
//                    }
//                    if (!exists) {
//                        logger.println(name +" appears to be missing the administrative share "+fullpath.substring(idx-1,idx+1)/*C$*/);
//                        return;
//                    }
//                }
//            }

            String id = WindowsSlaveInstaller.generateServiceId(path);
            Win32Service slaveService = services.getService(id);
            if(slaveService==null) {
                logger.println(Messages.ManagedWindowsServiceLauncher_InstallingSlaveService());
                if(!DotNet.isInstalled(2,0, name, auth)) {
                    // abort the launch
                    logger.println(Messages.ManagedWindowsServiceLauncher_DotNetRequired());
                    return;
                }

                // copy exe
                logger.println(Messages.ManagedWindowsServiceLauncher_CopyingSlaveExe());
                copyStreamAndClose(getClass().getResource("/windows-service/jenkins.exe").openStream(), new SmbFile(remoteRoot,"jenkins-slave.exe").getOutputStream());

                copySlaveJar(logger, remoteRoot);

                // copy jenkins-slave.xml
                String xml = createAndCopyJenkinsSlaveXml(id, logger, remoteRoot);

                // install it as a service
                logger.println(Messages.ManagedWindowsServiceLauncher_RegisteringService());
                Document dom = new SAXReader().read(new StringReader(xml));
                Win32Service svc = services.Get("Win32_Service").cast(Win32Service.class);
                int r;
                AccountInfo logOn = getLogOn();
                if (logOn == null) {
                    r = svc.Create(
                        id,
                        dom.selectSingleNode("/service/name").getText()+" at "+path,
                        path+"\\jenkins-slave.exe",
                        Win32OwnProcess, 0, "Manual", true);
                } else {
                    r = svc.Create(
                        id,
                        dom.selectSingleNode("/service/name").getText()+" at "+path,
                        path+"\\jenkins-slave.exe",
                        Win32OwnProcess,
                        0,
                        "Manual",
                        false, // When using a different user, it isn't possible to interact
                        logOn.userName,
                        Secret.toString(logOn.password),
                        null, null, null);

                }
                if(r!=0) {
                    listener.error("Failed to create a service: "+svc.getErrorMessage(r));
                    return;
                }
                slaveService = services.getService(id);
            } else {
                createAndCopyJenkinsSlaveXml(id, logger, remoteRoot);
                copySlaveJar(logger, remoteRoot);
            }

            logger.println(Messages.ManagedWindowsServiceLauncher_StartingService());
            slaveService.start();

            // wait until we see the port.txt, but don't do so forever
            logger.println(Messages.ManagedWindowsServiceLauncher_WaitingForService());
            SmbFile portFile = new SmbFile(remoteRoot, "port.txt");
            for( int i=0; !portFile.exists(); i++ ) {
                if(i>=30) {
                    listener.error(Messages.ManagedWindowsServiceLauncher_ServiceDidntRespond());
                    return;
                }
                Thread.sleep(1000);
            }
            int p = readSmbFile(portFile);

            // connect
            logger.println(Messages.ManagedWindowsServiceLauncher_ConnectingToPort(p));
            final Socket s = new Socket(name,p);

            // ready
            computer.setChannel(new BufferedInputStream(new SocketInputStream(s)),
                new BufferedOutputStream(new SocketOutputStream(s)),
                listener.getLogger(),new Listener() {
                    @Override
                    public void onClosed(Channel channel, IOException cause) {
                        afterDisconnect(computer,listener);
                    }
                });
            //destroy session to free the socket	
            JISession.destroySession(session);
        } catch (SmbException e) {
            e.printStackTrace(listener.error(e.getMessage()));
        } catch (JIException e) {
            if(e.getErrorCode()==5)
                // access denied error
                e.printStackTrace(listener.error(Messages.ManagedWindowsServiceLauncher_AccessDenied()));
            else
                e.printStackTrace(listener.error(e.getMessage()));
        } catch (DocumentException e) {
            e.printStackTrace(listener.error(e.getMessage()));
        }
    }
