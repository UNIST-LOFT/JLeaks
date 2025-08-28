    public synchronized boolean convert(String argv[]){
        List<String> v = new ArrayList<>(2);
        File outputFile = null;
        boolean createOutputFile = false;

        // Parse arguments
        for (int i = 0; i < argv.length; i++) {
            if (argv[i].equals("-encoding")) {
                if ((i + 1) < argv.length){
                    encodingString = argv[++i];
                } else {
                    error(getMsg("err.bad.arg"));
                    usage();
                    return false;
                }
            } else if (argv[i].equals("-reverse")){
                reverse = true;
            } else {
                if (v.size() > 1) {
                    usage();
                    return false;
                }
                v.add(argv[i]);
            }
        }
        if (encodingString == null)
           defaultEncoding = Charset.defaultCharset().name();

        char[] lineBreak = System.getProperty("line.separator").toCharArray();
        try {
            initializeConverter();

            if (v.size() == 1)
                inputFileName = v.get(0);

            if (v.size() == 2) {
                inputFileName = v.get(0);
                outputFileName = v.get(1);
                createOutputFile = true;
            }

            if (createOutputFile) {
                outputFile = new File(outputFileName);
                    if (outputFile.exists() && !outputFile.canWrite()) {
                        throw new Exception(formatMsg("err.cannot.write", outputFileName));
                    }
            }

            if (reverse){
                BufferedReader reader = getA2NInput(inputFileName);
                Writer osw = getA2NOutput(outputFileName);
                String line;

                while ((line = reader.readLine()) != null) {
                    osw.write(line.toCharArray());
                    osw.write(lineBreak);
                    if (outputFileName == null) { // flush stdout
                        osw.flush();
                    }
                }
                reader.close();  // Close the stream.
                osw.close();
            } else {
             //N2A
                String inLine;
                BufferedReader in = getN2AInput(inputFileName);
                BufferedWriter out = getN2AOutput(outputFileName);

                while ((inLine = in.readLine()) != null) {
                    out.write(inLine.toCharArray());
                    out.write(lineBreak);
                    if (outputFileName == null) { // flush stdout
                        out.flush();
                    }
                }
                out.close();
            }
            // Since we are done rename temporary file to desired output file
            if (createOutputFile) {
                if (outputFile.exists()) {
                    // Some win32 platforms can't handle atomic
                    // rename if source and target file paths are
                    // identical. To make things simple we just unconditionally
                    // delete the target file before calling renameTo()
                    outputFile.delete();
                }
                tempFile.renameTo(outputFile);
            }

        } catch(Exception e){
            error(e.toString());
            return false;
        }

        return true;
    }
