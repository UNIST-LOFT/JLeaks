private void writeToTxtFile() throws IOException 
{
    try (InputStream xslFile = getLocalizedXSLFile();
        ByteArrayOutputStream output = new ByteArrayOutputStream()) {
        Document dynamicDocument = document;
        generateText(dynamicDocument, xslFile, output);
        textResult = output.toString("UTF-8");
        // dump to text file.
        // NOI18N
        File outputFile = extractResultsFileToTmpDir(outputFileStr + ".txt");
        try (OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(outputFile))) {
            fw.write(textResult);
        }
    }
}