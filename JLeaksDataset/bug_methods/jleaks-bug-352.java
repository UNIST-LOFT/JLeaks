    public static void main(String[] ava) throws IOException {
        List<String> av = new ArrayList<>(Arrays.asList(ava));

        boolean doPack   = true;
        boolean doUnpack = false;
        boolean doRepack = false;
        boolean doZip = true;
        String logFile = null;
        String verboseProp = Utils.DEBUG_VERBOSE;

        {
            // Non-standard, undocumented "--unpack" switch enables unpack mode.
            String arg0 = av.isEmpty() ? "" : av.get(0);
            switch (arg0) {
                case "--pack":
                av.remove(0);
                    break;
                case "--unpack":
                av.remove(0);
                doPack = false;
                doUnpack = true;
                    break;
            }
        }

        // Collect engine properties here:
        Map<String,String> engProps = new HashMap<>();
        engProps.put(verboseProp, System.getProperty(verboseProp));

        String optionMap;
        String[] propTable;
        if (doPack) {
            optionMap = PACK200_OPTION_MAP;
            propTable = PACK200_PROPERTY_TO_OPTION;
        } else {
            optionMap = UNPACK200_OPTION_MAP;
            propTable = UNPACK200_PROPERTY_TO_OPTION;
        }

        // Collect argument properties here:
        Map<String,String> avProps = new HashMap<>();
        try {
            for (;;) {
                String state = parseCommandOptions(av, optionMap, avProps);
                // Translate command line options to Pack200 properties:
            eachOpt:
                for (Iterator<String> opti = avProps.keySet().iterator();
                     opti.hasNext(); ) {
                    String opt = opti.next();
                    String prop = null;
                    for (int i = 0; i < propTable.length; i += 2) {
                        if (opt.equals(propTable[1+i])) {
                            prop = propTable[0+i];
                            break;
                        }
                    }
                    if (prop != null) {
                        String val = avProps.get(opt);
                        opti.remove();  // remove opt from avProps
                        if (!prop.endsWith(".")) {
                            // Normal string or boolean.
                            if (!(opt.equals("--verbose")
                                  || opt.endsWith("="))) {
                                // Normal boolean; convert to T/F.
                                boolean flag = (val != null);
                                if (opt.startsWith("--no-"))
                                    flag = !flag;
                                val = flag? "true": "false";
                            }
                            engProps.put(prop, val);
                        } else if (prop.contains(".attribute.")) {
                            for (String val1 : val.split("\0")) {
                                String[] val2 = val1.split("=", 2);
                                engProps.put(prop+val2[0], val2[1]);
                            }
                        } else {
                            // Collection property: pack.pass.file.cli.NNN
                            int idx = 1;
                            for (String val1 : val.split("\0")) {
                                String prop1;
                                do {
                                    prop1 = prop+"cli."+(idx++);
                                } while (engProps.containsKey(prop1));
                                engProps.put(prop1, val1);
                            }
                        }
                    }
                }

                // See if there is any other action to take.
                if ("--config-file=".equals(state)) {
                    String propFile = av.remove(0);
                    InputStream propIn = new FileInputStream(propFile);
                    Properties fileProps = new Properties();
                    fileProps.load(new BufferedInputStream(propIn));
                    if (engProps.get(verboseProp) != null)
                        fileProps.list(System.out);
                    propIn.close();
                    for (Map.Entry<Object,Object> me : fileProps.entrySet()) {
                        engProps.put((String) me.getKey(), (String) me.getValue());
                    }
                } else if ("--version".equals(state)) {
                        System.out.println(MessageFormat.format(RESOURCE.getString(DriverResource.VERSION), Driver.class.getName(), "1.31, 07/05/05"));
                    return;
                } else if ("--help".equals(state)) {
                    printUsage(doPack, true, System.out);
                    System.exit(1);
                    return;
                } else {
                    break;
                }
            }
        } catch (IllegalArgumentException ee) {
                System.err.println(MessageFormat.format(RESOURCE.getString(DriverResource.BAD_ARGUMENT), ee));
            printUsage(doPack, false, System.err);
            System.exit(2);
            return;
        }

        // Deal with remaining non-engine properties:
        for (String opt : avProps.keySet()) {
            String val = avProps.get(opt);
            switch (opt) {
                case "--repack":
                    doRepack = true;
                    break;
                case "--no-gzip":
                    doZip = (val == null);
                    break;
                case "--log-file=":
                    logFile = val;
                    break;
                default:
                    throw new InternalError(MessageFormat.format(
                            RESOURCE.getString(DriverResource.BAD_OPTION),
                            opt, avProps.get(opt)));
            }
        }

        if (logFile != null && !logFile.equals("")) {
            if (logFile.equals("-")) {
                System.setErr(System.out);
            } else {
                OutputStream log = new FileOutputStream(logFile);
                //log = new BufferedOutputStream(out);
                System.setErr(new PrintStream(log));
            }
        }

        boolean verbose = (engProps.get(verboseProp) != null);

        String packfile = "";
        if (!av.isEmpty())
            packfile = av.remove(0);

        String jarfile = "";
        if (!av.isEmpty())
            jarfile = av.remove(0);

        String newfile = "";  // output JAR file if --repack
        String bakfile = "";  // temporary backup of input JAR
        String tmpfile = "";  // temporary file to be deleted
        if (doRepack) {
            // The first argument is the target JAR file.
            // (Note:  *.pac is nonstandard, but may be necessary
            // if a host OS truncates file extensions.)
            if (packfile.toLowerCase().endsWith(".pack") ||
                packfile.toLowerCase().endsWith(".pac") ||
                packfile.toLowerCase().endsWith(".gz")) {
                System.err.println(MessageFormat.format(
                        RESOURCE.getString(DriverResource.BAD_REPACK_OUTPUT),
                        packfile));
                printUsage(doPack, false, System.err);
                System.exit(2);
            }
            newfile = packfile;
            // The optional second argument is the source JAR file.
            if (jarfile.equals("")) {
                // If only one file is given, it is the only JAR.
                // It serves as both input and output.
                jarfile = newfile;
            }
            tmpfile = createTempFile(newfile, ".pack").getPath();
            packfile = tmpfile;
            doZip = false;  // no need to zip the temporary file
        }

        if (!av.isEmpty()
            // Accept jarfiles ending with .jar or .zip.
            // Accept jarfile of "-" (stdout), but only if unpacking.
            || !(jarfile.toLowerCase().endsWith(".jar")
                 || jarfile.toLowerCase().endsWith(".zip")
                 || (jarfile.equals("-") && !doPack))) {
            printUsage(doPack, false, System.err);
            System.exit(2);
            return;
        }

        if (doRepack)
            doPack = doUnpack = true;
        else if (doPack)
            doUnpack = false;

        Pack200.Packer jpack = Pack200.newPacker();
        Pack200.Unpacker junpack = Pack200.newUnpacker();

        jpack.properties().putAll(engProps);
        junpack.properties().putAll(engProps);
        if (doRepack && newfile.equals(jarfile)) {
            String zipc = getZipComment(jarfile);
            if (verbose && zipc.length() > 0)
                System.out.println(MessageFormat.format(RESOURCE.getString(DriverResource.DETECTED_ZIP_COMMENT), zipc));
            if (zipc.indexOf(Utils.PACK_ZIP_ARCHIVE_MARKER_COMMENT) >= 0) {
                    System.out.println(MessageFormat.format(RESOURCE.getString(DriverResource.SKIP_FOR_REPACKED), jarfile));
                        doPack = false;
                        doUnpack = false;
                        doRepack = false;
            }
        }

        try {

            if (doPack) {
                // Mode = Pack.
                JarFile in = new JarFile(new File(jarfile));
                OutputStream out;
                // Packfile must be -, *.gz, *.pack, or *.pac.
                if (packfile.equals("-")) {
                    out = System.out;
                    // Send warnings, etc., to stderr instead of stdout.
                    System.setOut(System.err);
                } else if (doZip) {
                    if (!packfile.endsWith(".gz")) {
                    System.err.println(MessageFormat.format(RESOURCE.getString(DriverResource.WRITE_PACK_FILE), packfile));
                        printUsage(doPack, false, System.err);
                        System.exit(2);
                    }
                    out = new FileOutputStream(packfile);
                    out = new BufferedOutputStream(out);
                    out = new GZIPOutputStream(out);
                } else {
                    if (!packfile.toLowerCase().endsWith(".pack") &&
                            !packfile.toLowerCase().endsWith(".pac")) {
                        System.err.println(MessageFormat.format(RESOURCE.getString(DriverResource.WIRTE_PACKGZ_FILE),packfile));
                        printUsage(doPack, false, System.err);
                        System.exit(2);
                    }
                    out = new FileOutputStream(packfile);
                    out = new BufferedOutputStream(out);
                }
                jpack.pack(in, out);
                //in.close();  // p200 closes in but not out
                out.close();
            }

            if (doRepack && newfile.equals(jarfile)) {
                // If the source and destination are the same,
                // we will move the input JAR aside while regenerating it.
                // This allows us to restore it if something goes wrong.
                File bakf = createTempFile(jarfile, ".bak");
                // On Windows target must be deleted see 4017593
                bakf.delete();
                boolean okBackup = new File(jarfile).renameTo(bakf);
                if (!okBackup) {
                        throw new Error(MessageFormat.format(RESOURCE.getString(DriverResource.SKIP_FOR_MOVE_FAILED),bakfile));
                } else {
                    // Open jarfile recovery bracket.
                    bakfile = bakf.getPath();
                }
            }

            if (doUnpack) {
                // Mode = Unpack.
                InputStream in;
                if (packfile.equals("-"))
                    in = System.in;
                else
                    in = new FileInputStream(new File(packfile));
                BufferedInputStream inBuf = new BufferedInputStream(in);
                in = inBuf;
                if (Utils.isGZIPMagic(Utils.readMagic(inBuf))) {
                    in = new GZIPInputStream(in);
                }
                String outfile = newfile.equals("")? jarfile: newfile;
                OutputStream fileOut;
                if (outfile.equals("-"))
                    fileOut = System.out;
                else
                    fileOut = new FileOutputStream(outfile);
                fileOut = new BufferedOutputStream(fileOut);
                JarOutputStream out = new JarOutputStream(fileOut);
                junpack.unpack(in, out);
                //in.close();  // p200 closes in but not out
                out.close();
                // At this point, we have a good jarfile (or newfile, if -r)
            }

            if (!bakfile.equals("")) {
                        // On success, abort jarfile recovery bracket.
                        new File(bakfile).delete();
                        bakfile = "";
            }

        } finally {
            // Close jarfile recovery bracket.
            if (!bakfile.equals("")) {
                File jarFile = new File(jarfile);
                jarFile.delete(); // Win32 requires this, see above
                new File(bakfile).renameTo(jarFile);
            }
            // In all cases, delete temporary *.pack.
            if (!tmpfile.equals(""))
                new File(tmpfile).delete();
        }
    }
