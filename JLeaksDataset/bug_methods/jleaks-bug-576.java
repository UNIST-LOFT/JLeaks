    public void install(Launcher launcher, Platform p, FileSystem fs, TaskListener log, String expectedLocation, String jdkBundle) throws IOException, InterruptedException {
        PrintStream out = log.getLogger();

        out.println("Installing "+ jdkBundle);
        switch (p) {
        case LINUX:
        case SOLARIS:
            // JDK on Unix up to 6 was distributed as shell script installer, but in JDK7 it switched to a plain tgz.
            // so check if the file is gzipped, and if so, treat it accordingly
            byte[] header = new byte[2];
            {
                DataInputStream in = new DataInputStream(fs.read(jdkBundle));
                in.readFully(header);
                in.close();
            }

            ProcStarter starter;
            if (header[0]==0x1F && header[1]==(byte)0x8B) {// gzip
                starter = launcher.launch().cmds("tar", "xvzf", jdkBundle);
            } else {
                fs.chmod(jdkBundle,0755);
                starter = launcher.launch().cmds(jdkBundle, "-noregister");
            }

            int exit = starter
                    .stdin(new ByteArrayInputStream("yes".getBytes())).stdout(out)
                    .pwd(new FilePath(launcher.getChannel(), expectedLocation)).join();
            if (exit != 0)
                throw new AbortException(Messages.JDKInstaller_FailedToInstallJDK(exit));

            // JDK creates its own sub-directory, so pull them up
            List<String> paths = fs.listSubDirectories(expectedLocation);
            for (Iterator<String> itr = paths.iterator(); itr.hasNext();) {
                String s =  itr.next();
                if (!s.matches("j(2s)?dk.*"))
                    itr.remove();
            }
            if(paths.size()!=1)
                throw new AbortException("Failed to find the extracted JDKs: "+paths);

            // remove the intermediate directory
            fs.pullUp(expectedLocation+'/'+paths.get(0),expectedLocation);
            break;
        case WINDOWS:
            /*
                Windows silent installation is full of bad know-how.

                On Windows, command line argument to a process at the OS level is a single string,
                not a string array like POSIX. When we pass arguments as string array, JRE eventually
                turn it into a single string with adding quotes to "the right place". Unfortunately,
                with the strange argument layout of InstallShield (like /v/qn" INSTALLDIR=foobar"),
                it appears that the escaping done by JRE gets in the way, and prevents the installation.
                Presumably because of this, my attempt to use /q/vn" INSTALLDIR=foo" didn't work with JDK5.

                I tried to locate exactly how InstallShield parses the arguments (and why it uses
                awkward option like /qn, but couldn't find any. Instead, experiments revealed that
                "/q/vn ARG ARG ARG" works just as well. This is presumably due to the Visual C++ runtime library
                (which does single string -> string array conversion to invoke the main method in most Win32 process),
                and this consistently worked on JDK5 and JDK4.

                Some of the official documentations are available at
                - http://java.sun.com/j2se/1.5.0/sdksilent.html
                - http://java.sun.com/j2se/1.4.2/docs/guide/plugin/developer_guide/silent.html
             */
            String logFile = jdkBundle+".install.log";

            expectedLocation = expectedLocation.trim();
            if (expectedLocation.endsWith("\\")) {
                // Prevent a trailing slash from escaping quotes
                expectedLocation = expectedLocation.substring(0, expectedLocation.length() - 1);
            }
            ArgumentListBuilder args = new ArgumentListBuilder();
            assert (new File(expectedLocation).exists()) : expectedLocation
                    + " must exist, otherwise /L will cause the installer to fail with error 1622";
            if (isJava15() || isJava14()) {
                // Installer uses InstallShield.
                args.add("CMD.EXE", "/C");

                // CMD.EXE /C must be followed by a single parameter (do not split it!)
                args.add(jdkBundle + " /s /v\"/qn REBOOT=ReallySuppress INSTALLDIR=\\\""
                        + expectedLocation + "\\\" /L \\\"" + expectedLocation
                        + "\\jdk.exe.install.log\\\"\"");
            } else {
                // Installed uses Windows Installer (MSI)
                args.add(jdkBundle, "/s");

                // Create a private JRE by omitting "PublicjreFeature"
                // @see http://docs.oracle.com/javase/7/docs/webnotes/install/windows/jdk-installation-windows.html#jdk-silent-installation
                args.add("ADDLOCAL=\"ToolsFeature\"");

                args.add("REBOOT=ReallySuppress", "INSTALLDIR=" + expectedLocation,
                        "/L \\\"" + expectedLocation + "\\jdk.exe.install.log\\\"");
            }
            int r = launcher.launch().cmds(args).stdout(out)
                    .pwd(new FilePath(launcher.getChannel(), expectedLocation)).join();
            if (r != 0) {
                out.println(Messages.JDKInstaller_FailedToInstallJDK(r));
                // log file is in UTF-16
                InputStreamReader in = new InputStreamReader(fs.read(logFile), "UTF-16");
                try {
                    IOUtils.copy(in,new OutputStreamWriter(out));
                } finally {
                    in.close();
                }
                throw new AbortException();
            }

            fs.delete(logFile);

            break;
        }
    }
