public void run() 
{
    updateThreadName(null);
    Socket socket = nextSocket();
    while (socket != null) {
        try {
            DataInputStream sockin = new DataInputStream(socket.getInputStream());
            DataOutputStream sockout = new DataOutputStream(socket.getOutputStream());
            // client info - command line arguments and environment
            List remoteArgs = new java.util.ArrayList();
            Properties remoteEnv = new Properties();
            // working directory
            String cwd = null;
            // alias or class name
            String command = null;
            // read everything from the client up to and including the command
            while (command == null) {
                int bytesToRead = sockin.readInt();
                byte chunkType = sockin.readByte();
                byte[] b = new byte[(int) bytesToRead];
                sockin.readFully(b);
                String line = new String(b, "US-ASCII");
                switch(chunkType) {
                    case NGConstants.CHUNKTYPE_ARGUMENT:
                        // command line argument
                        remoteArgs.add(line);
                        break;
                    case NGConstants.CHUNKTYPE_ENVIRONMENT:
                        // parse environment into property
                        int equalsIndex = line.indexOf('=');
                        if (equalsIndex > 0) {
                            remoteEnv.setProperty(line.substring(0, equalsIndex), line.substring(equalsIndex + 1));
                        }
                        String key = line.substring(0, equalsIndex);
                        break;
                    case NGConstants.CHUNKTYPE_COMMAND:
                        // command (alias or classname)
                        command = line;
                        break;
                    case NGConstants.CHUNKTYPE_WORKINGDIRECTORY:
                        // client working directory
                        cwd = line;
                        break;
                    // freakout?
                    default:
                }
            }
            updateThreadName(socket.getInetAddress().getHostAddress() + ": " + command);
            // can't create NGInputStream until we've received a command, because at
            // that point the stream from the client will only include stdin and stdin-eof
            // chunks
            InputStream in = null;
            PrintStream out = null;
            PrintStream err = null;
            PrintStream exit = null;
            try {
                in = new NGInputStream(sockin, sockout, server.out, heartbeatTimeoutMillis);
                out = new PrintStream(new NGOutputStream(sockout, NGConstants.CHUNKTYPE_STDOUT));
                err = new PrintStream(new NGOutputStream(sockout, NGConstants.CHUNKTYPE_STDERR));
                exit = new PrintStream(new NGOutputStream(sockout, NGConstants.CHUNKTYPE_EXIT));
                // ThreadLocal streams for System.in/out/err redirection
                ((ThreadLocalInputStream) System.in).init(in);
                ((ThreadLocalPrintStream) System.out).init(out);
                ((ThreadLocalPrintStream) System.err).init(err);
                try {
                    Alias alias = server.getAliasManager().getAlias(command);
                    Class cmdclass = null;
                    if (alias != null) {
                        cmdclass = alias.getAliasedClass();
                    } else if (server.allowsNailsByClassName()) {
                        cmdclass = Class.forName(command, true, classLoader);
                    } else {
                        cmdclass = server.getDefaultNailClass();
                    }
                    Object[] methodArgs = new Object[1];
                    // will be either main(String[]) or nailMain(NGContext)
                    Method mainMethod = null;
                    String[] cmdlineArgs = (String[]) remoteArgs.toArray(new String[remoteArgs.size()]);
                    // See: NonStaticNail.java
                    boolean isStaticNail = true;
                    Class[] interfaces = cmdclass.getInterfaces();
                    for (int i = 0; i < interfaces.length; i++) {
                        if (interfaces[i].equals(NonStaticNail.class)) {
                            isStaticNail = false;
                            break;
                        }
                    }
                    if (!isStaticNail) {
                        mainMethod = cmdclass.getMethod("nailMain", new Class[] { String[].class });
                        methodArgs[0] = cmdlineArgs;
                    } else {
                        try {
                            mainMethod = cmdclass.getMethod("nailMain", nailMainSignature);
                            NGContext context = new NGContext();
                            context.setArgs(cmdlineArgs);
                            context.in = in;
                            context.out = out;
                            context.err = err;
                            context.setCommand(command);
                            context.setExitStream(exit);
                            context.setNGServer(server);
                            context.setEnv(remoteEnv);
                            context.setInetAddress(socket.getInetAddress());
                            context.setPort(socket.getPort());
                            context.setWorkingDirectory(cwd);
                            methodArgs[0] = context;
                        } catch (NoSuchMethodException toDiscard) {
                            // that's ok - we'll just try main(String[]) next.
                        }
                        if (mainMethod == null) {
                            mainMethod = cmdclass.getMethod("main", mainSignature);
                            methodArgs[0] = cmdlineArgs;
                        }
                    }
                    if (mainMethod != null) {
                        server.nailStarted(cmdclass);
                        NGSecurityManager.setExit(exit);
                        try {
                            if (isStaticNail) {
                                mainMethod.invoke(null, methodArgs);
                            } else {
                                mainMethod.invoke(cmdclass.newInstance(), methodArgs);
                            }
                        } catch (InvocationTargetException ite) {
                            throw (ite.getCause());
                        } catch (InstantiationException e) {
                            throw (e);
                        } catch (IllegalAccessException e) {
                            throw (e);
                        } catch (Throwable t) {
                            throw (t);
                        } finally {
                            server.nailFinished(cmdclass);
                        }
                        exit.println(0);
                    }
                } catch (NGExitException exitEx) {
                    in.close();
                    exit.println(exitEx.getStatus());
                    server.out.println(Thread.currentThread().getName() + " exited with status " + exitEx.getStatus());
                } catch (Throwable t) {
                    in.close();
                    t.printStackTrace();
                    // remote exception constant
                    exit.println(NGConstants.EXIT_EXCEPTION);
                }
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (err != null) {
                    err.close();
                }
                if (exit != null) {
                    exit.close();
                }
                sockout.flush();
                socket.close();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        ((ThreadLocalInputStream) System.in).init(null);
        ((ThreadLocalPrintStream) System.out).init(null);
        ((ThreadLocalPrintStream) System.err).init(null);
        updateThreadName(null);
        sessionPool.give(this);
        socket = nextSocket();
    }
    // server.out.println("Shutdown NGSession " + instanceNumber);
}