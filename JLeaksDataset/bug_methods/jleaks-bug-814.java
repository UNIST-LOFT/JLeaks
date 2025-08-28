    private void writeToTxtFile() throws IOException {
        InputStream xslFile = getLocalizedXSLFile();
        
        Document dynamicDocument = document;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        generateText(dynamicDocument, xslFile, output);
        textResult = output.toString("UTF-8");
        
        // dump to text file.
        File outputFile = extractResultsFileToTmpDir(outputFileStr + ".txt"); // NOI18N
        OutputStreamWriter fw = new OutputStreamWriter(
                                    new FileOutputStream(outputFile));
        fw.write(textResult);
        fw.close();
    }
