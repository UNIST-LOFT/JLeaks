    public InputStream createGraph(String command, File workDir) throws IOException, RrdException {
        InputStream tempIn;
        String[] commandArray = StringUtils.createCommandArray(command, '@');
        Process process;
        try {
             process = Runtime.getRuntime().exec(commandArray, null, workDir);
        } catch (IOException e) {
            IOException newE = new IOException("IOException thrown while executing command '" + command + "' in " + workDir.getAbsolutePath() + ": " + e);
            newE.initCause(e);
            throw newE;
        }

        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
        BufferedInputStream in = new BufferedInputStream(process.getInputStream());

        StreamUtils.streamToStream(in, tempOut);

        in.close();
        tempOut.close();

        BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line = err.readLine();
        StringBuffer buffer = new StringBuffer();

        while (line != null) {
            buffer.append(line);
            line = err.readLine();
        }

        if (buffer.length() > 0) {
            throw new RrdException(buffer.toString());
        }

        byte[] byteArray = tempOut.toByteArray();
        tempIn = new ByteArrayInputStream(byteArray);
        return tempIn;
    }

    /**
     * No stats are kept for this implementation.
     */
    public String getStats() {
        return "";
    }
    
    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    // These offsets work perfectly for ranger@ with rrdtool 1.2.23 and Firefox
    public int getGraphLeftOffset() {
        return 65;
    }
    
    public int getGraphRightOffset() {
        return -30;
    }

    public int getGraphTopOffsetWithText() {
        return -75;
    }

    public String getDefaultFileExtension() {
        return ".rrd";
    }
    
    public RrdGraphDetails createGraphReturnDetails(String command, File workDir) throws IOException, org.opennms.netmgt.rrd.RrdException {
        // Creating Temp PNG File
        File pngFile = File.createTempFile("opennms.rrdtool.", ".png");
        command = command.replaceFirst("graph - ", "graph " + pngFile.getAbsolutePath() + " ");

        int width;
        int height;
        String[] printLines;
        InputStream pngStream;

        try {
            // Executing RRD Command
            InputStream is = createGraph(command, workDir);

            // Processing Command Output
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String s[] = reader.readLine().split("x");
            width = Integer.parseInt(s[0]);
            height = Integer.parseInt(s[1]);
            String line = null;
            List<String> printLinesList = new ArrayList<String>();
            while ((line = reader.readLine()) != null)
                printLinesList.add(line);
            printLines = new String[printLinesList.size()];
            printLinesList.toArray(printLines);

            // Creating PNG InputStream
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(pngFile));
            ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
            StreamUtils.streamToStream(in, tempOut);
            in.close();
            tempOut.close();
            byte[] byteArray = tempOut.toByteArray();
            pngStream = new ByteArrayInputStream(byteArray);
        } catch (Exception e) {
            throw new RrdException("Can't execute command " + command, e);
        } finally {
            pngFile.delete();
        }

        // Creating Graph Details
        RrdGraphDetails details = new JniGraphDetails(width, height, printLines, pngStream);
        return details;
    }
