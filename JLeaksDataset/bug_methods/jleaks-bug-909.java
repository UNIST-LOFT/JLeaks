    static void process(XRYFolder folder, Content parent) throws IOException, TskCoreException {
        //Get all XRY file readers from this folder.
        List<XRYFileReader> xryFileReaders = folder.getXRYFileReaders();

        for (XRYFileReader xryFileReader : xryFileReaders) {
            String reportType = xryFileReader.getReportType();
            if (XRYFileParserFactory.supports(reportType)) {
                XRYFileParser parser = XRYFileParserFactory.get(reportType);
                parser.parse(xryFileReader, parent);
            } else {
                logger.log(Level.SEVERE, String.format("[XRY DSP] XRY File (in brackets) "
                        + "[ %s ] was found, but no parser to support its report type exists. "
                        + "Report type is [ %s ]", xryFileReader.getReportPath().toString(), reportType));
            }
            xryFileReader.close();
        }
    }
